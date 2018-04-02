package com.fangelo.plunderous.client.ui.screen

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.fangelo.libraries.ui.Screen
import com.fangelo.libraries.ui.ScreenManager
import com.fangelo.libraries.utils.format
import com.fangelo.plunderous.client.Context
import com.fangelo.plunderous.client.game.ship.component.Ship
import ktx.actors.onChange

class InGameScreen : Screen() {

    private val topLeftContainer: Table
    private val topRightContainer: Table
    private val bottomRightContainer: Table
    private val bottomLeftContainer: Table

    private val container: WidgetGroup

    private val rudderRotationLabel: Label

    init {

        container = WidgetGroup()
        add(container).fill()

        topLeftContainer = Table()
        topRightContainer = Table()
        bottomRightContainer = Table()
        bottomLeftContainer = Table()

        container.addActor(topLeftContainer)
        container.addActor(topRightContainer)
        container.addActor(bottomRightContainer)
        container.addActor(bottomLeftContainer)

        addExitButton()

        addDebugButtons()

        rudderRotationLabel = addRudderRotationLabel()
    }

    override fun onLayout() {

        container.width = width
        container.height = height

        topLeftContainer.setPosition(
            (-container.width / 2 + topLeftContainer.prefWidth / 2).toInt().toFloat(),
            (container.height / 2 - topLeftContainer.prefHeight / 2).toInt().toFloat()
        )

        topRightContainer.setPosition(
            (container.width / 2 - topRightContainer.prefWidth / 2).toInt().toFloat(),
            (container.height / 2 - topRightContainer.prefHeight / 2).toInt().toFloat()
        )

        bottomLeftContainer.setPosition(
            (-container.width / 2 + bottomLeftContainer.prefWidth / 2).toInt().toFloat(),
            (-container.height / 2 + bottomLeftContainer.prefHeight / 2).toInt().toFloat()
        )

        bottomRightContainer.setPosition(
            (container.width / 2 - bottomRightContainer.prefWidth / 2).toInt().toFloat(),
            (-container.height / 2 + bottomRightContainer.prefHeight / 2).toInt().toFloat()
        )
    }

    override fun onUpdate(deltaTime: Float) {
        updateRudderLabel()
    }

    private fun updateRudderLabel() {
        val game = Context.activeGame ?: return
        val rudderRotation = game.player?.getComponent(Ship::class.java)?.rudderRotation ?: 0f
        rudderRotationLabel.setText("Rudder Rot: ${rudderRotation.format(2)}")
    }


    private fun addExitButton() {
        val exitButton = TextButton("Exit", skin)

        exitButton.onChange {
            returnToMainScreen()
        }

        topRightContainer.add(exitButton).minWidth(75f).padTop(5f).padRight(5f)
    }

    private fun addDebugButtons() {
        val lightsButton = TextButton("Lights", skin)
        lightsButton.onChange {
            Context.activeGame?.switchLights()
        }
        bottomRightContainer.add(lightsButton).minWidth(75f).padBottom(5f).padRight(5f)

        val drawDebugButton = TextButton("Draw Debug", skin)
        drawDebugButton.onChange {
            Context.activeGame?.switchDrawDebug()
        }
        bottomRightContainer.add(drawDebugButton).padBottom(5f).padRight(5f)
    }

    private fun addRudderRotationLabel(): Label {
        var label = Label("", skin)

        bottomLeftContainer.add(label).padBottom(5f).padLeft(5f)

        return label
    }


    private fun returnToMainScreen() {
        Context.activeGame?.dispose()
        Context.activeGame = null

        ScreenManager.show(MainMenuScreen())
    }
}