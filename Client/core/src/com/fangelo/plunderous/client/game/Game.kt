package com.fangelo.plunderous.client.game

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.fangelo.libraries.ashley.components.Camera
import com.fangelo.libraries.ashley.components.Transform
import com.fangelo.libraries.ashley.systems.*
import com.fangelo.plunderous.client.game.systems.ProcessShipInputSystem
import com.fangelo.plunderous.client.game.systems.VisualIslandRenderSystem
import com.fangelo.plunderous.client.game.world.WorldBuilder
import ktx.ashley.entity
import ktx.ashley.get
import ktx.assets.load

private const val REF_HEIGHT_IN_TILES = 32

class Game {

    var player: Entity? = null
        private set

    private val engine = PooledEngine()
    private val camera: Camera
    private val assetManager = AssetManager()
    private var debugEnabled = false

    init {

        loadAssets()

        camera = addCamera()

        initEngineSystems()

        resize(Gdx.graphics.width, Gdx.graphics.height)

        val worldBuilder = buildWorld()

        this.player = worldBuilder.player

        //disableDebug()
        enableDebug()
        switchLights()

        camera.followTransform = player?.get()
        camera.followTransformRotation = false
    }

    private fun buildWorld(): WorldBuilder {
        val worldBuilder = WorldBuilder()
        worldBuilder.build(engine, assetManager)
        return worldBuilder
    }

    private fun initEngineSystems() {

        engine.addSystem(PhysicsSystem())
        engine.addSystem(UpdatePhysicsSystem())
        engine.addSystem(UpdateCameraSystem())
        engine.addSystem(ProcessShipInputSystem())
        engine.addSystem(UpdateVisualAnimationSystem())
        engine.addSystem(VisualTilemapRenderSystem())
        engine.addSystem(VisualIslandRenderSystem())
        engine.addSystem(VisualSpriteRenderSystem())
        engine.addSystem(VisualLightsRenderSystem())
        engine.addSystem(VisualDebugPhysicsSystem())
    }

    private fun disableDebug() {
        debugEnabled = false
        engine.getSystem(VisualDebugPhysicsSystem::class.java).setProcessing(false)
    }

    private fun enableDebug() {
        debugEnabled = true
        engine.getSystem(VisualDebugPhysicsSystem::class.java).setProcessing(true)
    }

    private fun loadAssets() {
        assetManager.load<TextureAtlas>("tiles/tiles.atlas", TextureAtlasLoader.TextureAtlasParameter(true))
        assetManager.load<TextureAtlas>("items/items.atlas", TextureAtlasLoader.TextureAtlasParameter(true))
        assetManager.load<TextureAtlas>("ships/ships.atlas", TextureAtlasLoader.TextureAtlasParameter(true))
        assetManager.finishLoading()
    }

    private fun addCamera(): Camera {

        val entity = engine.entity {
            with<Transform> {
                set(16.5f, 16.5f, 0f)
            }
            with<Camera>()
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
        camera.resize((width.toDouble() * scale).toInt(), (height.toDouble() * scale).toInt())
    }

    fun dispose() {
        engine.removeAllEntities()
        engine.systems.toArray().forEach { system -> engine.removeSystem(system) }
        assetManager.dispose()
    }

    fun switchLights() {
        val lightsRenderSystem = engine.getSystem(VisualLightsRenderSystem::class.java)
        lightsRenderSystem.setProcessing(!lightsRenderSystem.checkProcessing())
    }

    fun switchDrawDebug() {
        if (debugEnabled)
            disableDebug()
        else
            enableDebug()
    }
}