package com.fangelo.libraries.tilemap.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.transform.Transform
import com.fangelo.libraries.render.system.VisualCameraRenderer
import com.fangelo.libraries.tilemap.component.Tilemap
import com.fangelo.libraries.tilemap.system.utils.VisualTilemapRenderBounds
import com.fangelo.libraries.tilemap.system.utils.VisualTilemapRenderBoundsCalculator
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class VisualTilemapRenderSystem : VisualCameraRenderer() {

    private lateinit var entities: ImmutableArray<Entity>

    private val transform = mapperFor<Transform>()
    private val tilemap = mapperFor<Tilemap>()

    private lateinit var batch: SpriteBatch
    private val renderBoundsCalculator = VisualTilemapRenderBoundsCalculator()

    override fun addedToEngine(engine: Engine) {
        batch = SpriteBatch()
        entities = engine.getEntitiesFor(allOf(Transform::class, Tilemap::class).get())
    }

    override fun removedFromEngine(engine: Engine) {
        batch.dispose()
    }

    override fun render(camera: Camera) {
        beginRender(camera)

        drawTilemaps(camera)

        endRender()
    }

    private val tmpBounds = VisualTilemapRenderBounds()

    private fun drawTilemaps(camera: Camera) {
        var tilemap: Tilemap
        var tilemapTransform: Transform

        for (e in entities) {

            tilemap = this.tilemap.get(e)
            tilemapTransform = this.transform.get(e)

            if (!camera.shouldRenderVisualComponent(tilemap))
                continue

            val bounds = renderBoundsCalculator.calculate(camera, tilemap, tilemapTransform, tmpBounds)

            drawFloor(bounds, tilemap)
        }
    }

    private fun drawFloor(bounds: VisualTilemapRenderBounds, tilemap: Tilemap) {
        val renderOffsetX = bounds.renderOffsetX.toFloat()
        val tileset = tilemap.tileset ?: return

        var drawY = bounds.renderOffsetY.toFloat()

        batch.disableBlending()
        for (mapX in bounds.fromY until bounds.toY) {
            var drawX = renderOffsetX
            for (mapY in bounds.fromX until bounds.toX) {
                val tile = tilemap.getTile(mapX, mapY)
                val tileTexture = tileset.getTileTexture(tile)
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