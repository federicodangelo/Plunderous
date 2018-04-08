package com.fangelo.libraries.light.component

import box2dLight.PointLight
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Pool
import com.fangelo.libraries.physics.component.World
import com.fangelo.libraries.render.component.VisualComponent
import com.fangelo.libraries.transform.Transform

class Light : VisualComponent(), Pool.Poolable {
    internal var world: World? = null
    internal var native: PointLight? = null

    var rays: Int = 128
        private set

    fun set(world: World, distance: Float, color: Color, rays: Int = 128) {
        this.world = world
        this.distance = distance
        this.color = color
        this.rays = rays
    }

    var color: Color = Color.BLACK
        set(value) {
            field = value
            native?.color = value
        }

    var distance: Float = 1.0f
        set(value) {
            field = value
            native?.distance = value
        }


    internal fun initNative(transform: Transform) {

        val world = this.world

        if (world == null) {
            Gdx.app.error("Light", "Missing world configuration")
            return
        }

        val worldLight = world.worldLight

        if (worldLight == null) {
            Gdx.app.error("Light", "WorldLight not initialized for world")
            return
        }

        if (worldLight.native == null) {
            Gdx.app.error("Light", "WorldLight not initialized for world")
            return
        }

        val native = PointLight(worldLight.native, this.rays, this.color, this.distance, transform.x, transform.y)
        native.ignoreAttachedBody = true
        this.native = native
    }

    internal fun destroyNative() {
        this.native?.remove(true)
        this.native = null
    }

    override fun reset() {
        world = null
        native = null
    }
}