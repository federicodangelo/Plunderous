package com.fangelo.libraries.ashley.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import ktx.ashley.mapperFor
import ktx.box2d.BodyDefinition
import ktx.box2d.body

class World : Component {

    var native: com.badlogic.gdx.physics.box2d.World? = null

    private val maxStepTime = 1 / 45f

    private var accumulator = 0f

    private val transform = mapperFor<Transform>()
    private val rigidbody = mapperFor<Rigidbody>()

    private val bodies = mutableSetOf<Entity>()

    val isEmpty: Boolean
        get() = bodies.isEmpty()

    fun addRigidbody(rigidbody: Rigidbody, transform: Transform, entity: Entity) {
        initRigidbody(entity, rigidbody, transform)
        bodies.add(entity)
    }

    fun removeRigidbody(rigidbody: Rigidbody, entity: Entity) {
        if (bodies.remove(entity))
            destroyRigidbody(rigidbody)
    }

    private fun initRigidbody(entity: Entity, rigidbody: Rigidbody, transform: Transform) {
        val bodyDefinition = rigidbody.definition

        if (bodyDefinition == null) {
            Gdx.app.error("World", "Missing body definition in rigidbody of entity $entity")
            return
        }

        val native = this.native

        if (native == null) {
            Gdx.app.error("World", "Native world not initialized")
            return
        }

        updateFromTransform(bodyDefinition, transform)

        val body = native.createBody(rigidbody.definition)
        body.userData = rigidbody
        initFixtures(rigidbody, body, bodyDefinition)
        bodyDefinition.creationCallback?.let { it(body) }

        rigidbody.native = body
    }

    private fun destroyRigidbody(rigidbody: Rigidbody) {
        val native = this.native

        if (native == null) {
            Gdx.app.error("World", "Native world not initialized")
            return
        }

        native.destroyBody(rigidbody.native)
        rigidbody.native = null
    }

    private fun updateFromTransform(bodyDefinition: BodyDefinition, transform: Transform) {
        bodyDefinition.position.set(transform.x, transform.y)
        bodyDefinition.angle = transform.rotation
    }

    private fun initFixtures(rigidbody: Rigidbody, body: Body, bodyDefinition: BodyDefinition) {
        for (fixtureDefinition in bodyDefinition.fixtureDefinitions) {
            val fixture = body.createFixture(fixtureDefinition)
            fixture.userData = rigidbody
            fixtureDefinition.creationCallback?.let { it(fixture) }
            fixtureDefinition.shape.dispose()
            fixtureDefinition.shape = null
        }
    }

    fun updatePhysics(deltaTime: Float) {
        val native = this.native

        if (native == null) {
            Gdx.app.error("World", "Native world not initialized")
            return
        }

        val frameTime = Math.min(deltaTime, 0.25f)
        accumulator += frameTime
        if (accumulator >= maxStepTime) {
            accumulator -= maxStepTime
            native.step(maxStepTime, 6, 2)

            updateBodiesTransforms()
        }
    }

    private fun updateBodiesTransforms() {
        for (entity in bodies) {
            val transform = this.transform.get(entity)
            val rigidbody = this.rigidbody.get(entity)
            val bodyNative = rigidbody.native ?: continue

            val position = bodyNative.position
            transform.x = position.x
            transform.y = position.y
            transform.rotation = bodyNative.angle
        }
    }

    fun buildBounds(width: Float, height: Float) {

        val native = this.native

        if (native == null) {
            Gdx.app.error("World", "Missing native initialization in $this")
            return
        }

        //Left
        native.body {
            box(
                height = height,
                position = Vector2(0f, height * 0.5f)
            )
        }
        //Right
        native.body {
            box(
                height = height,
                position = Vector2(width, height * 0.5f)
            )
        }
        //Top
        native.body {
            box(
                width = width,
                position = Vector2(width * 0.5f, 0f)
            )
        }
        //Bottom
        native.body {
            box(
                width = width,
                position = Vector2(width * 0.5f, height)
            )
        }
    }
}