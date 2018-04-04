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
import com.fangelo.libraries.light.component.Light
import com.fangelo.libraries.light.component.WorldLight
import com.fangelo.libraries.physics.component.Rigidbody
import com.fangelo.libraries.physics.component.World
import com.fangelo.libraries.sprite.Sprite
import com.fangelo.libraries.sprite.component.VisualAnimation
import com.fangelo.libraries.sprite.component.VisualSprite
import com.fangelo.libraries.tilemap.Tileset
import com.fangelo.libraries.tilemap.component.Tilemap
import com.fangelo.libraries.transform.Transform
import com.fangelo.plunderous.client.game.avatar.component.Avatar
import com.fangelo.plunderous.client.game.avatar.component.AvatarInput
import com.fangelo.plunderous.client.game.avatar.component.MainAvatar
import com.fangelo.plunderous.client.game.constants.GameRenderFlags
import com.fangelo.plunderous.client.game.island.component.Island
import com.fangelo.plunderous.client.game.island.component.VisualIsland
import com.fangelo.plunderous.client.game.ship.component.MainShip
import com.fangelo.plunderous.client.game.ship.component.Ship
import com.fangelo.plunderous.client.game.ship.component.ShipInput
import ktx.ashley.entity
import ktx.box2d.BodyDefinition
import ktx.collections.toGdxArray

class GameBuilder {

    private val mapWidth = 128
    private val mapHeight = 128

    private val playerShipSpawnPositionX = 0f
    private val playerShipSpawnPositionY = 0f

    private lateinit var engine: Engine
    private lateinit var assetManager: AssetManager

    var playerShip: Entity? = null
        private set

    var playerAvatar: Entity? = null
        private set

    var mainWorld: World? = null
        private set

    var shipWorld: World? = null
        private set

    fun build(engine: Engine, assetManager: AssetManager) {

        this.engine = engine
        this.assetManager = assetManager

        addMainWorld()
        addWater()
        addPlayerShip()
        addPlayerAvatar()
        addIslands()
    }

    private fun addIslands() {
        getRandomPositions().forEach { pos -> addIsland(pos.x, pos.y, MathUtils.random(2.0f, 5.5f)) }
    }

    private fun addIsland(x: Float, y: Float, radius: Float) {

        if (!isValidPosition(x, y))
            return

        engine.entity {
            with<Transform> {
                set(x, y, 0f)
            }
            with<Rigidbody> {
                set(mainWorld!!, buildIslandBodyDefinition(radius))
            }
            with<Island> {
                set(radius)
            }
            with<VisualIsland> {

                set(Color.CORAL)
            }
        }

        addIslandItems(radius, x, y)
    }

    private fun addIslandItems(radius: Float, x: Float, y: Float) {
        val itemsAtlas = assetManager.get<TextureAtlas>("items/items.atlas")

        getRandomPositionsInsideCircle(radius - 1.0f, 5).forEach { pos ->
            when (MathUtils.random(0, 2)) {
                0 -> addSimpleItem(itemsAtlas, "rock1", pos.x + x, pos.y + y)
                1 -> addSimpleItem(itemsAtlas, "rock2", pos.x + x, pos.y + y)
                2 -> addTree(itemsAtlas, pos.x + x, pos.y + y)
            }
        }
    }

    private fun addMainWorld() {

        val entity = engine.entity {
            with<World>()
            with<WorldLight>()
        }

        mainWorld = entity.getComponent(World::class.java)
        mainWorld?.buildBounds(mapWidth.toFloat(), mapHeight.toFloat())
    }

    private fun addWater() {
        val tilesetAtlas = assetManager.get<TextureAtlas>("tiles/tiles.atlas")
        val tileset = buildTileset(tilesetAtlas)
        val width = mapWidth
        val height = mapHeight
        val tiles = Array(width * height, { MathUtils.random(tileset.tilesTextures.size - 1) })

        engine.entity {
            with<Transform>()
            with<Tilemap> {
                set(width, height, tiles, tileset)
            }
        }
    }

    private fun buildTileset(tilesetAtlas: TextureAtlas): Tileset {

        return Tileset(
            arrayOf(
                tilesetAtlas.findRegion("water")
            )
        )
    }

    private fun getRandomPositions(amount: Int = 25): Array<Vector2> {
        return Array(amount, { _ ->
            Vector2(
                MathUtils.random(-mapWidth * 0.5f + 2f, mapWidth * 0.5f - 2f),
                MathUtils.random(-mapHeight * 0.5f + 2f, mapHeight * 0.5f - 2f)
            )
        })
    }

    private fun getRandomPositionsInsideCircle(radius: Float, amount: Int = 25): Array<Vector2> {
        return Array(amount, { _ -> clampToCircle(Vector2(MathUtils.random(-radius, radius), MathUtils.random(-radius, radius)), radius) })
    }

    private fun clampToCircle(vector: Vector2, radius: Float): Vector2 {

        if (vector.len2() > radius * radius) {
            vector.nor().scl(radius)
        }

        return vector
    }

    private fun addSimpleItem(itemsAtlas: TextureAtlas, name: String, x: Float, y: Float) {

        if (!isValidPosition(x, y))
            return

        val scale = 0.5f

        engine.entity {
            with<Transform> {
                set(x, y, 0f)
            }
            with<VisualSprite> {
                add(Sprite(itemsAtlas.findRegion(name), scale, scale).setAnchorBottom())
            }
        }
    }

    private fun addTree(itemsAtlas: TextureAtlas, x: Float, y: Float) {

        if (!isValidPosition(x, y))
            return

        val scale = 0.25f

        engine.entity {
            with<Transform> {
                set(x, y, 0f)
            }
            with<VisualSprite> {
                add(Sprite(itemsAtlas.findRegion("tree-trunk"), 2.0f * scale, 2.3f * scale))
                sprites[0].setAnchorBottom()
                sprites[0].offsetY += 0.25f * scale

                add(Sprite(itemsAtlas.findRegion("tree-leaves"), 3f * scale, 2.5f * scale, 0f, 0f))
                sprites[1].setAnchorBottom()
                sprites[1].offsetY -= 1.75f * scale
            }
        }
    }

    private fun isValidPosition(x: Float, y: Float): Boolean {
        return Vector2.dst2(x, y, playerShipSpawnPositionX, playerShipSpawnPositionY) >= 5.5f * 5.5f
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

    private fun buildIslandBodyDefinition(radius: Float): BodyDefinition {
        var islandBodyDefinition = BodyDefinition()

        islandBodyDefinition.type = BodyDef.BodyType.StaticBody
        islandBodyDefinition.circle(radius)

        return islandBodyDefinition
    }
}