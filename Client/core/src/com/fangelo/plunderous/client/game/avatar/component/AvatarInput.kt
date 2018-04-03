package com.fangelo.plunderous.client.game.avatar.component

import com.badlogic.ashley.core.Component

class AvatarInput(var left: Boolean = false, var right: Boolean = false, var up: Boolean = false, var down: Boolean = false) : Component {
    fun reset() {
        left = false
        right = false
        up = false
        down = false
    }
}