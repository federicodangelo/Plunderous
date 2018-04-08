package com.fangelo.plunderous.client.game.generator.generators

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.RandomXS128
import com.badlogic.gdx.math.Vector2
import com.fangelo.libraries.physics.component.World
import com.fangelo.libraries.sprite.Sprite
import com.fangelo.libraries.sprite.component.VisualSprite
import com.fangelo.libraries.transform.Transform
import com.fangelo.plunderous.client.game.generator.component.Generator
import com.fangelo.plunderous.client.game.generator.utils.RandomUtils
import com.fangelo.plunderous.client.game.island.factory.IslandFactory
import ktx.ashley.entity

class IslandsGeneratorFactory(private val engine: Engine, private val mainWorld: World, assetManager: AssetManager) {

    class GeneratedIsland {
        val entities = mutableListOf<Entity>()
    }

    private val random = RandomXS128()
    private val randomUtils = RandomUtils(random)
    private val islandFactory: IslandFactory
    private val itemsAtlas: TextureAtlas

    init {
        islandFactory = IslandFactory(engine)
        itemsAtlas = assetManager.get<TextureAtlas>("items/items.atlas")
    }

    fun buildComponent(): Generator {

        val generator = Generator()

        generator.areaWidth = 64
        generator.areaHeight = 64
        generator.generateContent = { fromX, fromY, toX, toY -> buildIslands(fromX, fromY, toX, toY) }
        generator.destroyContent = { islands -> destroyIslands(islands) }

        return generator

    }

    private fun buildIslands(fromX: Int, fromY: Int, toX: Int, toY: Int): Any {
        val islands = mutableListOf<GeneratedIsland>()

        initRandom(fromX, fromY, toX, toY)

        getRandomPositions(fromX, fromY, toX, toY, 10)
            .filter { isValidPosition(it.x, it.y) }
            .forEach { pos ->
                val radius = randomUtils.random(2.0f, 5.5f)
                if (isIslandRadiusInsideBounds(pos, radius, fromX, fromY, toX, toY)) {
                    islands.add(addIsland(pos.x, pos.y, radius))
                }
            }

        return islands
    }

    private fun isIslandRadiusInsideBounds(pos: Vector2, radius: Float, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        return pos.x - radius >= fromX &&
                pos.y - radius >= fromY &&
                pos.x + radius <= toX &&
                pos.y + radius <= toY
    }

    private fun initRandom(fromX: Int, fromY: Int, toX: Int, toY: Int) {
        random.setSeed(
            fromX * (1L shl 0) +
                    fromY * (1L shl 16) +
                    toX * (1L shl 32) +
                    toY * (1L shl 48)
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun destroyIslands(content: Any) {
        val islands = content as MutableList<GeneratedIsland>

        for (island in islands) {
            for (entity in island.entities) {
                engine.removeEntity(entity)
            }
        }
    }

    private fun addIsland(x: Float, y: Float, radius: Float): GeneratedIsland {

        val generatedIsland = GeneratedIsland()

        val island = islandFactory.buildIsland(x, y, radius, mainWorld)

        generatedIsland.entities.add(island)

        generatedIsland.entities.addAll(addIslandItems(radius, x, y))

        return generatedIsland
    }

    private fun addIslandItems(radius: Float, x: Float, y: Float): List<Entity> {
        val entities = mutableListOf<Entity>()

        getRandomPositionsInsideCircle(x, y, radius - 1.0f, 5)
            .filter { isValidPosition(it.x, it.y) }
            .forEach { pos -> entities.add(addRandomItem(pos)) }

        return entities
    }

    private fun addRandomItem(pos: Vector2): Entity {
        return when (randomUtils.random(0, 2)) {
            0 -> addSimpleItem("rock1", pos.x, pos.y)
            1 -> addSimpleItem("rock2", pos.x, pos.y)
            2 -> addTree(pos.x, pos.y)
            else -> addTree(pos.x, pos.y)
        }
    }

    private fun getRandomPositions(fromX: Int, fromY: Int, toX: Int, toY: Int, amount: Int = 25): Array<Vector2> {
        return Array(amount, { _ ->
            Vector2(
                randomUtils.random(fromX.toFloat(), toX.toFloat()),
                randomUtils.random(fromY.toFloat(), toY.toFloat())
            )
        })
    }

    private fun getRandomPositionsInsideCircle(x: Float, y: Float, radius: Float, amount: Int = 25): Array<Vector2> {
        return Array(amount, { _ ->
            Vector2(randomUtils.random(-radius, radius), randomUtils.random(-radius, radius)).clamp(0f, radius).add(x, y)
        })
    }

    private fun addSimpleItem(name: String, x: Float, y: Float): Entity {

        val scale = 0.5f

        return engine.entity {
            with<Transform> {
                set(x, y, 0f)
            }
            with<VisualSprite> {
                add(getOrBuildSprite(name, { Sprite(itemsAtlas.findRegion(name), scale, scale).setAnchorBottom() }))
            }
        }
    }

    private var cachedSprites = hashMapOf<String, Sprite>()

    private inline fun getOrBuildSprite(id: String, crossinline buildSprite: () -> Sprite): Sprite {
        var sprite = cachedSprites[id]

        if (sprite == null) {
            sprite = buildSprite()
            cachedSprites[id] = sprite
        }

        return sprite
    }


    private fun addTree(x: Float, y: Float): Entity {

        val scale = 0.25f

        return engine.entity {
            with<Transform> {
                set(x, y, 0f)
            }
            with<VisualSprite> {
                add(getOrBuildSprite("tree-trunk", { Sprite(itemsAtlas.findRegion("tree-trunk"), 2.0f * scale, 2.3f * scale) }))
                sprites[0].setAnchorBottom()
                sprites[0].offsetY += 0.25f * scale

                add(getOrBuildSprite("tree-leaves", { Sprite(itemsAtlas.findRegion("tree-leaves"), 3f * scale, 2.5f * scale, 0f, 0f) }))
                sprites[1].setAnchorBottom()
                sprites[1].offsetY -= 1.75f * scale
            }
        }
    }

    private fun isValidPosition(x: Float, y: Float): Boolean {
        return Vector2.dst2(x, y, 0f, 0f) >= 5.5f * 5.5f
    }
}