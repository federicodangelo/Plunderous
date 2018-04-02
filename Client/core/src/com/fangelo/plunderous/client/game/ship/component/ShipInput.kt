package com.fangelo.plunderous.client.game.ship.component

import com.badlogic.ashley.core.Component

class ShipInput(var left: Boolean = false, var right: Boolean = false, var forward: Boolean = false, var backward: Boolean = false) : Component {
    fun reset() {
        left = false
        right = false
        forward = false
        backward = false
    }
}
