package com.fangelo.plunderous.client.game

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.camera.system.UpdateCameraSystem
import com.fangelo.libraries.input.system.InputSystem
import com.fangelo.libraries.light.system.VisualLightsRenderSystem
import com.fangelo.libraries.physics.system.PhysicsSystem
import com.fangelo.libraries.physics.system.UpdatePhysicsSystem
import com.fangelo.libraries.physics.system.VisualDebugPhysicsSystem
import com.fangelo.libraries.render.system.VisualCameraRenderSystem
import com.fangelo.libraries.sprite.system.UpdateVisualAnimationSystem
import com.fangelo.libraries.sprite.system.VisualSpriteRenderSystem
import com.fangelo.libraries.tilemap.system.VisualTilemapRenderSystem
import com.fangelo.libraries.transform.Transform
import com.fangelo.plunderous.client.game.avatar.system.ProcessAvatarInputSystem
import com.fangelo.plunderous.client.game.avatar.system.UpdateAvatarAnimationSystem
import com.fangelo.plunderous.client.game.avatar.system.UpdateMainAvatarInputSystem
import com.fangelo.plunderous.client.game.camera.system.ProcessCameraInputSystem
import com.fangelo.plunderous.client.game.constants.GameCameraConstants
import com.fangelo.plunderous.client.game.constants.GameRenderFlags
import com.fangelo.plunderous.client.game.island.system.VisualIslandRenderSystem
import com.fangelo.plunderous.client.game.ship.system.ProcessShipInputSystem
import com.fangelo.plunderous.client.game.ship.system.UpdateMainShipInputSystem
import ktx.ashley.entity
import ktx.ashley.get
import ktx.assets.load

private const val REF_HEIGHT_IN_TILES = 32

class Game {

    var player: Entity? = null
        private set

    private val engine = PooledEngine()
    private val mainCamera: Camera
    private val assetManager = AssetManager()
    private var debugEnabled = false

    init {

        loadAssets()

        mainCamera = addMainCamera()

        initEngineSystems()

        resize(Gdx.graphics.width, Gdx.graphics.height)

        buildGame()

        //disableDebug()
        enableDebug()
        switchLights()

        initCamera()
    }

    private fun initCamera() {
        mainCamera.followTransform = player?.get()
        mainCamera.followTransformRotation = false
    }

    private fun buildGame() {
        val gameBuilder = GameBuilder()
        gameBuilder.build(engine, assetManager)
        this.player = gameBuilder.playerShip
    }

    private lateinit var debugPhysicsSystem: VisualDebugPhysicsSystem
    private lateinit var lightsRendererSystem: VisualLightsRenderSystem

    private fun initEngineSystems() {

        var cameraRenderSystem = VisualCameraRenderSystem()

        engine.addSystem(PhysicsSystem())
        engine.addSystem(UpdatePhysicsSystem())
        engine.addSystem(UpdateMainShipInputSystem())
        engine.addSystem(UpdateMainAvatarInputSystem())
        engine.addSystem(ProcessShipInputSystem())
        engine.addSystem(ProcessAvatarInputSystem())
        engine.addSystem(ProcessCameraInputSystem())
        engine.addSystem(UpdateCameraSystem())
        engine.addSystem(UpdateAvatarAnimationSystem())
        engine.addSystem(UpdateVisualAnimationSystem())
        engine.addSystem(InputSystem())
        engine.addSystem(cameraRenderSystem)

        cameraRenderSystem.addRenderer(VisualTilemapRenderSystem())
        cameraRenderSystem.addRenderer(VisualIslandRenderSystem())
        cameraRenderSystem.addRenderer(VisualSpriteRenderSystem())
        lightsRendererSystem = cameraRenderSystem.addRenderer(VisualLightsRenderSystem())
        debugPhysicsSystem = cameraRenderSystem.addRenderer(VisualDebugPhysicsSystem())

    }

    private fun disableDebug() {
        debugEnabled = false
        debugPhysicsSystem.enabled = false
    }

    private fun enableDebug() {
        debugEnabled = true
        debugPhysicsSystem.enabled = true
    }

    private fun loadAssets() {
        assetManager.load<TextureAtlas>("tiles/tiles.atlas", TextureAtlasLoader.TextureAtlasParameter(true))
        assetManager.load<TextureAtlas>("items/items.atlas", TextureAtlasLoader.TextureAtlasParameter(true))
        assetManager.load<TextureAtlas>("ships/ships.atlas", TextureAtlasLoader.TextureAtlasParameter(true))
        assetManager.load<TextureAtlas>("players/players.atlas", TextureAtlasLoader.TextureAtlasParameter(true))
        assetManager.finishLoading()
    }

    private fun addMainCamera(): Camera {

        val entity = engine.entity {
            with<Transform> {
                set(16.5f, 16.5f, 0f)
            }
            with<Camera> {
                id = GameCameraConstants.mainCameraId
                renderMask = GameRenderFlags.main
            }
        }

        return entity.getComponent(Camera::class.java)
    }

    fun update(deltaTime: Float) {

        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            if (debugEnabled)
                disableDebug()
            else
                enableDebug()
        }

        engine.update(deltaTime)
    }

    fun resize(width: Int, height: Int) {
        var scale = height.toDouble() / REF_HEIGHT_IN_TILES.toDouble()
        scale = 1.0 / scale
        mainCamera.resize((width.toDouble() * scale).toInt(), (height.toDouble() * scale).toInt())
    }

    fun dispose() {
        engine.removeAllEntities()
        engine.systems.toArray().forEach { system -> engine.removeSystem(system) }
        assetManager.dispose()
    }

    fun switchLights() {
        lightsRendererSystem.enabled = !lightsRendererSystem.enabled
    }

    fun switchDrawDebug() {
        if (debugEnabled)
            disableDebug()
        else
            enableDebug()
    }
}