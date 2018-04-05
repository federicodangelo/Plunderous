package com.fangelo.plunderous.client.game.generator.utils

import com.badlogic.gdx.math.Rectangle

object RectangleUtils {
    fun mergeRectangles(rectangle: Rectangle, toMerge: Rectangle): Rectangle {
        if (rectangle.area() == 0f) {
            if (toMerge.area() > 0f)
                rectangle.set(toMerge)
        } else if (toMerge.area() > 0f) {
            rectangle.merge(toMerge)
        }
        return rectangle
    }
}