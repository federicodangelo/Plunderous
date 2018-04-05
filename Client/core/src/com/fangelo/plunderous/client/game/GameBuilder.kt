package com.fangelo.plunderous.client.game

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.light.component.Light
import com.fangelo.libraries.light.component.WorldLight
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
import com.fangelo.plunderous.client.game.generator.component.GeneratorAreaSource
import com.fangelo.plunderous.client.game.generator.generators.IslandsGeneratorFactory
import com.fangelo.plunderous.client.game.ship.component.MainShip
import com.fangelo.plunderous.client.game.ship.component.Ship
import com.fangelo.plunderous.client.game.ship.component.ShipInput
import com.fangelo.plunderous.client.game.water.component.VisualWater
import com.fangelo.plunderous.client.game.water.component.Water
import ktx.ashley.entity
import ktx.box2d.BodyDefinition
import ktx.collections.toGdxArray

class GameBuilder {

    private val playerShipSpawnPositionX = 0f
    private val playerShipSpawnPositionY = 0f

    private lateinit var engine: Engine
    private lateinit var assetManager: AssetManager
    private lateinit var camera: Camera

    var playerShip: Entity? = null
        private set

    var playerAvatar: Entity? = null
        private set

    var mainWorld: World? = null
        private set

    var shipWorld: World? = null
        private set

    fun build(engine: Engine, assetManager: AssetManager, camera: Camera) {

        this.engine = engine
        this.assetManager = assetManager
        this.camera = camera

        addMainWorld()
        addWater()
        addPlayerShip()
        addPlayerAvatar()
        addGenerators()
    }

    private fun addGenerators() {
        engine.entity().add(IslandsGeneratorFactory(engine, mainWorld!!, assetManager).buildComponent())
    }

    private fun addMainWorld() {

        val entity = engine.entity {
            with<World>()
            with<WorldLight>()
        }

        mainWorld = entity.getComponent(World::class.java)
    }

    private fun addWater() {
        val tilesetAtlas = assetManager.get<TextureAtlas>("tiles/tiles.atlas")
        val waterTileTexture = tilesetAtlas.findRegion("water")

        engine.entity {
            with<Water>()
            with<VisualWater> {
                this.waterTileTexture = waterTileTexture
            }
        }
    }

    private fun addPlayerShip() {

        val shipAtlas = assetManager.get<TextureAtlas>("ships/ships.atlas")
        val shipRegion = shipAtlas.findRegion("ship")

        val playerShip = engine.entity {
            with<Transform> {
                set(playerShipSpawnPositionX, playerShipSpawnPositionY, MathUtils.PI)
            }
            with<Rigidbody> {
                set(mainWorld!!, buildShipBodyDefinition())
            }
            with<Ship>()
            with<MainShip>()
            with<ShipInput>()
            with<VisualSprite> {
                add(Sprite(shipRegion, 2f, 3f, 0f, 0f))
            }
            with<Light> {
                set(mainWorld!!, 10.0f, Color.WHITE)
            }
            with<World>()
            with<WorldLight> {
                renderFlags = GameRenderFlags.ship
            }
        }

        val generatorAreaSource = GeneratorAreaSource()
        generatorAreaSource.followCamera = camera
        generatorAreaSource.followTransform = playerShip.getComponent(Transform::class.java)
        generatorAreaSource.followTransformRadius = 5.0f

        playerShip.add(generatorAreaSource)

        shipWorld = playerShip.getComponent(World::class.java)
        shipWorld?.followTransform = playerShip.getComponent(Transform::class.java)
        shipWorld?.buildBounds(2f, 3f, 0.25f)

        this.playerShip = playerShip
    }

    private fun buildShipBodyDefinition(): BodyDefinition {
        val shipBack = -1.25f
        val shipBackHalfWidth = 0.4f

        val shipMiddle = 0.5f
        val shipMiddleHalfWidth = 0.6f

        val shipFront = 1.45f

        val shipBodyDefinition = BodyDefinition()

        shipBodyDefinition.type = BodyDef.BodyType.DynamicBody

        shipBodyDefinition.polygon(
            Vector2(-shipBackHalfWidth, shipBack),
            Vector2(-shipMiddleHalfWidth, shipMiddle),
            Vector2(0f, shipFront),
            Vector2(shipMiddleHalfWidth, shipMiddle),
            Vector2(shipBackHalfWidth, shipBack)
        ) {
            density = 1f
            restitution = 0f
            friction = 0.2f
        }
        shipBodyDefinition.linearDamping = 0.5f
        shipBodyDefinition.angularDamping = 0.9f
        return shipBodyDefinition
    }


    private fun addPlayerAvatar() {

        val playersAtlas = assetManager.get<TextureAtlas>("players/players.atlas")
        val playerRegion = playersAtlas.findRegion("player-walk-south-0")
        val playerAnimations = buildPlayerAnimations("player", playersAtlas)

        val playerAvatar = engine.entity {
            with<Transform> {
                set(0f, 0f, 0f)
            }
            with<Rigidbody> {
                set(shipWorld!!, buildPlayerBodyDefinition())
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
                set(shipWorld!!, 2.0f, Color.WHITE)
            }
        }

        this.playerAvatar = playerAvatar
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