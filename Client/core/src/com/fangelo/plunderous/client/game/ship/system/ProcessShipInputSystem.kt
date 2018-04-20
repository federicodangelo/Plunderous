package com.fangelo.plunderous.client.game.ship.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.fangelo.libraries.physics.component.Rigidbody
import com.fangelo.plunderous.client.game.ship.component.Ship
import com.fangelo.plunderous.client.game.ship.component.ShipInput
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import kotlin.math.absoluteValue
import kotlin.math.sign

class ProcessShipInputSystem : IteratingSystem(allOf(Rigidbody::class, Ship::class, ShipInput::class).get()) {
    private val rigidbody = mapperFor<Rigidbody>()
    private val ship = mapperFor<Ship>()
    private val shipInput = mapperFor<ShipInput>()

    var movementType = ShipMovementType.SIMPLIFIED

    public override fun processEntity(entity: Entity, deltaTime: Float) {
        val rigidbody = rigidbody.get(entity)
        val ship = ship.get(entity)
        val shipInput = shipInput.get(entity)

        val body = rigidbody.native ?: return

        updateRudder(shipInput, ship, deltaTime)
        updatePhysics(body, rigidbody, ship, shipInput)
    }

    private fun updatePhysics(body: Body, rigidbody: Rigidbody, ship: Ship, shipInput: ShipInput) {
        when (movementType) {
            ShipMovementType.REALISTIC -> updatePhysicsRealistic(body, ship, shipInput)
            ShipMovementType.SIMPLIFIED -> updatePhysicsSimplified(body, rigidbody, ship, shipInput)
        }
    }

    private fun updatePhysicsSimplified(body: Body, rigidbody: Rigidbody, ship: Ship, shipInput: ShipInput) {
        updateTorqueSimplified(body, shipInput, rigidbody)

        updateDriveSimplified(body, rigidbody, ship, shipInput)
    }

    private fun updateDriveSimplified(body: Body, rigidbody: Rigidbody, ship: Ship, shipInput: ShipInput) {
        if (isTargetRotationTooFarAway(body, shipInput))
            return

        var desiredSpeed = MathUtils.clamp(shipInput.targetSpeed, ship.maxBackwardSpeed, ship.maxForwardSpeed)

        //find current targetSpeed in up direction
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

    private fun isTargetRotationTooFarAway(body: Body, shipInput: ShipInput): Boolean {
        val bodyAngle = body.angle
        val desiredAngle = shipInput.targetRudderRotation
        val rotationDelta = normalizeRotation(desiredAngle - bodyAngle)

        return rotationDelta.absoluteValue > MathUtils.PI / 2.0f
    }

    private fun updateTorqueSimplified(body: Body, shipInput: ShipInput, rigidbody: Rigidbody) {
        val bodyAngle = body.angle
        val desiredAngle = shipInput.targetRudderRotation

        val worldStepTime = (rigidbody.world?.stepTime ?: 1f)

        val bodyAngularVelocity = body.angularVelocity

        val nextAngle = bodyAngle + bodyAngularVelocity * worldStepTime
        val currentRotationDelta = normalizeRotation(desiredAngle - bodyAngle)
        val nextRotationDelta = normalizeRotation(desiredAngle - nextAngle)

        var desiredAngularVelocity = nextRotationDelta / worldStepTime

        if (currentRotationDelta.sign == nextRotationDelta.sign) {
            //Clamp rotation speed when the target isn't reached yet
            val maxChange = 1 * MathUtils.degreesToRadians
            desiredAngularVelocity = MathUtils.clamp(desiredAngularVelocity, -maxChange, maxChange)
        }

        val impulse = body.inertia * desiredAngularVelocity
        if (impulse.absoluteValue > 0.01f)
            body.applyAngularImpulse(impulse, true)
    }

    private fun normalizeRotation(rotation: Float): Float {
        var clampedRotation = rotation
        while (clampedRotation < -180 * MathUtils.degreesToRadians) clampedRotation += 360 * MathUtils.degreesToRadians
        while (clampedRotation > 180 * MathUtils.degreesToRadians) clampedRotation -= 360 * MathUtils.degreesToRadians
        return clampedRotation
    }

    private fun updatePhysicsRealistic(body: Body, ship: Ship, shipInput: ShipInput) {
        updateFriction(body)
        updateDrive(ship, body, shipInput)
        updateTorque(ship, body)
    }

    private fun updateTorque(ship: Ship, body: Body) {
        var currentForwardNormal = body.getWorldVector(Vector2(0f, 1f))
        var currentSpeed = getForwardVelocity(body).cpy().dot(currentForwardNormal)

        var torqueForce = (currentSpeed / ship.maxForwardSpeed) * (ship.rudderRotation / ship.maxRudderRotation) * ship.maxRudderTorque

        if (torqueForce.absoluteValue > 0.01f)
            body.applyTorque(torqueForce, true)
    }

    private fun updateDrive(ship: Ship, body: Body, shipInput: ShipInput) {

        var desiredSpeed = MathUtils.clamp(shipInput.targetSpeed, ship.maxBackwardSpeed, ship.maxForwardSpeed)

        //find current targetSpeed in up direction
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
        when (movementType) {
            ShipMovementType.SIMPLIFIED -> updateRudderSimplified(shipInput, ship)
            ShipMovementType.REALISTIC -> updateRudderRealistic(shipInput, ship, deltaTime)
        }
    }

    private fun updateRudderSimplified(shipInput: ShipInput, ship: Ship) {
        ship.rudderRotation = shipInput.targetRudderRotation
    }

    private fun updateRudderRealistic(shipInput: ShipInput, ship: Ship, deltaTime: Float) {
        if (shipInput.targetRudderRotation > ship.rudderRotation)
            rotateRudderRight(ship, deltaTime)
        else if (shipInput.targetRudderRotation < ship.rudderRotation)
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