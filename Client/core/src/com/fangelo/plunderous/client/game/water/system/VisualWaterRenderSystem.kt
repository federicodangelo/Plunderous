package com.fangelo.libraries.tilemap.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.render.system.VisualCameraRenderer
import com.fangelo.libraries.tilemap.system.utils.VisualTilemapRenderBounds
import com.fangelo.libraries.tilemap.system.utils.VisualTilemapRenderBoundsCalculator
import com.fangelo.plunderous.client.game.water.component.VisualWater
import com.fangelo.plunderous.client.game.water.component.Water
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class VisualWaterRenderSystem : VisualCameraRenderer() {

    private lateinit var entities: ImmutableArray<Entity>

    private val visualWater = mapperFor<VisualWater>()
    private val water = mapperFor<Water>()

    private lateinit var batch: SpriteBatch
    private val renderBoundsCalculator = VisualTilemapRenderBoundsCalculator()

    override fun addedToEngine(engine: Engine) {
        batch = SpriteBatch()
        entities = engine.getEntitiesFor(allOf(Water::class, VisualWater::class).get())
    }

    override fun removedFromEngine(engine: Engine) {
        batch.dispose()
    }

    override fun render(camera: Camera) {
        beginRender(camera)

        drawWaters(camera)

        endRender()
    }

    private val tmpBounds = VisualTilemapRenderBounds()

    private fun drawWaters(camera: Camera) {
        var water: Water
        var visualWater: VisualWater

        for (e in entities) {

            visualWater = this.visualWater.get(e)
            water = this.water.get(e)

            if (!camera.shouldRenderVisualComponent(visualWater))
                continue

            val bounds = renderBoundsCalculator.calculate(camera, 0f, 0f, Int.MAX_VALUE, Int.MAX_VALUE, 2, tmpBounds)

            drawWater(water, visualWater, bounds)
        }
    }

    private fun drawWater(water: Water, visualWater: VisualWater, bounds: VisualTilemapRenderBounds) {
        val renderOffsetX = bounds.renderOffsetX.toFloat()
        val tileTexture = visualWater.waterTileTexture ?: return

        var drawY = bounds.renderOffsetY.toFloat()

        batch.disableBlending()
        for (mapX in bounds.fromY until bounds.toY) {
            var drawX = renderOffsetX
            for (mapY in bounds.fromX until bounds.toX) {
                batch.draw(tileTexture, drawX, drawY, 1f, 1f)
                drawX++
            }
            drawY++
        }
    }

    private fun beginRender(camera: Camera) {
        batch.projectionMatrix = camera.combined
        batch.begin()
    }

    private fun endRender() {
        batch.end()
    }
}