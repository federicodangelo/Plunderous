package com.fangelo.plunderous.client.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.fangelo.libraries.ui.DialogResult
import com.fangelo.libraries.ui.ScreenManager
import com.fangelo.plunderous.client.ui.dialog.ConfirmDialog

class ExitInputHandler : InputAdapter() {
    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
            ScreenManager.show(ConfirmDialog("Exit", "Exit game?")).onClosed += { res ->
                if (res == DialogResult.Yes) {
                    Gdx.app.exit()
                }
            }
            return true
        }
        return false
    }

}