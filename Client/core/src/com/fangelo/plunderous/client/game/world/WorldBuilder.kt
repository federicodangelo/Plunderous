package com.fangelo.plunderous.client.game.world

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.fangelo.libraries.ashley.components.*
import com.fangelo.libraries.ashley.data.Sprite
import com.fangelo.libraries.ashley.systems.PhysicsSystem
import com.fangelo.plunderous.client.game.components.island.Island
import com.fangelo.plunderous.client.game.components.island.VisualIsland
import com.fangelo.plunderous.client.game.components.ship.MainShip
import com.fangelo.plunderous.client.game.components.ship.Ship
import ktx.ashley.entity
import ktx.box2d.BodyDefinition
import ktx.box2d.body

class WorldBuilder {

    private val MapWidth = 128
    private val MapHeight = 128

    private val PlayerSpawnPositionX = MapWidth / 2.0f
    private val PlayerSpawnPositionY = MapHeight / 2.0f

    private lateinit var engine: Engine
    private lateinit var assetManager: AssetManager

    var player: Entity? = null
        private set

    fun build(engine: Engine, assetManager: AssetManager) {

        this.engine = engine
        this.assetManager = assetManager

        addWorldBounds()
        addTilemap()
        addPlayer()
        addIslands()
        addItems()
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
                set(buildIslandBodyDefinition(radius))
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

    private fun addWorldBounds() {
        val world = engine.getSystem(PhysicsSystem::class.java).world

        //Left
        world.body {
            box(
                height = MapHeight.toFloat(),
                position = Vector2(0f, MapHeight * 0.5f)
            )
        }
        //Right
        world.body {
            box(
                height = MapHeight.toFloat(),
                position = Vector2(MapWidth.toFloat(), MapHeight * 0.5f)
            )
        }
        //Top
        world.body {
            box(
                width = MapWidth.toFloat(),
                position = Vector2(MapWidth * 0.5f, 0f)
            )
        }
        //Bottom
        world.body {
            box(
                width = MapWidth.toFloat(),
                position = Vector2(MapWidth * 0.5f, MapHeight.toFloat())
            )
        }
    }


    private fun addTilemap() {
        val tilesetAtlas = assetManager.get<TextureAtlas>("tiles/tiles.atlas")
        val tileset = buildTileset(tilesetAtlas)
        val width = MapWidth
        val height = MapHeight
        val tiles = Array(width * height, { MathUtils.random(tileset.size - 1) })

        engine.entity {
            with<Transform>()
            with<Tilemap> {
                set(width, height, tiles)
            }
            with<VisualTileset> {
                set(tileset)
            }
        }
    }

    private fun buildTileset(tilesetAtlas: TextureAtlas): Array<TextureRegion> {

        return arrayOf(
            tilesetAtlas.findRegion("water")
        )
    }


    private fun addItems() {
        /*
        val itemsAtlas = assetManager.get<TextureAtlas>("items/items.atlas")

        addSimpleItem(itemsAtlas, "rock1", 14.5f, 16f)

        addSimpleItem(itemsAtlas, "rock2", 18.5f, 16f)

        addTree(itemsAtlas, 20.5f, 16f)

        getRandomPositions().forEach { pos -> addTree(itemsAtlas, pos.x, pos.y) }
        getRandomPositions().forEach { pos -> addSimpleItem(itemsAtlas, "rock1", pos.x, pos.y) }
        getRandomPositions().forEach { pos -> addSimpleItem(itemsAtlas, "rock2", pos.x, pos.y) }
        */
    }

    private fun getRandomPositions(amount: Int = 25): Array<Vector2> {
        return Array(amount, { _ -> Vector2(MathUtils.random() * (MapWidth - 4) + 2f, MathUtils.random() * (MapHeight - 4) + 2f) })
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
        return x >= PlayerSpawnPositionX + 5.5f || x <= PlayerSpawnPositionX - 5.5f ||
                y >= PlayerSpawnPositionY + 5.5f || y <= PlayerSpawnPositionY - 5.5f
    }


    private fun addPlayer() {

        val playersAtlas = assetManager.get<TextureAtlas>("ships/ships.atlas")
        val playerRegion = playersAtlas.findRegion("ship")

        this.player = engine.entity {
            with<Transform> {
                set(PlayerSpawnPositionX, PlayerSpawnPositionY, 0f)
            }
            with<Rigidbody> {
                set(buildShipBodyDefinition())
            }
            with<VisualSprite> {
                add(Sprite(playerRegion, 2f, 3f, 0f, 0f))
            }
            with<Ship>()
            with<MainShip>()
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
        playerBodyDefinition.angularDamping = 0.5f
        return playerBodyDefinition
    }

    private fun buildIslandBodyDefinition(radius: Float): BodyDefinition {
        var islandBodyDefinition = BodyDefinition()

        islandBodyDefinition.type = BodyDef.BodyType.StaticBody
        islandBodyDefinition.circle(radius)

        return islandBodyDefinition
    }
}