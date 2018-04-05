package com.fangelo.libraries.physics.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.fangelo.libraries.light.component.WorldLight
import com.fangelo.libraries.transform.Transform
import ktx.ashley.mapperFor
import ktx.box2d.body
import ktx.box2d.createWorld

class World : Component, Pool.Poolable {
    var followTransform: Transform? = null

    internal var native: com.badlogic.gdx.physics.box2d.World? = null
    internal var worldLight: WorldLight? = null

    private val maxStepTime = 1 / 45f

    private var accumulator = 0f

    private val transform = mapperFor<Transform>()
    private val rigidbody = mapperFor<Rigidbody>()

    private val bodies = mutableSetOf<Entity>()

    val isEmpty: Boolean
        get() = bodies.isEmpty()

    internal fun initNative() {
        this.native = createWorld(gravity = Vector2())
    }

    internal fun destroyNative() {
        if (!isEmpty)
            Gdx.app.error("World", "Destroying non-empty world, bad things can happen!!")

        this.native?.dispose()
        this.native = null
    }

    internal fun addRigidbody(rigidbody: Rigidbody, transform: Transform, entity: Entity) {
        rigidbody.initNative(transform)
        bodies.add(entity)
    }

    internal fun removeRigidbody(rigidbody: Rigidbody, entity: Entity) {
        if (bodies.remove(entity))
            rigidbody.destroyNative()
    }

    internal fun updatePhysics(deltaTime: Float) {
        val native = this.native

        if (native == null) {
            Gdx.app.error("World", "Native world not initialized")
            return
        }

        val frameTime = Math.min(deltaTime, 0.25f)
        accumulator += frameTime
        if (accumulator >= maxStepTime) {
            accumulator -= maxStepTime
            native.step(maxStepTime, 6, 2)

            updateBodiesTransforms()
        }
    }

    private fun updateBodiesTransforms() {

        val followTransform = followTransform

        for (entity in bodies) {
            val transform = this.transform.get(entity)
            val rigidbody = this.rigidbody.get(entity)
            val bodyNative = rigidbody.native ?: continue

            var position = bodyNative.position
            var rotation = bodyNative.angle

            if (followTransform != null) {
                position = followTransform.localPositionToWorldPosition(position)
                rotation = followTransform.localRotationToWorldRotation(rotation)
            }

            transform.x = position.x
            transform.y = position.y
            transform.rotation = rotation
        }
    }

    fun buildBounds(width: Float, height: Float, thick: Float = 1f) {

        val native = this.native

        if (native == null) {
            Gdx.app.error("World", "Missing native initialization in $this")
            return
        }

        //Left
        native.body {
            box(
                height = height,
                width = thick,
                position = Vector2(-width * 0.5f, 0f)
            )
        }
        //Right
        native.body {
            box(
                height = height,
                width = thick,
                position = Vector2(width * 0.5f, 0f)
            )
        }
        //Top
        native.body {
            box(
                width = width,
                height = thick,
                position = Vector2(0f, -height * 0.5f)
            )
        }
        //Bottom
        native.body {
            box(
                width = width,
                height = thick,
                position = Vector2(0f, height * 0.5f)
            )
        }
    }

    override fun reset() {
        followTransform = null
        native = null
        worldLight = null
    }
}