package com.fangelo.plunderous.client.game.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.MathUtils
import com.fangelo.libraries.ashley.components.Rigidbody
import com.fangelo.libraries.ashley.components.Transform
import com.fangelo.plunderous.client.game.components.ship.MainShip
import com.fangelo.plunderous.client.game.components.ship.Ship
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class ProcessShipInputSystem : IteratingSystem(allOf(Rigidbody::class, Ship::class, MainShip::class, Transform::class).get()) {
    private val transform = mapperFor<Transform>()
    private val rigidbody = mapperFor<Rigidbody>()
    private val ship = mapperFor<Ship>()

    public override fun processEntity(entity: Entity, deltaTime: Float) {
        val rigidbody = rigidbody.get(entity)
        val ship = ship.get(entity)
        val transform = transform.get(entity)

        val body = rigidbody.native ?: return

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            body.applyTorque(ship.rotationForce, true)

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            body.applyTorque(-ship.rotationForce, true)

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            val x = -MathUtils.sin(transform.rotation)
            val y = MathUtils.cos(transform.rotation)
            body.applyForceToCenter(x * ship.moveForce, y * ship.moveForce, true)
        }


        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            val x = MathUtils.sin(transform.rotation)
            val y = -MathUtils.cos(transform.rotation)
            body.applyForceToCenter(x * ship.moveForce, y * ship.moveForce, true)
        }

    }
}