package com.fangelo.libraries.physics.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d.Body
import com.fangelo.libraries.transform.Transform
import ktx.box2d.BodyDefinition

class Rigidbody : Component {

    var definition: BodyDefinition? = null
    var world: World? = null
    var native: Body? = null //TODO: Make internal!!

    fun set(world: World, definition: BodyDefinition) {
        this.world = world
        this.definition = definition
    }

    internal fun initNative(transform: Transform) {
        val definition = this.definition

        if (definition == null) {
            Gdx.app.error("Rigidbody", "Missing body definition")
            return
        }

        val world = this.world

        if (world == null) {
            Gdx.app.error("Rigidbody", "Missing world configuration")
            return
        }

        val native = world.native

        if (native == null) {
            Gdx.app.error("World", "Native world not initialized")
            return
        }

        updateFromTransform(definition, transform)

        val body = native.createBody(definition)
        body.userData = this
        initFixtures(body, definition)
        definition.creationCallback?.let { it(body) }

        this.native = body
    }

    private fun updateFromTransform(bodyDefinition: BodyDefinition, transform: Transform) {
        bodyDefinition.position.set(transform.x, transform.y)
        bodyDefinition.angle = transform.rotation
    }

    private fun initFixtures(body: Body, bodyDefinition: BodyDefinition) {
        for (fixtureDefinition in bodyDefinition.fixtureDefinitions) {
            val fixture = body.createFixture(fixtureDefinition)
            fixture.userData = this
            fixtureDefinition.creationCallback?.let { it(fixture) }
            fixtureDefinition.shape.dispose()
            fixtureDefinition.shape = null
        }
    }

    internal fun destroyNative() {
        val native = this.native

        if (native == null) {
            Gdx.app.error("World", "Native world not initialized")
            return
        }

        world?.native?.destroyBody(native)
        this.native = null
    }
}