package com.fangelo.plunderous.client.game.avatar.factory

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.physics.box2d.BodyDef
import com.fangelo.libraries.light.component.Light
import com.fangelo.libraries.physics.component.Rigidbody
import com.fangelo.libraries.physics.component.World
import com.fangelo.libraries.sprite.Sprite
import com.fangelo.libraries.sprite.component.VisualAnimation
import com.fangelo.libraries.sprite.component.VisualSprite
import com.fangelo.libraries.transform.Transform
import com.fangelo.plunderous.client.game.avatar.component.Avatar
import com.fangelo.plunderous.client.game.avatar.component.AvatarInput
import com.fangelo.plunderous.client.game.avatar.component.MainAvatar
import com.fangelo.plunderous.client.game.constants.GameRenderFlags
import ktx.ashley.entity
import ktx.box2d.BodyDefinition
import ktx.collections.toGdxArray

class AvatarFactory(val assetManager: AssetManager, val engine: Engine) {

    fun buildMainAvatar(world: World, positionX: Float, positionY: Float): Entity {

        val playersAtlas = assetManager.get<TextureAtlas>("players/players.atlas")
        val playerRegion = playersAtlas.findRegion("player-walk-south-0")
        val playerAnimations = buildPlayerAnimations("player", playersAtlas)

        val playerAvatar = engine.entity {
            with<Transform> {
                set(positionX, positionY, 0f)
            }
            with<Rigidbody> {
                set(world, buildPlayerBodyDefinition())
            }
            with<Avatar>()
            with<MainAvatar>()
            with<AvatarInput>()
            with<VisualSprite> {
                add(Sprite(playerRegion, 0.5f, 0.5f, 0f, -0.175f))
                layer = 1
                renderFlags = GameRenderFlags.ship
            }
            with<VisualAnimation> {
                set(playerAnimations, "walk-east")
            }
            with<Light> {
                set(world, 3.0f, Color.WHITE)
            }
        }

        return playerAvatar
    }


    private fun buildPlayerAnimations(playerName: String, playersAtlas: TextureAtlas): Map<String, Animation<TextureRegion>> {
        val animations = mutableMapOf<String, Animation<TextureRegion>>()

        addAnimations(animations, playersAtlas, playerName, "walk-north", 9, Animation.PlayMode.LOOP)
        addAnimations(animations, playersAtlas, playerName, "walk-west", 9, Animation.PlayMode.LOOP)
        addAnimations(animations, playersAtlas, playerName, "walk-south", 9, Animation.PlayMode.LOOP)
        addAnimations(animations, playersAtlas, playerName, "walk-east", 9, Animation.PlayMode.LOOP)

        return animations
    }

    private fun addAnimations(
        animations: MutableMap<String, Animation<TextureRegion>>, atlas: TextureAtlas, playerName: String, animationPrefix: String,
        totalFrames: Int, playMode: Animation.PlayMode
    ) {

        val frames: MutableList<TextureRegion> = mutableListOf()

        for (frameNumber in 0 until totalFrames) {
            val frame = atlas.findRegion("$playerName-$animationPrefix-$frameNumber")!!
            frames.add(frame)
        }

        val animation = Animation<TextureRegion>(0.1f, frames.toGdxArray(), playMode)

        animations[animationPrefix] = animation
    }

    private fun buildPlayerBodyDefinition(): BodyDefinition {

        val playerBodyDefinition = BodyDefinition()

        playerBodyDefinition.type = BodyDef.BodyType.DynamicBody

        playerBodyDefinition.circle(0.1f) {
            density = 1f
            restitution = 0f
            friction = 0.2f
        }
        playerBodyDefinition.linearDamping = 0.5f
        playerBodyDefinition.fixedRotation = true
        return playerBodyDefinition
    }
}