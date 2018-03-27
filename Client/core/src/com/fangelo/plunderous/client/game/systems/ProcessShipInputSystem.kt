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
import ktx.math.vec2

class ProcessShipInputSystem : IteratingSystem(allOf(Rigidbody::class, Ship::class, MainShip::class, Transform::class).get()) {
    private val transform = mapperFor<Transform>()
    private val movement = mapperFor<Rigidbody>()
    private val avatar = mapperFor<Ship>()

    private val tmpVelocity = vec2()

    public override fun processEntity(entity: Entity, deltaTime: Float) {
        val movement = movement.get(entity)
        val avatar = avatar.get(entity)
        val transform = transform.get(entity)

        tmpVelocity.set(movement.velocityX, movement.velocityY)
        var tmpVelocityRot = movement.velocityRot

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            tmpVelocityRot += avatar.rotationAcc * deltaTime

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            tmpVelocityRot -= avatar.rotationAcc * deltaTime

        if (!Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            if (tmpVelocityRot > 0f) {

                tmpVelocityRot -= avatar.rotationFric * deltaTime
                if (tmpVelocityRot < 0f)
                    tmpVelocityRot = 0f

            } else if (tmpVelocityRot < 0f) {

                tmpVelocityRot += avatar.rotationFric * deltaTime
                if (tmpVelocityRot > 0f)
                    tmpVelocityRot = 0f
            }

        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            tmpVelocity.x -= MathUtils.sin(transform.rot) * avatar.moveAcc * deltaTime
            tmpVelocity.y += MathUtils.cos(transform.rot) * avatar.moveAcc * deltaTime
        }

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            tmpVelocity.x += MathUtils.sin(transform.rot) * avatar.moveAcc * deltaTime
            tmpVelocity.y -= MathUtils.cos(transform.rot) * avatar.moveAcc * deltaTime
        }

        if (!tmpVelocity.isZero) {
            if (tmpVelocity.len() > avatar.maxMoveSpeed) {
                tmpVelocity.nor().scl(avatar.maxMoveSpeed)
            }
        }

        tmpVelocityRot = MathUtils.clamp(tmpVelocityRot, -avatar.maxRotationSpeed, avatar.maxRotationSpeed)

        movement.velocityX = tmpVelocity.x
        movement.velocityY = tmpVelocity.y
        movement.velocityRot = tmpVelocityRot
    }
}