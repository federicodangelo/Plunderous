package com.fangelo.libraries.ashley.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

class Transform(var x: Float = 0f, var y: Float = 0f, var rotation: Float = 0f) : Component {
    fun set(x: Float, y: Float, rotation: Float): Transform {
        this.x = x
        this.y = y
        this.rotation = rotation
        return this
    }

    val forward: Vector2
        get() = Vector2(-MathUtils.sin(rotation), MathUtils.cos(rotation))

    val backward: Vector2
        get() = Vector2(MathUtils.sin(rotation), -MathUtils.cos(rotation))

    val right: Vector2
        get() = Vector2(-MathUtils.cos(rotation), -MathUtils.sin(rotation))

    val left: Vector2
        get() = Vector2(MathUtils.cos(rotation), MathUtils.sin(rotation))
}