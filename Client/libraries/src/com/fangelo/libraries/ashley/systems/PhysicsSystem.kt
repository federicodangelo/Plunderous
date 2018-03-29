package com.fangelo.libraries.ashley.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import com.fangelo.libraries.ashley.components.Rigidbody
import com.fangelo.libraries.ashley.components.Transform
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import ktx.box2d.BodyDefinition

class PhysicsSystem(var world: World) : EntitySystem(), EntityListener {
    private val transform = mapperFor<Transform>()
    private val rigidbody = mapperFor<Rigidbody>()

    override fun addedToEngine(engine: Engine) {
        engine.addEntityListener(allOf(Transform::class, Rigidbody::class).get(), this)
    }

    override fun removedFromEngine(engine: Engine) {
        engine.removeEntityListener(this)
    }

    override fun entityAdded(entity: Entity?) {
        val rigidbody = this.rigidbody.get(entity)
        initRigidbody(entity, rigidbody)
    }

    override fun entityRemoved(entity: Entity?) {
        val rigidbody = this.rigidbody.get(entity)
        world.destroyBody(rigidbody.native)
        rigidbody.native = null
    }

    private fun initRigidbody(entity: Entity?, rigidbody: Rigidbody) {
        val transform = this.transform.get(entity)
        val bodyDefinition = rigidbody.definition ?: return

        updateFromTransform(bodyDefinition, transform)

        val body = world.createBody(rigidbody.definition)
        body.userData = rigidbody
        initFixtures(rigidbody, body, bodyDefinition)
        bodyDefinition.creationCallback?.let { it(body) }

        rigidbody.native = body
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
}