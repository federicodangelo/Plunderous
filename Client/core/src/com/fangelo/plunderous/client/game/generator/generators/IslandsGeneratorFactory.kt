package com.fangelo.plunderous.client.game.generator.generators

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.RandomXS128
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.fangelo.libraries.physics.component.Rigidbody
import com.fangelo.libraries.physics.component.World
import com.fangelo.libraries.sprite.Sprite
import com.fangelo.libraries.sprite.component.VisualSprite
import com.fangelo.libraries.transform.Transform
import com.fangelo.plunderous.client.game.generator.component.Generator
import com.fangelo.plunderous.client.game.generator.utils.RandomUtils
import com.fangelo.plunderous.client.game.island.component.Island
import com.fangelo.plunderous.client.game.island.component.VisualIsland
import ktx.ashley.entity
import ktx.box2d.BodyDefinition

class IslandsGeneratorFactory(private val engine: Engine, private val mainWorld: World, private val assetManager: AssetManager) {

    class GeneratedIsland {
        val entities = mutableListOf<Entity>()
    }

    private val random = RandomXS128()
    private val randomUtils = RandomUtils(random)

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

        random.setSeed(
            fromX * (1L shl 0) +
                    fromY * (1L shl 16) +
                    toX * (1L shl 32) +
                    toY * (1L shl 48)
        )

        getRandomPositions(fromX, fromY, toX, toY, 10)
            .filter { isValidPosition(it.x, it.y) }
            .forEach { pos ->
                val radius = randomUtils.random(2.0f, 5.5f)
                if (pos.x - radius >= fromX &&
                    pos.y - radius >= fromY &&
                    pos.x + radius <= toX &&
                    pos.y + radius <= toY
                ) {
                    islands.add(addIsland(pos.x, pos.y, radius))
                }
            }

        return islands
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

        val island = engine.entity {
            with<Transform> {
                set(x, y, 0f)
            }
            with<Rigidbody> {
                set(mainWorld, buildIslandBodyDefinition(radius))
            }
            with<Island> {
                set(radius)
            }
            with<VisualIsland> {
                set(Color.CORAL)
            }
        }

        generatedIsland.entities.add(island)

        addIslandItems(radius, x, y, generatedIsland.entities)

        return generatedIsland
    }

    private fun addIslandItems(
        radius: Float,
        x: Float,
        y: Float,
        entities: MutableList<Entity>
    ) {
        val itemsAtlas = assetManager.get<TextureAtlas>("items/items.atlas")

        getRandomPositionsInsideCircle(x, y, radius - 1.0f, 5)
            .filter { isValidPosition(it.x, it.y) }
            .forEach { pos ->
                when (randomUtils.random(0, 2)) {
                    0 -> entities.add(addSimpleItem(itemsAtlas, "rock1", pos.x, pos.y))
                    1 -> entities.add(addSimpleItem(itemsAtlas, "rock2", pos.x, pos.y))
                    2 -> entities.add(addTree(itemsAtlas, pos.x, pos.y))
                }
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

    private fun addSimpleItem(itemsAtlas: TextureAtlas, name: String, x: Float, y: Float): Entity {

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


    private fun addTree(itemsAtlas: TextureAtlas, x: Float, y: Float): Entity {

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


    private fun buildIslandBodyDefinition(radius: Float): BodyDefinition {
        var islandBodyDefinition = BodyDefinition()

        islandBodyDefinition.type = BodyDef.BodyType.StaticBody
        islandBodyDefinition.circle(radius)

        return islandBodyDefinition
    }

}