package com.fangelo.plunderous.client.game.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.fangelo.libraries.ashley.components.Camera
import com.fangelo.libraries.ashley.components.Rigidbody
import com.fangelo.libraries.ashley.components.Transform
import com.fangelo.libraries.ui.ScreenManager
import com.fangelo.plunderous.client.game.components.ship.MainShip
import com.fangelo.plunderous.client.game.components.ship.Ship
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import kotlin.math.absoluteValue


class ShipInput(var left: Boolean = false, var right: Boolean = false, var forward: Boolean = false, var backward: Boolean = false)

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

        val shipInput = ShipInput()

        getTouchInput(shipInput, transform)

        getKeyboardInput(shipInput)

        updateRudder(shipInput, ship, deltaTime)

        updatePhysics(body, ship, shipInput)
    }

    private fun updatePhysics(body: Body, ship: Ship, shipInput: ShipInput) {
        updateFriction(body)
        updateDrive(ship, body, shipInput)
        updateTorque(ship, body)
    }

    private fun updateTorque(ship: Ship, body: Body) {
        var currentForwardNormal = body.getWorldVector(Vector2(0f, 1f))
        var currentSpeed = getForwardVelocity(body).cpy().dot(currentForwardNormal)

        var torqueForce = (currentSpeed / ship.maxSpeed) * (ship.rudderRotation / ship.maxRudderRotation) * ship.maxRudderTorque

        if (torqueForce.absoluteValue > 0.01f)
            body.applyTorque(torqueForce, true)
    }

    private fun updateDrive(ship: Ship, body: Body, shipInput: ShipInput) {

        var desiredSpeed = 0f
        if (shipInput.forward)
            desiredSpeed = ship.maxSpeed
        else if (shipInput.backward)
            desiredSpeed = -ship.maxSpeed * 0.1f

        //find current speed in forward direction
        var currentForwardNormal = body.getWorldVector(Vector2(0f, 1f))
        var currentSpeed = getForwardVelocity(body).cpy().dot(currentForwardNormal)

        //apply necessary force
        var force = when {
            desiredSpeed > 0 && currentSpeed < desiredSpeed -> ship.driveForce
            desiredSpeed < 0 && currentSpeed <= 0.1f && currentSpeed > desiredSpeed -> -ship.driveForce
            else -> 0f
        }

        if (force.absoluteValue > 0.01f)
            body.applyForce(currentForwardNormal.scl(force), body.worldCenter, true)
    }

    private fun updateFriction(body: Body) {
        var impulse = getLateralVelocity(body).scl(-body.mass)

        val maxLateralImpulse = 3f
        impulse.limit(maxLateralImpulse)

        body.applyLinearImpulse(impulse, body.worldCenter, true)
    }

    private fun getLateralVelocity(body: Body): Vector2 {
        val currentRightNormal = body.getWorldVector(Vector2(1f, 0f)).cpy()
        return currentRightNormal.scl(currentRightNormal.cpy().dot(body.linearVelocity))
    }

    private fun getForwardVelocity(body: Body): Vector2 {
        val currentForwardNormal = body.getWorldVector(Vector2(0f, 1f)).cpy()
        return currentForwardNormal.scl(currentForwardNormal.cpy().dot(body.linearVelocity))
    }

    private fun updateRudder(shipInput: ShipInput, ship: Ship, deltaTime: Float) {
        if (shipInput.right)
            rotateRudderRight(ship, deltaTime)

        if (shipInput.left)
            rotateRudderLeft(ship, deltaTime)
    }

    private fun rotateRudderLeft(ship: Ship, deltaTime: Float) {
        ship.rudderRotation = MathUtils.clamp(
            ship.rudderRotation - ship.rudderRotationSpeed * deltaTime, -ship.maxRudderRotation, ship.maxRudderRotation
        )
    }

    private fun rotateRudderRight(ship: Ship, deltaTime: Float) {
        ship.rudderRotation = MathUtils.clamp(
            ship.rudderRotation + ship.rudderRotationSpeed * deltaTime, -ship.maxRudderRotation, ship.maxRudderRotation
        )
    }

    private fun getTouchInput(shipInput: ShipInput, transform: Transform) {
        if (!Gdx.input.isTouched)
            return

        if (cameras.size() == 0)
            return

        val camera = camera.get(cameras.first())

        val x = Gdx.input.x
        val y = Gdx.input.y

        if (ScreenManager.isUiAtScreenPosition(x.toFloat(), y.toFloat()))
            return

        val shipForward = transform.forward
        val shipRight = transform.right

        var touchWorldPos = camera.screenPositionToWorldPosition(x.toFloat(), y.toFloat())

        touchWorldPos.sub(transform.x, transform.y)

        val forwardDistance = touchWorldPos.dot(shipForward)
        val rightDistance = touchWorldPos.dot(shipRight)

        if (forwardDistance > 2.0f) {
            shipInput.forward = true
        } else if (forwardDistance < -2.0f) {
            shipInput.backward = true
        }

        if (rightDistance > 2.0f) {
            shipInput.right = true
        } else if (rightDistance < -2.0f) {
            shipInput.left = true
        }
    }

    private fun getKeyboardInput(shipInput: ShipInput) {
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            shipInput.right = true

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            shipInput.left = true

        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            shipInput.forward = true

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            shipInput.backward = true
    }
}