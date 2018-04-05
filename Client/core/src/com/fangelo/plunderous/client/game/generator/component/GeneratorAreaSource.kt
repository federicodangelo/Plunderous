package com.fangelo.plunderous.client.game.generator.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Rectangle
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.transform.Transform
import com.fangelo.plunderous.client.game.generator.utils.RectangleUtils

class GeneratorAreaSource : Component {

    var followCamera: Camera? = null

    var followTransform: Transform? = null
    var followTransformRadius: Float = 1f

    fun getArea(): Rectangle {

        val followCamera = followCamera

        val rectangle = Rectangle()

        if (followCamera != null) {
            val cameraBoundingBox = followCamera.worldBoundingBox()

            RectangleUtils.mergeRectangles(
                rectangle, Rectangle(
                    cameraBoundingBox.min.x, cameraBoundingBox.min.y,
                    cameraBoundingBox.width, cameraBoundingBox.height
                )
            )
        }

        val followTransform = followTransform

        if (followTransform != null) {

            val x = followTransform.x
            val y = followTransform.y

            RectangleUtils.mergeRectangles(
                rectangle, Rectangle(
                    x - followTransformRadius, y - followTransformRadius,
                    followTransformRadius, followTransformRadius
                )
            )
        }

        return rectangle
    }
}