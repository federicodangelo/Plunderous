package com.fangelo.plunderous.client.game.avatar.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.fangelo.libraries.physics.component.Rigidbody
import com.fangelo.plunderous.client.game.avatar.component.Avatar
import com.fangelo.plunderous.client.game.avatar.component.AvatarInput
import ktx.ashley.allOf
import ktx.ashley.mapperFor


class ProcessAvatarInputSystem : IteratingSystem(allOf(Rigidbody::class, Avatar::class, AvatarInput::class).get()) {
    private val rigidbody = mapperFor<Rigidbody>()
    private val avatar = mapperFor<Avatar>()
    private val avatarInput = mapperFor<AvatarInput>()

    public override fun processEntity(entity: Entity, deltaTime: Float) {
        val rigidbody = rigidbody.get(entity)
        val avatar = avatar.get(entity)
        val avatarInput = avatarInput.get(entity)

        val body = rigidbody.native ?: return

        updatePhysics(avatar, avatarInput, body)
    }

    private fun updatePhysics(avatar: Avatar, avatarInput: AvatarInput, body: Body) {

        var targetVelocity = Vector2()

        if (avatarInput.left)
            targetVelocity.x -= avatar.walkSpeed
        if (avatarInput.right)
            targetVelocity.x += avatar.walkSpeed
        if (avatarInput.up)
            targetVelocity.y -= avatar.walkSpeed
        if (avatarInput.down)
            targetVelocity.y += avatar.walkSpeed

        targetVelocity.clamp(0f, avatar.walkSpeed)

        val currentVelocity = body.linearVelocity

        if (!currentVelocity.epsilonEquals(targetVelocity)) {
            val velocityChange = targetVelocity.cpy().sub(currentVelocity)
            val impulse = velocityChange.cpy().scl(body.mass)

            body.applyLinearImpulse(impulse, body.worldCenter, true)
        }
    }
}