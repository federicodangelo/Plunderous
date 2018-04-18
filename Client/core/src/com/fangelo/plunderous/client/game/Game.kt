package com.fangelo.plunderous.client.game

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.physics.box2d.Filter
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.camera.system.UpdateCameraSystem
import com.fangelo.libraries.input.system.InputSystem
import com.fangelo.libraries.light.system.VisualLightsRenderSystem
import com.fangelo.libraries.physics.component.Rigidbody
import com.fangelo.libraries.physics.system.PhysicsSystem
import com.fangelo.libraries.physics.system.UpdatePhysicsSystem
import com.fangelo.libraries.physics.system.VisualDebugPhysicsSystem
import com.fangelo.libraries.render.system.VisualCameraRenderSystem
import com.fangelo.libraries.sprite.system.UpdateVisualAnimationSystem
import com.fangelo.libraries.sprite.system.VisualSpriteRenderSystem
import com.fangelo.libraries.tilemap.system.VisualTilemapRenderSystem
import com.fangelo.plunderous.client.game.water.system.VisualWaterRenderSystem
import com.fangelo.libraries.transform.Transform
import com.fangelo.plunderous.client.game.avatar.system.ProcessAvatarInputSystem
import com.fangelo.plunderous.client.game.avatar.system.UpdateAvatarAnimationSystem
import com.fangelo.plunderous.client.game.avatar.system.UpdateMainAvatarInputSystem
import com.fangelo.plunderous.client.game.camera.system.ProcessCameraInputSystem
import com.fangelo.plunderous.client.game.constants.GameCameraConstants
import com.fangelo.plunderous.client.game.constants.GameRenderFlags
import com.fangelo.plunderous.client.game.debug.GameDebug
import com.fangelo.plunderous.client.game.generator.system.ProcessGeneratorSystem
import com.fangelo.plunderous.client.game.island.system.VisualIslandRenderSystem
import com.fangelo.plunderous.client.game.ship.system.ProcessShipInputSystem
import com.fangelo.plunderous.client.game.ship.system.ShipInputProvider
import com.fangelo.plunderous.client.game.ship.system.UpdateMainShipInputSystem
import ktx.ashley.entity
import ktx.ashley.get
import ktx.assets.load

private const val REF_HEIGHT_IN_TILES = 32

class Game {

    var playerShip: Entity? = null
        private set

    private val engine = PooledEngine()
    private val mainCamera: Camera
    private val assetManager = AssetManager()

    val debug = GameDebug()

    constructor(shipInputProvider: ShipInputProvider) {

        loadAssets()

        mainCamera = addMainCamera()

        initEngineSystems(shipInputProvider)

        resize(Gdx.graphics.width, Gdx.graphics.height)

        buildGame()

        initCamera()

        initDebug()
    }

    private fun initDebug() {

        debug.addSwitch(
            "Lights",
            { lightsRendererSystem.enabled },
            { lightsRendererSystem.enabled = !lightsRendererSystem.enabled }
        )

        debug.addSwitch(
            "Draw Physics Info",
            { debugPhysicsSystem.enabled },
            { debugPhysicsSystem.enabled = !debugPhysicsSystem.enabled }
        )

        debug.addSwitch(
            "Ship Collision",
            {
                val maskBits = playerShip?.getComponent(Rigidbody::class.java)?.native?.fixtureList?.get(0)?.filterData?.maskBits?.toInt() ?: 0
                maskBits == -1
            },
            {
                val filterData = playerShip?.getComponent(Rigidbody::class.java)?.native?.fixtureList?.get(0)?.filterData ?: Filter()
                filterData.maskBits = if (filterData.maskBits.toInt() == -1) 0 else -1
                playerShip?.getComponent(Rigidbody::class.java)?.native?.fixtureList?.get(0)?.filterData = filterData
            }
        )
    }

    private fun initCamera() {
        mainCamera.followTransform = playerShip?.get()
        mainCamera.followTransformRotation = false
    }

    private fun buildGame() {
        val gameBuilder = GameBuilder()
        gameBuilder.build(engine, assetManager, mainCamera)
        this.playerShip = gameBuilder.playerShip
    }

    private lateinit var debugPhysicsSystem: VisualDebugPhysicsSystem
    private lateinit var lightsRendererSystem: VisualLightsRenderSystem
    lateinit var processShipInputSystem: ProcessShipInputSystem

    private fun initEngineSystems(shipInputProvider: ShipInputProvider) {

        var cameraRenderSystem = VisualCameraRenderSystem()

        processShipInputSystem = ProcessShipInputSystem()

        engine.addSystem(PhysicsSystem())
        engine.addSystem(UpdatePhysicsSystem())
        engine.addSystem(ProcessGeneratorSystem())
        engine.addSystem(UpdateMainShipInputSystem(shipInputProvider))
        engine.addSystem(UpdateMainAvatarInputSystem())
        engine.addSystem(processShipInputSystem)
        engine.addSystem(ProcessAvatarInputSystem())
        engine.addSystem(ProcessCameraInputSystem())
        engine.addSystem(UpdateCameraSystem())
        engine.addSystem(UpdateAvatarAnimationSystem())
        engine.addSystem(UpdateVisualAnimationSystem())
        engine.addSystem(InputSystem())

        cameraRenderSystem.addRenderer(VisualWaterRenderSystem())
        cameraRenderSystem.addRenderer(VisualTilemapRenderSystem())
        cameraRenderSystem.addRenderer(VisualIslandRenderSystem())
        cameraRenderSystem.addRenderer(VisualSpriteRenderSystem())
        lightsRendererSystem = cameraRenderSystem.addRenderer(VisualLightsRenderSystem())
        debugPhysicsSystem = cameraRenderSystem.addRenderer(VisualDebugPhysicsSystem())

        engine.addSystem(cameraRenderSystem)
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
}