package com.fangelo.libraries.physics.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.fangelo.libraries.light.component.WorldLight
import com.fangelo.libraries.transform.Transform
import ktx.ashley.mapperFor
import ktx.box2d.body
import ktx.box2d.createWorld

class World : Component {

    internal var native: com.badlogic.gdx.physics.box2d.World? = null
    internal var worldLight: WorldLight? = null

    private val maxStepTime = 1 / 45f

    private var accumulator = 0f

    private val transform = mapperFor<Transform>()
    private val rigidbody = mapperFor<Rigidbody>()

    private val bodies = mutableSetOf<Entity>()

    val isEmpty: Boolean
        get() = bodies.isEmpty()

    internal fun initNative() {
        this.native = createWorld(gravity = Vector2())
    }

    internal fun destroyNative() {
        if (!isEmpty)
            Gdx.app.error("World", "Destroying non-empty world, bad things can happen!!")

        this.native?.dispose()
        this.native = null
    }

    fun addRigidbody(rigidbody: Rigidbody, transform: Transform, entity: Entity) {
        rigidbody.initNative(transform)
        bodies.add(entity)
    }

    fun removeRigidbody(rigidbody: Rigidbody, entity: Entity) {
        if (bodies.remove(entity))
            rigidbody.destroyNative()
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