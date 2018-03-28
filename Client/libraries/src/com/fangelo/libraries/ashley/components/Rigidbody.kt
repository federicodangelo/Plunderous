package com.fangelo.libraries.ashley.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.physics.box2d.Body

class Rigidbody(var body: Body? = null) : Component {
    fun set(body: Body) {
        this.body = body
    }

    fun forcePosition(x: Float, y: Float, rotation: Float) {
        val body = this.body ?: return
        body.setTransform(x, y, rotation)
    }
}