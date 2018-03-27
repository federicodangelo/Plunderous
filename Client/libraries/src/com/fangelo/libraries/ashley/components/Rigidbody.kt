package com.fangelo.libraries.ashley.components

import com.badlogic.ashley.core.Component

class Rigidbody(var velocityX: Float = 0f, var velocityY: Float = 0f, var velocityRot: Float = 0f) : Component {
    fun set(velocityX: Float, velocityY: Float, velocityRot: Float): Rigidbody {
        this.velocityX = velocityX
        this.velocityY = velocityY
        this.velocityRot = velocityRot
        return this
    }
}