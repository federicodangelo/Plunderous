package com.fangelo.plunderous.client.game.components.island

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color

class VisualIsland(var color:Color = Color.RED) : Component {
    fun set(color:Color) {
        this.color = color
    }
}
