package com.fangelo.plunderous.client.ui.screen.ingame

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.fangelo.libraries.input.InputInfo
import com.fangelo.libraries.physics.component.Rigidbody
import com.fangelo.libraries.physics.component.World
import com.fangelo.libraries.render.component.VisualComponent
import com.fangelo.libraries.transform.Transform
import com.fangelo.libraries.ui.ScreenManager
import com.fangelo.libraries.utils.format
import com.fangelo.plunderous.client.Context
import com.fangelo.plunderous.client.game.Game
import com.fangelo.plunderous.client.game.camera.component.GameCamera
import com.fangelo.plunderous.client.game.camera.component.GameCameraState
import com.fangelo.plunderous.client.game.constants.GameRenderFlags
import com.fangelo.plunderous.client.game.island.component.Island
import com.fangelo.plunderous.client.game.ship.component.Ship
import com.fangelo.plunderous.client.game.ship.system.ShipMovementType
import ktx.actors.onChange

class PlayerControls(skin: Skin, bottomCenterContainer: Table, middleRightContainer: Table, middleLeftContainer: Table) {

    private val returnRudderToDefaultPositionSpeed = 2.0f

    private val rudderLabel: Label
    private val rudderSlider: Slider

    private val speedLabel: Label
    private val speedSlider: Slider

    private val goToIslandButton: Button
    private val goToShipButton: Button

    private var rudderReturning = false
    private var rudderReturningValue = 0f

    private var touchingSpeed = 0f
    private var touchingRotation = MathUtils.PI

    init {
        rudderSlider = Slider(-1.0f, 1.0f, 0.1f, false, skin)
        rudderSlider.value = 0f
        rudderLabel = Label("", skin)

        speedSlider = Slider(-0.1f, 1.0f, 0.1f, true, skin)
        speedSlider.value = 0f
        speedLabel = Label("", skin)

        goToIslandButton = TextButton("Go To\nIsland", skin)
        goToIslandButton.onChange {
            goToIsland()
        }

        goToShipButton = TextButton("Go To\nShip", skin)
        goToShipButton.onChange {
            goToShip()
        }

        middleLeftContainer.row().left().table.add(goToIslandButton).padLeft(10f)
        middleLeftContainer.row().left().table.add(goToShipButton).padLeft(10f)

        bottomCenterContainer.row().center().table.add(rudderLabel).padBottom(0f)
        bottomCenterContainer.row().center().table.add(rudderSlider).padBottom(10f).width(200f)

        middleRightContainer.row().right().table.add(speedLabel).padRight(10f).padBottom(0f)
        middleRightContainer.row().right().table.add(speedSlider).padRight(10f).height(200f)
    }

    private fun goToIsland() {
        val game = Context.activeGame ?: return
        val mainWorld = game.mainWorld ?: return
        val closestIsland = getClosestIslandToPlayerShip(game) ?: return

        val playerAvatar = game.playerAvatar ?: return
        val playerAvatarTransform = playerAvatar.getComponent(Transform::class.java)
        val playerAvatarRigidbody = playerAvatar.getComponent(Rigidbody::class.java)

        updateEntityRenderFlags(playerAvatar, GameRenderFlags.main)
        playerAvatarTransform.rotation = 0f
        playerAvatarRigidbody.moveToOtherWorld(mainWorld)

        Gdx.app.log("[PLAYER]", "Going to island $closestIsland")
    }

    private fun goToShip() {
        val game = Context.activeGame ?: return
        val closestShip = getClosestShipToPlayer(game) ?: return
        val closestShipWorld = closestShip.getComponent(World::class.java)

        val playerAvatar = game.playerAvatar ?: return
        val playerAvatarTransform = playerAvatar.getComponent(Transform::class.java)
        val playerAvatarRigidbody = playerAvatar.getComponent(Rigidbody::class.java)

        updateEntityRenderFlags(playerAvatar, GameRenderFlags.ship)
        playerAvatarTransform.set(0f, 0f, 0f)
        playerAvatarRigidbody.moveToOtherWorld(closestShipWorld)

        Gdx.app.log("[PLAYER]", "Going to ship $closestShip")
    }

    private fun updateEntityRenderFlags(playerAvatar: Entity, renderFlags: Int) {
        for (component in playerAvatar.components) {
            if (component is VisualComponent) {
                component.renderFlags = renderFlags
            }
        }
    }

    fun update(deltaTime: Float) {
        val game = Context.activeGame ?: return

        updateShipMovement(game, deltaTime)
        updateGoToIslandButton(game)
        updateGoToShipButton(game)
    }

    private fun updateGoToIslandButton(game: Game) {
        goToIslandButton.isVisible = isPlayerOnShip(game) && getClosestIslandToPlayerShip(game) != null
    }

    private fun updateGoToShipButton(game: Game) {
        goToShipButton.isVisible = !isPlayerOnShip(game) && getClosestShipToPlayer(game) != null
    }

    private fun isPlayerOnShip(game: Game): Boolean {

        val avatarRigidbody = game.playerAvatar?.getComponent(Rigidbody::class.java) ?: return false

        if (avatarRigidbody.world?.followTransform != null)
            return true

        return false
    }

    private fun getCloseIslands(world: World, centerX: Float, centerY: Float, width: Float, height: Float): List<Entity> {
        val closeBodies = world.getBodiesInAABB(centerX, centerY, width, height)
        val closeIslands = closeBodies.filter { body -> body.entity?.getComponent(Island::class.java) != null }.mapNotNull { body -> body.entity }
        return closeIslands
    }

