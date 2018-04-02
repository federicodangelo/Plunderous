package com.fangelo.plunderous.client.game.camera.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.math.MathUtils
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.input.InputInfo
import com.fangelo.plunderous.client.game.constants.GameCameraIds
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class ProcessCameraInputSystem : EntitySystem() {

    private val switchToShipZoomLevel = 0.250f
    private val minZoomValue = 0.125f
    private val maxZoomValue = 2.0f

    private lateinit var cameras: ImmutableArray<Entity>
    private val camera = mapperFor<Camera>()

    private var mainCameraStartingZoom = 0f

    override fun addedToEngine(engine: Engine) {
        cameras = engine.getEntitiesFor(allOf(Camera::class).get())
    }

    override fun update(deltaTime: Float) {

        val mainCamera = getMainCamera() ?: return

        if (zoomingJustStarted()) {
            mainCameraStartingZoom = mainCamera.zoom
        }

        if (zooming()) {
            mainCamera.zoom =
                    MathUtils.clamp(
                        mainCameraStartingZoom * (InputInfo.zoomingInitialDistance / InputInfo.zoomingDistance),
                        minZoomValue,
                        maxZoomValue
                    )
        }

        if (scrolling()) {
            mainCamera.zoom = MathUtils.clamp(mainCamera.zoom + InputInfo.scrollingAmount * 0.1f, minZoomValue, maxZoomValue)
        }

        if (mainCamera.zoom <= switchToShipZoomLevel) {
            mainCamera.enabled = false
            getShipCamera()?.enabled = true
        } else {
            mainCamera.enabled = true
            getShipCamera()?.enabled = false
        }
    }

    private fun scrolling(): Boolean {
        return InputInfo.scrolling
    }

    private fun zoomingJustStarted(): Boolean {
        return InputInfo.zoomingJustStarted
    }

    private fun zooming(): Boolean {
        return InputInfo.zooming
    }

    private fun getMainCamera(): Camera? {
        return cameras.map { entity -> camera.get(entity) }.find { camera -> camera.id == GameCameraIds.main }
    }

    private fun getShipCamera(): Camera? {
        return cameras.map { entity -> camera.get(entity) }.find { camera -> camera.id == GameCameraIds.ship }
    }
}