package com.fangelo.plunderous.client.game.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.fangelo.libraries.camera.Camera
import com.fangelo.libraries.transform.Transform
import com.fangelo.libraries.input.InputInfo
import com.fangelo.libraries.ui.ScreenManager
import com.fangelo.plunderous.client.game.components.ship.MainShip
import com.fangelo.plunderous.client.game.components.ship.Ship
import com.fangelo.plunderous.client.game.components.ship.ShipInput
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class UpdateMainShipInputSystem : IteratingSystem(allOf(Ship::class, ShipInput::class, MainShip::class, Transform::class).get()) {
    private val transform = mapperFor<Transform>()
    private val camera = mapperFor<Camera>()
    private val shipInput = mapperFor<ShipInput>()
    private lateinit var cameras: ImmutableArray<Entity>

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        cameras = engine.getEntitiesFor(allOf(Camera::class).get())
    }

    public override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = transform.get(entity)
        val shipInput = shipInput.get(entity)

        shipInput.reset()

        if (!isMainCameraEnabled())
            return

        updateTouchInput(shipInput, transform)
        updateKeyboardInput(shipInput)
    }

    private fun updateTouchInput(shipInput: ShipInput, transform: Transform) {
        if (!InputInfo.touching || InputInfo.touchingTime < 0.25f || InputInfo.zooming)
            return

        val camera = getMainCamera() ?: return

        val x = InputInfo.touchingX
        val y = InputInfo.touchingY

        if (ScreenManager.isUiAtScreenPosition(x.toFloat(), y.toFloat()))
            return

        var touchWorldPos = camera.screenPositionToWorldPosition(x.toFloat(), y.toFloat())

        var touchLocalPos = transform.worldPositionToLocalPosition(touchWorldPos)

        val forwardDistance = touchLocalPos.y
        val rightDistance = touchLocalPos.x

        if (forwardDistance > 2.0f) {
            shipInput.forward = true
        } else if (forwardDistance < -2.0f) {
            shipInput.backward = true
        }

        if (rightDistance > 2.0f) {
            shipInput.right = true
        } else if (rightDistance < -2.0f) {
            shipInput.left = true
        }
    }

    private fun isMainCameraEnabled(): Boolean {
        return getMainCamera()?.enabled ?: false
    }

    private fun getMainCamera(): Camera? {
        return cameras.map { entity -> camera.get(entity) }.find { camera -> camera.id == "Main" }
    }

    private fun updateKeyboardInput(shipInput: ShipInput) {
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            shipInput.right = true

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            shipInput.left = true

        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            shipInput.forward = true

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            shipInput.backward = true
    }
}