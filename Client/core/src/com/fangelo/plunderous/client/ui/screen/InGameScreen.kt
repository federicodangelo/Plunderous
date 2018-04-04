package com.fangelo.plunderous.client.ui.screen

import com.badlogic.gdx.scenes.scene2d.ui.*
import com.fangelo.libraries.ui.Screen
import com.fangelo.libraries.ui.ScreenManager
import com.fangelo.libraries.utils.format
import com.fangelo.plunderous.client.Context
import com.fangelo.plunderous.client.game.ship.component.Ship
import com.fangelo.plunderous.client.game.ship.system.ShipInputProvider
import ktx.actors.onChange

class InGameScreen : Screen(), ShipInputProvider {
    private val topLeftContainer: Table
    private val middleRightContainer: Table
    private val topRightContainer: Table
    private val bottomRightContainer: Table
    private val bottomCenterContainer: Table
    private val bottomLeftContainer: Table

    private val container: WidgetGroup

    private lateinit var rudderLabel: Label
    private lateinit var rudderSlider: Slider

    private lateinit var speedLabel: Label
    private lateinit var speedSlider: Slider

    init {

        container = WidgetGroup()
        add(container).fill()

        topLeftContainer = Table()
        middleRightContainer = Table()
        topRightContainer = Table()
        bottomRightContainer = Table()
        bottomCenterContainer = Table()
        bottomLeftContainer = Table()

        container.addActor(topLeftContainer)
        container.addActor(middleRightContainer)
        container.addActor(topRightContainer)
        container.addActor(bottomLeftContainer)
        container.addActor(bottomCenterContainer)
        container.addActor(bottomRightContainer)

        addDebugButtons()

        addExitButton()

        addShipControls()
    }

    private fun addShipControls() {

        rudderSlider = Slider(-1.0f, 1.0f, 0.1f, false, skin)
        rudderSlider.value = 0f
        rudderLabel = Label("", skin)

        speedSlider = Slider(-0.1f, 1.0f, 0.1f, true, skin)
        speedSlider.value = 0f
        speedLabel = Label("", skin)


        bottomCenterContainer.row().center().table.add(rudderLabel).padBottom(0f)
        bottomCenterContainer.row().center().table.add(rudderSlider).padBottom(10f).width(200f)

        middleRightContainer.row().right().table.add(speedLabel).padRight(10f).padBottom(0f)
        middleRightContainer.row().right().table.add(speedSlider).padRight(10f).height(200f)
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
        updateLabels()
    }

    private fun updateLabels() {
        val rudderRotation = getShipTargetRudderRotation()
        rudderLabel.setText("${rudderRotation.format(2)}")

        val speed = getShipTargetSpeed()
        speedLabel.setText("${speed.format(2)}")
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
        topRightContainer.add(lightsButton).minWidth(75f).padTop(5f).padRight(5f)

        val drawDebugButton = TextButton("Debug", skin)
        drawDebugButton.onChange {
            Context.activeGame?.switchDrawDebug()
        }
        topRightContainer.add(drawDebugButton).minWidth(75f).padTop(5f).padRight(5f)
    }

    private fun returnToMainScreen() {
        Context.activeGame?.dispose()
        Context.activeGame = null

        ScreenManager.show(MainMenuScreen())
    }

    override fun getShipTargetSpeed(): Float {
        val game = Context.activeGame ?: return 0f
        val ship = game.player?.getComponent(Ship::class.java) ?: return 0f
        val speed =
            if (speedSlider.value >= 0f)
                speedSlider.value * ship.maxForwardSpeed
            else
                (speedSlider.value / speedSlider.minValue) * ship.maxBackwardSpeed
        return speed
    }

    override fun getShipTargetRudderRotation(): Float {
        val game = Context.activeGame ?: return 0f
        val ship = game.player?.getComponent(Ship::class.java) ?: return 0f

        val rudderRotation = rudderSlider.value * ship.maxRudderRotation
        return rudderRotation
    }

}