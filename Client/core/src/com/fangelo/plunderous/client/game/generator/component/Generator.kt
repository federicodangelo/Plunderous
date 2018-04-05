package com.fangelo.plunderous.client.game.generator.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Rectangle
import com.fangelo.plunderous.client.game.generator.utils.RectangleUtils

class GeneratedArea(val fromX: Int, val fromY: Int, val toX: Int, val toY: Int, val content: Any) {
    fun overlaps(fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        return this.fromX < toX &&
                this.toX > fromX &&
                this.fromY < toY &&
                this.toY > fromY
    }
}

class Generator : Component {

    var areaWidth: Int = 16
    var areaHeight: Int = 16

    var generateContent: (fromX: Int, fromY: Int, toX: Int, toY: Int) -> Any = { _, _, _, _ -> "" }
    var destroyContent: (content: Any) -> Unit = { _ -> }

    private val generatedAreas = mutableListOf<GeneratedArea>()
    private val lastArea = Rectangle()

    fun generate(areaSources: List<GeneratorAreaSource>) {

        val finalArea = areaSources
            .map { areaSource -> areaSource.getArea() }
            .reduce({ acc, rect -> RectangleUtils.mergeRectangles(acc, rect) })

        if (finalArea != lastArea) {
            lastArea.set(finalArea)

            val fromX = Math.floor(finalArea.x.toDouble() / areaWidth).toInt() * areaWidth
            val fromY = Math.floor(finalArea.y.toDouble() / areaHeight).toInt() * areaHeight
            val toX = Math.ceil((finalArea.x + finalArea.width).toDouble() / areaWidth).toInt() * areaWidth
            val toY = Math.ceil((finalArea.y + finalArea.height).toDouble() / areaHeight).toInt() * areaHeight

            destroyOldAreas(fromX, fromY, toX, toY)
            generateNewAreas(fromX, fromY, toX, toY)
        }
    }

    private fun destroyOldAreas(fromX: Int, fromY: Int, toX: Int, toY: Int) {
        val destroyContent = destroyContent

        val areasToDestoy = mutableListOf<GeneratedArea>()

        for (area in generatedAreas) {
            if (!area.overlaps(fromX, fromY, toX, toY)) {
                areasToDestoy.add(area)
            }
        }
        areasToDestoy.forEach { area ->
            generatedAreas.remove(area)
            destroyContent(area.content)
        }
    }

    private fun generateNewAreas(fromX: Int, fromY: Int, toX: Int, toY: Int) {
        val generateContent = generateContent

        for (x in fromX until toX step areaWidth) {
            for (y in fromY until toY step areaHeight) {

                if (!isAreaGenerated(x, y)) {
                    val newContent = generateContent(x, y, x + areaWidth, y + areaHeight)
                    generatedAreas.add(GeneratedArea(x, y, x + areaWidth, y + areaHeight, newContent))
                }
            }
        }
    }

    private fun isAreaGenerated(fromX: Int, fromY: Int): Boolean {
        return generatedAreas.find { area -> area.fromX == fromX && area.fromY == fromY } != null
    }
}