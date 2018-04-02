package com.fangelo.plunderous.client.game.world

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.fangelo.libraries.light.Light
import com.fangelo.libraries.light.WorldLight
import com.fangelo.libraries.sprite.Sprite
import com.fangelo.libraries.tilemap.Tileset
import com.fangelo.libraries.physics.Rigidbody
import com.fangelo.libraries.physics.World
import com.fangelo.libraries.sprite.VisualSprite
import com.fangelo.libraries.tilemap.Tilemap
import com.fangelo.libraries.transform.Transform
import com.fangelo.plunderous.client.game.components.island.Island
import com.fangelo.plunderous.client.game.components.island.VisualIsland
import com.fangelo.plunderous.client.game.components.ship.MainShip
import com.fangelo.plunderous.client.game.components.ship.Ship
import com.fangelo.plunderous.client.game.components.ship.ShipInput
import ktx.ashley.entity
import ktx.box2d.BodyDefinition

class WorldBuilder {

    private val mapWidth = 128
    private val mapHeight = 128

    private val playerSpawnPositionX = mapWidth / 2.0f
    private val playerSpawnPositionY = mapHeight / 2.0f

    private lateinit var engine: Engine
    private lateinit var assetManager: AssetManager

    var player: Entity? = null
        private set

    var mainWorld: World? = null
        private set

    fun build(engine: Engine, assetManager: AssetManager) {

        this.engine = engine
        this.assetManager = assetManager

        addMainWorld()
        addTilemap()
        addPlayer()
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

    private fun addTilemap() {
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
        return Array(amount, { _ -> Vector2(MathUtils.random() * (mapWidth - 4) + 2f, MathUtils.random() * (mapHeight - 4) + 2f) })
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
        return x >= playerSpawnPositionX + 5.5f || x <= playerSpawnPositionX - 5.5f ||
                y >= playerSpawnPositionY + 5.5f || y <= playerSpawnPositionY - 5.5f
    }


    private fun addPlayer() {

        val playersAtlas = assetManager.get<TextureAtlas>("ships/ships.atlas")
        val playerRegion = playersAtlas.findRegion("ship")

        this.player = engine.entity {
            with<Transform> {
                set(playerSpawnPositionX, playerSpawnPositionY, 0f)
            }
            with<Rigidbody> {
                set(mainWorld!!, buildShipBodyDefinition())
            }
            with<Ship>()
            with<MainShip>()
            with<ShipInput>()
            with<VisualSprite> {
                add(Sprite(playerRegion, 2f, 3f, 0f, 0f))
            }
            with<Light> {
                set(mainWorld!!, 10.0f, Color.WHITE)
            }
        }
    }

    private fun buildShipBodyDefinition(): BodyDefinition {
        val shipBack = -1.25f
        val shipBackHalfWidth = 0.4f

        val shipMiddle = 0.5f
        val shipMiddleHalfWidth = 0.6f

        val shipFront = 1.45f

        val playerBodyDefinition = BodyDefinition()

        playerBodyDefinition.type = BodyDef.BodyType.DynamicBody

        playerBodyDefinition.polygon(
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
        playerBodyDefinition.linearDamping = 0.5f
        playerBodyDefinition.angularDamping = 0.9f
        return playerBodyDefinition
    }

    private fun buildIslandBodyDefinition(radius: Float): BodyDefinition {
        var islandBodyDefinition = BodyDefinition()

        islandBodyDefinition.type = BodyDef.BodyType.StaticBody
        islandBodyDefinition.circle(radius)

        return islandBodyDefinition
    }
}