package com.fangelo.libraries.camera.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.utils.Pool
import com.fangelo.libraries.render.component.VisualComponent
import com.fangelo.libraries.transform.Transform

class Camera : Component, Pool.Poolable {
    var id = ""
    var enabled = true
    var renderMask: Int = Int.MAX_VALUE

    var followTransform: Transform? = null
    var followTransformOffset = Vector2.Zero
    var followTransformRotation = true
    var followTransformRotationOffset = 0f

    private var cameraChanged: Boolean = true

    private var camera = OrthographicCamera()

    internal val native: OrthographicCamera
        get() = this.camera

    val x: Float
        get() = camera.position.x

    val y: Float
        get() = camera.position.y

    var rotation: Float = 0f
        private set

    var zoom: Float
        get() = camera.zoom
        set(value) {
            camera.zoom = value
            cameraChanged = true
        }

    val viewportWidth: Float
        get() = camera.viewportWidth

    val viewportHeight: Float
        get() = camera.viewportHeight

    val up: Vector2
        get() = Vector2(camera.up.x, camera.up.y)

    val right: Vector2
        get() = up.rotate90(-1)

    val combined: Matrix4
        get() {
            if (cameraChanged) {
                cameraChanged = false
                camera.update()
            }
            return camera.combined
        }

    init {
        camera = OrthographicCamera()
        camera.setToOrtho(true)
        camera.zoom = 1f
        camera.position.set(0f, 0f, 0f)
    }

    internal fun update(x: Float, y: Float, rotation: Float) {

        if (x != camera.position.x) {
            camera.position.x = x
            cameraChanged = true
        }
        if (y != camera.position.y) {
            camera.position.y = y
            cameraChanged = true
        }

        if (rotation != this.rotation) {
            this.rotation = rotation
            camera.up.x = MathUtils.sin(rotation)
            camera.up.y = -MathUtils.cos(rotation)

            cameraChanged = true
        }
    }

    fun resize(width: Int, height: Int) {
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.update()
    }

    fun screenPositionToWorldPosition(x: Float, y: Float): Vector2 {
        val vec = Vector3(x, y, 0f)
        native.unproject(vec)
        return Vector2(vec.x, vec.y)
    }

    fun worldPositionToScreenPosition(x: Float, y: Float): Vector2 {
        val vec = Vector3(x, y, 0f)
        native.project(vec)
        return Vector2(vec.x, vec.y)
    }

    fun worldBoundingBox(): BoundingBox {
        var cameraBoundingBox = BoundingBox(
            Vector3(-viewportWidth * zoom * 0.5f, -viewportHeight * zoom * 0.5f, 0f).add(camera.position),
            Vector3(viewportWidth * zoom * 0.5f, viewportHeight * zoom * 0.5f, 0f).add(camera.position)
        )

        if (rotation != 0f)
            cameraBoundingBox.mul(Matrix4().rotateRad(0f, 0f, -1f, rotation))

        return cameraBoundingBox
    }

    fun shouldRenderVisualComponent(v: VisualComponent): Boolean {
        return (renderMask and v.renderFlags) != 0
    }

    override fun reset() {
        followTransform = null
    }
}