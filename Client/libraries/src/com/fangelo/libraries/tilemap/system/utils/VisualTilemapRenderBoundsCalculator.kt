package com.fangelo.libraries.tilemap.system.utils

import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.tilemap.component.Tilemap
import com.fangelo.libraries.transform.Transform

class VisualTilemapRenderBoundsCalculator {

    fun calculate(
        camera: Camera,
        tilemap: Tilemap,
        tilemapTransform: Transform,
        extraTilesToDraw: Int = 2,
        toReturn: VisualTilemapRenderBounds? = null
    ): VisualTilemapRenderBounds {
        return calculate(
            camera,
            tilemapTransform.x, tilemapTransform.y,
            tilemap.width, tilemap.height,
            extraTilesToDraw,
            toReturn
        )
    }

    fun calculate(
        camera: Camera,
        tilemapX: Float,
        tilemapY: Float,
        tilemapWidth: Int,
        tilemapHeight: Int,
        extraTilesToDraw: Int = 2,
        toReturn: VisualTilemapRenderBounds? = null
    ): VisualTilemapRenderBounds {

        var cameraBoundingBox = camera.worldBoundingBox()

        val viewPortWidth = cameraBoundingBox.width + extraTilesToDraw * 2
        val viewPortHeight = cameraBoundingBox.height + extraTilesToDraw * 2
        val cameraPositionX = camera.x - extraTilesToDraw
        val cameraPositionY = camera.y + extraTilesToDraw

        val offsetX = (tilemapX - tilemapWidth * 0.5).toInt()
        val offsetY = (tilemapY - tilemapHeight * 0.5).toInt()

        val offsetXscreen = offsetX.toDouble()
        val offsetYscreen = offsetY.toDouble()

        val viewBoundsX = cameraPositionX - offsetXscreen - viewPortWidth / 2
        val viewBoundsY = cameraPositionY - offsetYscreen - viewPortHeight / 2

        val fromX = Math.max(0, viewBoundsX.toInt())
        val toX = Math.min(tilemapWidth, (viewBoundsX + viewPortWidth + 1f).toInt())

        val fromY = Math.max(0, viewBoundsY.toInt())
        val toY = Math.min(tilemapHeight, (viewBoundsY + viewPortHeight + 1f).toInt())

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