    private fun getClosestIslandToPlayerShip(game: Game): Island? {
        val playerShip = game.playerShip ?: return null

        val transform = playerShip.getComponent(Transform::class.java)
        val rigidbody = playerShip.getComponent(Rigidbody::class.java)

        val world = rigidbody.world ?: return null

        val closeIslands = getCloseIslands(world, transform.x, transform.y, 10f, 10f)

        if (closeIslands.isEmpty())
            return null

        return closeIslands.reduce({ acc, island ->

            val accTransform = acc.getComponent(Transform::class.java)
            val islandTransform = island.getComponent(Transform::class.java)

            val accDistanceToPlayerShip = Vector2.dst(accTransform.x, accTransform.y, transform.x, transform.y)
            val islandDistanceToPlayerShip = Vector2.dst(islandTransform.x, islandTransform.y, transform.x, transform.y)

            if (islandDistanceToPlayerShip < accDistanceToPlayerShip) island else acc
        }).getComponent(Island::class.java)
    }

    private fun getCloseShips(world: World, centerX: Float, centerY: Float, width: Float, height: Float): List<Entity> {
        val closeBodies = world.getBodiesInAABB(centerX, centerY, width, height)
        val closeShips = closeBodies.filter { body -> body.entity?.getComponent(Ship::class.java) != null }.mapNotNull { body -> body.entity }
        return closeShips
    }

    private fun getClosestShipToPlayer(game: Game): Entity? {
        val playerAvatar = game.playerAvatar ?: return null

        val transform = playerAvatar.getComponent(Transform::class.java)
        val rigidbody = playerAvatar.getComponent(Rigidbody::class.java)

        val world = rigidbody.world ?: return null

        val closeShips = getCloseShips(world, transform.x, transform.y, 10f, 10f)

        if (closeShips.isEmpty())
            return null

        return closeShips.reduce({ acc, island ->

            val accTransform = acc.getComponent(Transform::class.java)
            val islandTransform = island.getComponent(Transform::class.java)

            val accDistanceToPlayerShip = Vector2.dst(accTransform.x, accTransform.y, transform.x, transform.y)
            val islandDistanceToPlayerShip = Vector2.dst(islandTransform.x, islandTransform.y, transform.x, transform.y)

            if (islandDistanceToPlayerShip < accDistanceToPlayerShip) island else acc
        })
    }

    private fun updateShipMovement(game: Game, deltaTime: Float) {
        when (game.processShipInputSystem.movementType) {
            ShipMovementType.REALISTIC -> {
                showShipMovementControls()
                returnRudderToDefaultPosition(deltaTime)
                updateLabels()
            }
            ShipMovementType.SIMPLIFIED -> {
                hideShipMovementControls()
                updateShipTouchInput(game)
            }
        }
    }

    private fun hideShipMovementControls() {
        speedSlider.isVisible = false
        speedLabel.isVisible = false
        rudderSlider.isVisible = false
        rudderLabel.isVisible = false
    }

    private fun showShipMovementControls() {
        speedSlider.isVisible = true
        speedLabel.isVisible = true
        rudderSlider.isVisible = true
        rudderLabel.isVisible = true
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
        val ship = game.playerShip?.getComponent(Ship::class.java) ?: return 0f

        return when (game.processShipInputSystem.movementType) {
            ShipMovementType.REALISTIC -> {
                val speed =
                    if (speedSlider.value >= 0f)
                        speedSlider.value * ship.maxForwardSpeed
                    else
                        (speedSlider.value / speedSlider.minValue) * ship.maxBackwardSpeed
                return speed
            }
            ShipMovementType.SIMPLIFIED -> touchingSpeed * ship.maxForwardSpeed
        }
    }

    fun getShipTargetRudderRotation(): Float {
        val game = Context.activeGame ?: return 0f

        return when (game.processShipInputSystem.movementType) {
            ShipMovementType.REALISTIC -> {
                val ship = game.playerShip?.getComponent(Ship::class.java) ?: return 0f
                val rudderRotation = rudderSlider.value * ship.maxRudderRotation
                return rudderRotation
            }
            ShipMovementType.SIMPLIFIED -> touchingRotation
        }
    }

    private fun updateShipTouchInput(game: Game) {
        if (!InputInfo.touching || InputInfo.touchingTime < 0.25f || InputInfo.zooming || !isGameCameraFollowingShip(game)) {
            touchingSpeed = 0.0f
            return
        }

        val touchingPos = Vector2(InputInfo.touchingX.toFloat(), InputInfo.touchingY.toFloat())

        if (ScreenManager.isUiAtScreenPosition(touchingPos.x, touchingPos.y))
            return

        val screenWidth = Gdx.graphics.width
        val screenHeight = Gdx.graphics.height

        val screenCenterX = screenWidth / 2
        val screenCenterY = screenHeight / 2

        val screenLen = Math.sqrt((screenCenterX * screenCenterX + screenCenterY * screenCenterY).toDouble()).toFloat()

        val touchingPosRelativeToCenter = Vector2(touchingPos.x - screenCenterX, touchingPos.y - screenCenterY)
        val touchingDistanceToCenter = touchingPosRelativeToCenter.len()

        val angle = touchingPosRelativeToCenter.angleRad() - MathUtils.PI / 2.0f
        val distanceNormalized = touchingDistanceToCenter / screenLen

        if (distanceNormalized > 0.1f) {
            touchingRotation = angle
            touchingSpeed = MathUtils.clamp(distanceNormalized / 0.35f, 0.0f, 1.0f)
        } else {
            touchingSpeed = 0.0f
        }

        //Gdx.app.log("DEBUG", "Angle: $angle Distance: $distanceNormalized")
    }

    private fun isGameCameraFollowingShip(game: Game): Boolean {
        return game.mainGameCamera?.getComponent(GameCamera::class.java)?.state == GameCameraState.FollowingShip
    }
}