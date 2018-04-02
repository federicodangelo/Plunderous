package com.fangelo.libraries.input

import com.badlogic.ashley.core.EntitySystem
import com.fangelo.libraries.input.InputInfo

class InputSystem : EntitySystem() {
    override fun update(deltaTime: Float) {
        InputInfo.update(deltaTime)
    }
}