package com.fangelo.plunderous.client.game.components.island

import com.badlogic.gdx.graphics.Color
import com.fangelo.libraries.render.VisualComponent

class VisualIsland(var color: Color = Color.RED) : VisualComponent() {
    fun set(color: Color) {
        this.color = color
    }
}
