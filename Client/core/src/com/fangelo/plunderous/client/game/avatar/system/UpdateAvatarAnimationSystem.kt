package com.fangelo.plunderous.client.game.avatar.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.fangelo.libraries.physics.component.Rigidbody
import com.fangelo.libraries.sprite.component.VisualAnimation
import com.fangelo.plunderous.client.game.avatar.component.Avatar
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class UpdateAvatarAnimationSystem : IteratingSystem(allOf(Rigidbody::class, Avatar::class, VisualAnimation::class).get()) {
    private val rigidbody = mapperFor<Rigidbody>()
    private val visualAnimation = mapperFor<VisualAnimation>()

    public override fun processEntity(entity: Entity, deltaTime: Float) {
        val rigidbody = rigidbody.get(entity)
        val visualAnimation = visualAnimation.get(entity)

        val velocity = rigidbody.native?.linearVelocity ?: Vector2.Zero

        when {
            velocity.x > 0 -> visualAnimation.playAnimation("walk-east")
            velocity.x < 0 -> visualAnimation.playAnimation("walk-west")
            velocity.y < 0 -> visualAnimation.playAnimation("walk-north")
            velocity.y > 0 -> visualAnimation.playAnimation("walk-south")
            else -> visualAnimation.stop()
        }
    }
}