package com.fangelo.plunderous.client.ui.screen.ingame

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.fangelo.libraries.ui.Screen
import com.fangelo.libraries.ui.ScreenManager
import com.fangelo.plunderous.client.Context
import com.fangelo.plunderous.client.game.ship.system.ShipInputProvider
import com.fangelo.plunderous.client.ui.screen.mainmenu.MainMenuScreen
import com.fangelo.plunderous.client.ui.screen.settings.SettingsScreen
import ktx.actors.onChange

class InGameScreen : Screen(), ShipInputProvider {

    private val topLeftContainer: Table
    private val middleRightContainer: Table
    private val middleLeftContainer: Table
    private val topRightContainer: Table
    private val bottomRightContainer: Table
    private val bottomCenterContainer: Table
    private val bottomLeftContainer: Table

    private val container: WidgetGroup
    private val playerControls: PlayerControls

    init {

        container = WidgetGroup()
        add(container).fill()

        topLeftContainer = Table()
        middleRightContainer = Table()
        middleLeftContainer = Table()
        topRightContainer = Table()
        bottomRightContainer = Table()
        bottomCenterContainer = Table()
        bottomLeftContainer = Table()

        container.addActor(topLeftContainer)
        container.addActor(middleRightContainer)
        container.addActor(middleLeftContainer)
        container.addActor(topRightContainer)
        container.addActor(bottomLeftContainer)
        container.addActor(bottomCenterContainer)
        container.addActor(bottomRightContainer)

        addSettingsButtons()

        addExitButton()

        playerControls = addShipControls()
    }

    private fun addShipControls(): PlayerControls {
        return PlayerControls(skin, bottomCenterContainer, middleRightContainer, middleLeftContainer)
    }

    override fun onLayout() {

        container.width = width
        container.height = height

        topLeftContainer.setPosition(
            (-container.width / 2 + topLeftContainer.prefWidth / 2).toInt().toFloat(),
            (container.height / 2 - topLeftContainer.prefHeight / 2).toInt().toFloat()
        )

        middleRightContainer.setPosition(
            (container.width / 2 - middleRightContainer.prefWidth / 2).toInt().toFloat(),
            0f
        )

        middleLeftContainer.setPosition(
            (-container.width / 2 + middleLeftContainer.prefWidth / 2).toInt().toFloat(),
            0f
        )

        topRightContainer.setPosition(
            (container.width / 2 - topRightContainer.prefWidth / 2).toInt().toFloat(),
            (container.height / 2 - topRightContainer.prefHeight / 2).toInt().toFloat()
        )

        bottomLeftContainer.setPosition(
            (-container.width / 2 + bottomLeftContainer.prefWidth / 2).toInt().toFloat(),
            (-container.height / 2 + bottomLeftContainer.prefHeight / 2).toInt().toFloat()
        )

        bottomCenterContainer.setPosition(
            0f,
            (-container.height / 2 + bottomCenterContainer.prefHeight / 2).toInt().toFloat()
        )

        bottomRightContainer.setPosition(
            (container.width / 2 - bottomRightContainer.prefWidth / 2).toInt().toFloat(),
            (-container.height / 2 + bottomRightContainer.prefHeight / 2).toInt().toFloat()
        )
    }

    override fun onUpdate(deltaTime: Float) {
        playerControls.update(deltaTime)
    }

    private fun addExitButton() {
        val exitButton = TextButton("Exit", skin)
        exitButton.onChange {
            returnToMainScreen()
        }
        topRightContainer.add(exitButton).minWidth(75f).padTop(5f).padRight(5f)
    }

    private fun addSettingsButtons() {
        val settingsButton = TextButton("Settings", skin)
        settingsButton.onChange {
            ScreenManager.push(SettingsScreen())
        }
        topRightContainer.add(settingsButton).minWidth(75f).padTop(5f).padRight(5f)
    }

    private fun returnToMainScreen() {
        Context.activeGame?.dispose()
        Context.activeGame = null

        ScreenManager.show(MainMenuScreen())
    }

    override fun getShipTargetSpeed() = playerControls.getShipTargetSpeed()

    override fun getShipTargetRudderRotation() = playerControls.getShipTargetRudderRotation()
}