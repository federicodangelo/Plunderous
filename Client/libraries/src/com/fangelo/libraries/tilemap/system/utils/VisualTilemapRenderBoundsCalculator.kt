package com.fangelo.libraries.tilemap.system.utils

import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.transform.Transform
import com.fangelo.libraries.tilemap.component.Tilemap

class VisualTilemapRenderBoundsCalculator {

    private val extraTilesToDraw = 2

    fun calculate(
        camera: Camera,
        tilemap: Tilemap,
        tilemapTransform: Transform,
        toReturn: VisualTilemapRenderBounds? = null
    ): VisualTilemapRenderBounds {

        var cameraBoundingBox = camera.worldBoundingBox()

        val viewPortWidth = cameraBoundingBox.width + extraTilesToDraw * 2
        val viewPortHeight = cameraBoundingBox.height + extraTilesToDraw * 2
        val cameraPositionX = camera.x - extraTilesToDraw
        val cameraPositionY = camera.y + extraTilesToDraw

        val offsetX = tilemapTransform.x.toInt()
        val offsetY = tilemapTransform.y.toInt()

        val offsetXscreen = offsetX.toFloat()
        val offsetYscreen = offsetY.toFloat()

        val width = tilemap.width
        val height = tilemap.height

        val viewBoundsX = cameraPositionX - offsetXscreen - viewPortWidth / 2
        val viewBoundsY = cameraPositionY - offsetYscreen - viewPortHeight / 2
        val viewBoundsWidth = viewPortWidth
        val viewBoundsHeight = viewPortHeight

        val fromX = Math.max(0, viewBoundsX.toInt())
        val toX = Math.min(width, (viewBoundsX + viewBoundsWidth + 1f).toInt())

        val fromY = Math.max(0, viewBoundsY.toInt())
        val toY = Math.min(height, (viewBoundsY + viewBoundsHeight + 1f).toInt())

        val renderOffsetX = fromX + offsetX
        val renderOffsetY = fromY + offsetY

        if (toReturn != null) {
            toReturn.fromX = fromX
            toReturn.toX = toX
            toReturn.fromY = fromY
            toReturn.toY = toY
            toReturn.renderOffsetX = renderOffsetX
            toReturn.renderOffsetY = renderOffsetY
            return toReturn
        }

        return VisualTilemapRenderBounds(fromX, toX, fromY, toY, renderOffsetX, renderOffsetY)
    }
}