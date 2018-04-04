package com.fangelo.plunderous.client.game.ship.component

import com.badlogic.ashley.core.Component

class ShipInput(var targetSpeed: Float = 0f, var targetRudderRotation: Float = 0f) : Component {
    fun reset() {
        targetSpeed = 0f
        targetRudderRotation = 0f
    }
}
