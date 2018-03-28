package com.fangelo.plunderous.client.game.world

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.fangelo.libraries.ashley.components.*
import com.fangelo.libraries.ashley.data.Sprite
import com.fangelo.libraries.ashley.systems.PhysicsSystem
import com.fangelo.plunderous.client.game.components.ship.MainShip
import com.fangelo.plunderous.client.game.components.ship.Ship
import ktx.ashley.entity
import ktx.box2d.body
import ktx.collections.toGdxArray
import java.util.*

class WorldBuilder {

    private val MapWidth = 64
    private val MapHeight = 64

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
        addItems()
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
        val rnd = Random()
        val tileset = buildTileset(tilesetAtlas)
        val width = MapWidth
        val height = MapHeight
        val tiles = Array(width * height, { rnd.nextInt(tileset.size) })

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
        val rnd = Random()
        return Array(amount, { _ -> Vector2(rnd.nextFloat() * (MapWidth - 4) + 2f, rnd.nextFloat() * (MapHeight - 4) + 2f) })
    }

    private fun addSimpleItem(itemsAtlas: TextureAtlas, name: String, x: Float, y: Float) {

        if (!isValidPosition(x, y))
            return

        engine.entity {
            with<Transform> {
                set(x, y, 0f)
            }
            with<VisualSprite> {
                add(Sprite(itemsAtlas.findRegion(name)).setAnchorBottom())
            }
        }
    }

    private fun addTree(itemsAtlas: TextureAtlas, x: Float, y: Float) {

        if (!isValidPosition(x, y))
            return

        engine.entity {
            with<Transform> {
                set(x, y, 0f)
            }
            with<VisualSprite> {
                add(Sprite(itemsAtlas.findRegion("tree-trunk"), 2.0f, 2.3f))
                sprites[0].setAnchorBottom()
                sprites[0].offsetY += 0.25f

                add(Sprite(itemsAtlas.findRegion("tree-leaves"), 3f, 2.5f, 0f, 0f))
                sprites[1].setAnchorBottom()
                sprites[1].offsetY -= 1.75f
            }
        }
    }

    private fun isValidPosition(x: Float, y: Float): Boolean {

        return x >= PlayerSpawnPositionX + 1.5f || x <= PlayerSpawnPositionX - 1.5f ||
                y >= PlayerSpawnPositionY + 1.5f || y <= PlayerSpawnPositionY - 1.5f

    }


    private fun addPlayer() {

        val world = engine.getSystem(PhysicsSystem::class.java).world;
        val playersAtlas = assetManager.get<TextureAtlas>("ships/ships.atlas")
        val playerRegion = playersAtlas.findRegion("ship")
        //val playerAnimations = buildPlayerAnimations("player", playersAtlas)

        val shipBack = -1.25f
        val shipBackHalfWidth = 0.4f

        val shipMiddle = 0.5f
        val shipMiddleHalfWidth = 0.6f

        val shipFront = 1.45f

        this.player = engine.entity {
            with<Transform> {
                set(PlayerSpawnPositionX, PlayerSpawnPositionY, 0f)
            }
            with<Rigidbody> {
                set(
                    world.body(BodyDef.BodyType.DynamicBody) {
                        position.set(PlayerSpawnPositionX, PlayerSpawnPositionY)
                        polygon(
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
                        linearDamping = 0.5f
                        angularDamping = 0.5f
                    }
                )
            }
            with<VisualSprite> {
                add(Sprite(playerRegion, 2f, 3f, 0f, 0f))
            }
            //with<VisualAnimation> {
            //    set(playerAnimations, "walk-east")
            //}
            with<Ship>()
            with<MainShip>()
        }
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
}