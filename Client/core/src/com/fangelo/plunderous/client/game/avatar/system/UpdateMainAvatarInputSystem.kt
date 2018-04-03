package com.fangelo.plunderous.client.game.avatar.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.input.InputInfo
import com.fangelo.libraries.transform.Transform
import com.fangelo.libraries.ui.ScreenManager
import com.fangelo.plunderous.client.game.avatar.component.Avatar
import com.fangelo.plunderous.client.game.avatar.component.AvatarInput
import com.fangelo.plunderous.client.game.avatar.component.MainAvatar
import com.fangelo.plunderous.client.game.constants.GameCameraConstants
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class UpdateMainAvatarInputSystem : IteratingSystem(allOf(Avatar::class, AvatarInput::class, MainAvatar::class, Transform::class).get()) {
    private val transform = mapperFor<Transform>()
    private val camera = mapperFor<Camera>()
    private val avatarInput = mapperFor<AvatarInput>()
    private lateinit var cameras: ImmutableArray<Entity>

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        cameras = engine.getEntitiesFor(allOf(Camera::class).get())
    }

    public override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = transform.get(entity)
        val avatarInput = avatarInput.get(entity)

        avatarInput.reset()

        if (!isMainCameraZoomedOnAvatar())
            return

        updateTouchInput(avatarInput, transform)
        updateKeyboardInput(avatarInput)
    }

    private fun updateTouchInput(avatarInput: AvatarInput, transform: Transform) {
        if (!InputInfo.touching || InputInfo.touchingTime < 0.1f || InputInfo.zooming)
            return

        val camera = getMainCamera() ?: return

        val x = InputInfo.touchingX
        val y = InputInfo.touchingY

        if (ScreenManager.isUiAtScreenPosition(x.toFloat(), y.toFloat()))
            return

        var touchWorldPos = camera.screenPositionToWorldPosition(x.toFloat(), y.toFloat())

        var touchLocalPos = touchWorldPos.sub(transform.x, transform.y)

        val forwardDistance = touchLocalPos.y
        val rightDistance = touchLocalPos.x

        val minDistance = 0.5f

        if (forwardDistance > minDistance) {
            avatarInput.down = true
        } else if (forwardDistance < -minDistance) {
            avatarInput.up = true
        }

        if (rightDistance > minDistance) {
            avatarInput.right = true
        } else if (rightDistance < -minDistance) {
            avatarInput.left = true
        }
    }

    private fun isMainCameraZoomedOnAvatar(): Boolean {
        return getMainCameraZoom() <= GameCameraConstants.switchToShipZoomLevel
    }

    private fun getMainCameraZoom(): Float {
        return getMainCamera()?.zoom ?: 0f
    }

    private fun getMainCamera(): Camera? {
        return cameras.map { entity -> camera.get(entity) }.find { camera -> camera.id == GameCameraConstants.mainCameraId }
    }

    private fun updateKeyboardInput(avatarInput: AvatarInput) {
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            avatarInput.right = true

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            avatarInput.left = true

        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            avatarInput.up = true

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            avatarInput.down = true
    }
}