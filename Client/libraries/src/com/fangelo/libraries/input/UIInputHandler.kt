package com.fangelo.libraries.input

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.fangelo.libraries.ui.ScreenManager

class UIInputHandler : InputAdapter() {
    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
            if (ScreenManager.canPop()) {
                ScreenManager.pop()
                return true
            }
        }
        return false
    }
}