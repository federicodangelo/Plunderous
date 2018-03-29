package com.fangelo.plunderous.client.game.components.island

import com.badlogic.ashley.core.Component

class Island(var radius: Float = 1.5f) : Component {
    fun set(radius: Float) {
        this.radius = radius
    }
}