package com.fangelo.libraries.light

import box2dLight.PointLight
import com.badlogic.gdx.graphics.Color
import com.fangelo.libraries.physics.World
import com.fangelo.libraries.render.VisualComponent

class Light(val rays: Int = 256) : VisualComponent() {

    var world: World? = null
    internal var native: PointLight? = null

    fun set(world: World, distance: Float, color: Color) {
        this.world = world
        this.distance = distance
        this.color = color
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
}