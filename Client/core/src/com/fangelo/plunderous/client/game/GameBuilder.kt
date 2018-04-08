package com.fangelo.plunderous.client.game

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.light.component.WorldLight
import com.fangelo.libraries.physics.component.World
import com.fangelo.plunderous.client.game.avatar.factory.AvatarFactory
import com.fangelo.plunderous.client.game.generator.component.GeneratorAreaSource
import com.fangelo.plunderous.client.game.generator.generators.IslandsGeneratorFactory
import com.fangelo.plunderous.client.game.ship.factory.ShipFactory
import com.fangelo.plunderous.client.game.water.component.VisualWater
import com.fangelo.plunderous.client.game.water.component.Water
import ktx.ashley.entity

class GameBuilder {

    private val playerShipSpawnPositionX = 0f
    private val playerShipSpawnPositionY = 0f

    private lateinit var engine: Engine
    private lateinit var assetManager: AssetManager
    private lateinit var camera: Camera

    var playerShip: Entity? = null
        private set

    var playerAvatar: Entity? = null
        private set

    var mainWorld: World? = null
        private set

    var shipWorld: World? = null
        private set

    fun build(engine: Engine, assetManager: AssetManager, camera: Camera) {

        this.engine = engine
        this.assetManager = assetManager
        this.camera = camera

        addMainWorld()
        addWater()
        addPlayerShip()
        addPlayerAvatar()
        addGenerators()
    }

    private fun addGenerators() {
        engine.entity().add(IslandsGeneratorFactory(engine, mainWorld!!, assetManager).buildComponent())
    }

    private fun addMainWorld() {

        val entity = engine.entity {
            with<World>()
            with<WorldLight>()
        }

        mainWorld = entity.getComponent(World::class.java)
    }

    private fun addWater() {
        val tilesetAtlas = assetManager.get<TextureAtlas>("tiles/tiles.atlas")
        val waterTileTexture = tilesetAtlas.findRegion("water")

        engine.entity {
            with<Water>()
            with<VisualWater> {
                this.waterTileTexture = waterTileTexture
            }
        }
    }

    private fun addPlayerShip() {
        val playerShip = ShipFactory(engine, assetManager, mainWorld!!)
            .buildMainShip(playerShipSpawnPositionX, playerShipSpawnPositionY)
        playerShip.getComponent(GeneratorAreaSource::class.java).followCamera = camera
        shipWorld = playerShip.getComponent(World::class.java)
        this.playerShip = playerShip
    }


    private fun addPlayerAvatar() {
        this.playerAvatar = AvatarFactory(assetManager, engine).buildMainAvatar(shipWorld!!, 0f, 0f)
    }
}