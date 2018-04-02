package com.fangelo.plunderous.client.game.island.component

import com.badlogic.gdx.graphics.Color
import com.fangelo.libraries.render.component.VisualComponent

class VisualIsland(var color: Color = Color.RED) : VisualComponent() {
    fun set(color: Color) {
        this.color = color
    }
}
