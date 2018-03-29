package com.fangelo.libraries.ashley.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

class Camera : Component {

    var followTransform: Transform? = null

    private var cameraChanged: Boolean = true

    private var camera = OrthographicCamera()

    val native: OrthographicCamera
        get() = this.camera

    var zoom: Float
        get() = camera.zoom
        set(value) {
            camera.zoom = value
            cameraChanged = true
        }

    init {
        camera = OrthographicCamera()
        camera.setToOrtho(true)
        camera.zoom = 1f
        camera.position.set(0f, 0f, 0f)
    }

    internal fun update(x: Float, y: Float) {
        if (x != camera.position.x) {
            camera.position.x = x
            cameraChanged = true
        }
        if (y != camera.position.y) {
            camera.position.y = y
            cameraChanged = true
        }
    }

    fun resize(width: Int, height: Int) {
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.update()
    }

    val combined: Matrix4
        get() {
            if (cameraChanged) {
                cameraChanged = false
                camera.update()
            }
            return camera.combined
        }

    val viewportWidth: Float
        get() = camera.viewportWidth

    val viewportHeight: Float
        get() = camera.viewportHeight

    fun screenPositionToWorldPosition(x: Float, y: Float) : Vector2 {
        val vec = Vector3(x, y, 0f)
        native.unproject(vec)
        return Vector2(vec.x, vec.y)
    }

    fun worldPositionToScreenPosition(x: Float, y: Float) : Vector2 {
        val vec = Vector3(x, y, 0f)
        native.project(vec)
        return Vector2(vec.x, vec.y)
    }
}