package com.fangelo.plunderous.client.game.camera.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.input.InputInfo
import com.fangelo.libraries.transform.Transform
import com.fangelo.plunderous.client.Context
import com.fangelo.plunderous.client.game.Game
import com.fangelo.plunderous.client.game.camera.component.GameCamera
import com.fangelo.plunderous.client.game.camera.component.GameCameraState
import com.fangelo.plunderous.client.game.constants.GameCameraConstants
import com.fangelo.plunderous.client.game.constants.GameRenderFlags
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class UpdateGameCameraInputSystem : IteratingSystem(allOf(Camera::class, GameCamera::class).get()) {
    private val minZoomValue = 0.125f
    private val maxZoomValue = 2.0f

    private val camera = mapperFor<Camera>()
    private val gameCamera = mapperFor<GameCamera>()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val camera = camera.get(entity)
        val gameCamera = gameCamera.get(entity)
        val game = Context.activeGame ?: return

        updateZoom(camera, gameCamera)

        updateGameCameraState(camera, gameCamera, game)
    }

    private fun updateGameCameraState(camera: Camera, gameCamera: GameCamera, game: Game) {
        gameCamera.state = getNewCameraState(camera)
        when (gameCamera.state) {
            GameCameraState.FollowingShip -> {
                camera.followTransform = game.playerShip?.getComponent(Transform::class.java)
                camera.followTransformRotation = false
                camera.renderMask = GameRenderFlags.main
            }
            GameCameraState.FollowingAvatar -> {
                camera.followTransform = game.playerAvatar?.getComponent(Transform::class.java)
                camera.followTransformRotation = true
                camera.renderMask = GameRenderFlags.main or GameRenderFlags.ship
            }
            GameCameraState.None -> {

            }
        }
    }

    private fun getNewCameraState(camera: Camera): GameCameraState {
        return if (camera.zoom <= GameCameraConstants.switchToShipZoomLevel)
            GameCameraState.FollowingAvatar
        else
            GameCameraState.FollowingShip
    }


    private fun updateZoom(camera: Camera, gameCamera: GameCamera) {
        if (zoomingJustStarted()) {
            gameCamera.startingZoom = camera.zoom
        }

        if (zooming()) {
            camera.zoom =
                    MathUtils.clamp(
                        gameCamera.startingZoom * (InputInfo.zoomingInitialDistance / InputInfo.zoomingDistance),
                        minZoomValue,
                        maxZoomValue
                    )
        }

        if (scrolling()) {
            camera.zoom = MathUtils.clamp(camera.zoom + InputInfo.scrollingAmount * 0.1f, minZoomValue, maxZoomValue)
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
}