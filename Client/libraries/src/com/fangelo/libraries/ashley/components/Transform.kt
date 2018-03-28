package com.fangelo.libraries.ashley.components

import com.badlogic.ashley.core.Component

class Transform(var x: Float = 0f, var y: Float = 0f, var rotation: Float = 0f) : Component {
    fun set(x: Float, y: Float, rotation: Float): Transform {
        this.x = x
        this.y = y
        this.rotation = rotation
        return this
    }
}