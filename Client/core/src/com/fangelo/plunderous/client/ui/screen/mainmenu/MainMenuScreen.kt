package com.fangelo.plunderous.client.ui.screen.mainmenu

import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.fangelo.libraries.debug.DebugSettings
import com.fangelo.plunderous.client.ui.dialog.loadinggame.LoadingGameDialog
import com.fangelo.plunderous.client.ui.dialog.test.TestDialog
import com.fangelo.libraries.ui.Screen
import com.fangelo.libraries.ui.ScreenManager
import com.fangelo.plunderous.client.ui.screen.settings.SettingsScreen
import com.fangelo.plunderous.client.ui.screen.about.AboutScreen
import ktx.actors.onChange

class MainMenuScreen : Screen() {

    object Static {
        var firstTime = true
    }

    init {
        setBackground("panel-brown")

        addTitle("Plunderous!!")

        addTextButton("Play", { loadGame() })
        addTextButton("About..", { showAboutScreen() })
        addTextButton("Settings", { showSettingsScreen() })
        addTextButton("Test Dialog", { showTestDialog() })

        if (Static.firstTime) {
            Static.firstTime = false
            DebugSettings.showFps = true
            loadGame()
        }
    }

    private fun loadGame() {
        ScreenManager.show(LoadingGameDialog())
    }

    private fun showTestDialog() {
        ScreenManager.show(TestDialog())
    }

    private fun showSettingsScreen() {
        ScreenManager.push(SettingsScreen())
    }

    private fun showAboutScreen() {
        ScreenManager.push(AboutScreen())
    }

    private fun addTitle(title: String) {
        add(title).padBottom(20f)
        row()
    }

    private fun addTextButton(text: String, listener: (() -> Unit)) {
        val button = TextButton(text, skin)

        button.onChange(listener)

        add(button).minWidth(150f).pad(10f)
        row()
    }
}
