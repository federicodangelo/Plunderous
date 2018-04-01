package com.fangelo.plunderous.client.game.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.fangelo.libraries.ashley.components.Camera
import com.fangelo.libraries.input.InputInfo
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class ProcessCameraInputSystem : EntitySystem() {

    private val minZoomValue = 0.125f
    private val maxZoomValue = 2.0f

    private lateinit var cameras: ImmutableArray<Entity>
    private val camera = mapperFor<Camera>()
    private var zoomingCameras: List<Pair<Camera, Float>> = mutableListOf()

    override fun addedToEngine(engine: Engine) {
        cameras = engine.getEntitiesFor(allOf(Camera::class).get())
    }

    override fun update(deltaTime: Float) {

        if (zoomingJustStarted()) {
            zoomingCameras = getActiveCameras()
        }

        if (zooming()) {
            for (pair in zoomingCameras) {
                val camera = pair.first
                val startingZoom = pair.second

                camera.zoom =
                        MathUtils.clamp(startingZoom * (InputInfo.zoomingInitialDistance / InputInfo.zoomingDistance), minZoomValue, maxZoomValue)

                Gdx.app.log("Input", "Initial distance: ${InputInfo.zoomingInitialDistance} Distance: ${InputInfo.zoomingDistance}")
            }
        }

        if (scrolling()) {
            for (pair in getActiveCameras()) {
                val camera = pair.first
                camera.zoom = MathUtils.clamp(camera.zoom + InputInfo.scrollingAmount * 0.1f, minZoomValue, maxZoomValue)
            }
        }
    }

    private fun getActiveCameras() = cameras.map { camera -> Pair(this.camera.get(camera), this.camera.get(camera).zoom) }

    private fun scrolling(): Boolean {
        return InputInfo.scrolling

    }

    private fun zoomingJustStarted(): Boolean {
        return InputInfo.zoomingJustStarted
    }

    private fun zooming(): Boolean {
        return InputInfo.zooming
    }

}