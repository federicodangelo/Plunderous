package com.fangelo.plunderous.client.game.island.component

import com.badlogic.ashley.core.Component

class Island(var radius: Float = 1.5f) : Component {
    fun set(radius: Float) {
        this.radius = radius
    }
}