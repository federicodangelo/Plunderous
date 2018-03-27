package com.fangelo.libraries.ashley.components

import com.badlogic.ashley.core.Component

class Transform(var x: Float = 0f, var y: Float = 0f, var rot: Float = 0f) : Component {
    fun set(x: Float, y: Float, rot: Float): Transform {
        this.x = x
        this.y = y
        this.rot = rot
        return this
    }
}