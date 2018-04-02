package com.fangelo.libraries.transform

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

    fun worldPositionToLocalPosition(worldPos: Vector2): Vector2 {
        return worldPositionToLocalPosition(worldPos.x, worldPos.y)
    }

    fun worldPositionToLocalPosition(x: Float, y: Float): Vector2 {
        var vector = Vector2(x, y)
        vector.sub(this.x, this.y)
        vector.rotateRad(-this.rotation)
        vector.x = -vector.x
        return vector
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