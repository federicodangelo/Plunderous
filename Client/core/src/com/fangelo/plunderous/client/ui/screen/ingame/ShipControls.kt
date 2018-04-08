package com.fangelo.plunderous.client.ui.screen.ingame

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Slider
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.fangelo.libraries.utils.format
import com.fangelo.plunderous.client.Context
import com.fangelo.plunderous.client.game.ship.component.Ship

class ShipControls(skin: Skin, bottomCenterContainer: Table, middleRightContainer: Table) {

    private val returnRudderToDefaultPositionSpeed = 2.0f

    private val rudderLabel: Label
    private val rudderSlider: Slider

    private val speedLabel: Label
    private val speedSlider: Slider

    private var rudderReturning = false
    private var rudderReturningValue = 0f

    init {
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

    fun update(deltaTime: Float) {
        returnRudderToDefaultPosition(deltaTime)
        updateLabels()
    }

    private fun returnRudderToDefaultPosition(deltaTime: Float) {
        if (rudderSlider.isDragging) {
            rudderReturning = false
            return
        }

        if (!rudderReturning) {
            rudderReturning = true
            rudderReturningValue = rudderSlider.value
        }

        rudderReturningValue = moveTowards(rudderReturningValue, 0f, deltaTime * returnRudderToDefaultPositionSpeed)
        rudderSlider.value = rudderReturningValue
    }

    private fun moveTowards(value: Float, target: Float, maxDelta: Float): Float {
        return when {
            value > target -> Math.max(value - maxDelta, target)
            value < target -> Math.min(value + maxDelta, target)
            else -> target
        }
    }

    private fun updateLabels() {
        val rudderRotation = getShipTargetRudderRotation()
        rudderLabel.setText("${rudderRotation.format(2)}")

        val speed = getShipTargetSpeed()
        speedLabel.setText("${speed.format(2)}")
    }


    fun getShipTargetSpeed(): Float {
        val game = Context.activeGame ?: return 0f
        val ship = game.player?.getComponent(Ship::class.java) ?: return 0f
        val speed =
            if (speedSlider.value >= 0f)
                speedSlider.value * ship.maxForwardSpeed
            else
                (speedSlider.value / speedSlider.minValue) * ship.maxBackwardSpeed
        return speed
    }

    fun getShipTargetRudderRotation(): Float {
        val game = Context.activeGame ?: return 0f
        val ship = game.player?.getComponent(Ship::class.java) ?: return 0f

        val rudderRotation = rudderSlider.value * ship.maxRudderRotation
        return rudderRotation
    }
}