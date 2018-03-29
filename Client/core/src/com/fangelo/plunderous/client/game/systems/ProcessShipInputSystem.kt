package com.fangelo.plunderous.client.game.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.physics.box2d.Body
import com.fangelo.libraries.ashley.components.Camera
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
    private val camera = mapperFor<Camera>()
    private lateinit var cameras: ImmutableArray<Entity>

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        cameras = engine.getEntitiesFor(allOf(Camera::class).get())
    }

    public override fun processEntity(entity: Entity, deltaTime: Float) {
        val rigidbody = rigidbody.get(entity)
        val ship = ship.get(entity)
        val transform = transform.get(entity)

        val body = rigidbody.native ?: return

        handleTouch(body, ship, transform)

        handleKeyboard(body, ship, transform)
    }

    private fun handleTouch(body: Body, ship: Ship, transform: Transform) {
        if (!Gdx.input.isTouched)
            return

        if (cameras.size() == 0)
            return

        val camera = camera.get(cameras.first())

        val x = Gdx.input.x
        val y = Gdx.input.y

        val shipForward = transform.forward
        val shipRight = transform.right

        var touchWorldPos = camera.screenPositionToWorldPosition(x.toFloat(), y.toFloat())

        touchWorldPos.sub(transform.x, transform.y)

        val forwardDistance = touchWorldPos.dot(shipForward)
        val rightDistance = touchWorldPos.dot(shipRight)

        if (forwardDistance > 2.0f) {
            moveForward(transform, body, ship)
        } else if (forwardDistance < -2.0f) {
            moveBackwards(transform, body, ship)
        }

        if (rightDistance > 2.0f) {
            rotateRight(body, ship)
        } else if (rightDistance < -2.0f) {
            rotateLeft(body, ship)
        }
    }

    private fun handleKeyboard(body: Body, ship: Ship, transform: Transform) {
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            rotateRight(body, ship)

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            rotateLeft(body, ship)

        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            moveForward(transform, body, ship)

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            moveBackwards(transform, body, ship)
    }

    private fun rotateLeft(body: Body, ship: Ship) {
        body.applyTorque(-ship.rotationForce, true)
    }

    private fun rotateRight(body: Body, ship: Ship) {
        body.applyTorque(ship.rotationForce, true)
    }

    private fun moveForward(transform: Transform, body: Body, ship: Ship) {
        body.applyForceToCenter(transform.forward.scl(ship.moveForce), true)
    }

    private fun moveBackwards(transform: Transform, body: Body, ship: Ship) {
        body.applyForceToCenter(transform.backward.scl(ship.moveForce), true)
    }
}