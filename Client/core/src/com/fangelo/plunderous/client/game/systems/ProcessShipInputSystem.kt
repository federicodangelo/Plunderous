package com.fangelo.plunderous.client.game.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.fangelo.libraries.physics.Rigidbody
import com.fangelo.plunderous.client.game.components.ship.Ship
import com.fangelo.plunderous.client.game.components.ship.ShipInput
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import kotlin.math.absoluteValue


class ProcessShipInputSystem : IteratingSystem(allOf(Rigidbody::class, Ship::class, ShipInput::class).get()) {
    private val rigidbody = mapperFor<Rigidbody>()
    private val ship = mapperFor<Ship>()
    private val shipInput = mapperFor<ShipInput>()

    public override fun processEntity(entity: Entity, deltaTime: Float) {
        val rigidbody = rigidbody.get(entity)
        val ship = ship.get(entity)
        val shipInput = shipInput.get(entity)

        val body = rigidbody.native ?: return

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
}