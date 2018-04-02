package com.fangelo.libraries.light

import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.fangelo.libraries.camera.Camera
import com.fangelo.libraries.physics.World
import com.fangelo.libraries.render.VisualCameraRenderer
import com.fangelo.libraries.transform.Transform
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class VisualLightsRenderSystem : VisualCameraRenderer() {

    private lateinit var lights: ImmutableArray<Entity>
    private lateinit var worldLights: ImmutableArray<Entity>

    private val light = mapperFor<Light>()
    private val transform = mapperFor<Transform>()
    private val worldLight = mapperFor<WorldLight>()

    private val worldLightsListener = WorldLightsListener()
    private val lightsListener = LightsListener()


    override fun addedToEngine(engine: Engine) {
        lights = engine.getEntitiesFor(allOf(Transform::class, Light::class).get())
        worldLights = engine.getEntitiesFor(allOf(World::class, WorldLight::class).get())

        engine.addEntityListener(allOf(Transform::class, Light::class).get(), lightsListener)
        engine.addEntityListener(allOf(World::class, WorldLight::class).get(), worldLightsListener)
    }

    override fun removedFromEngine(engine: Engine) {
        engine.removeEntityListener(lightsListener)
        engine.removeEntityListener(worldLightsListener)
    }

    override fun render(camera: Camera) {
        updateLightsPositions(camera)

        for (e in worldLights) {
            val worldLight = worldLight.get(e)

            if (!camera.shouldRenderVisualComponent(worldLight))
                continue

            val native = worldLight.native ?: continue

            native.setCombinedMatrix(camera.native)
            native.updateAndRender()
        }
    }

    private fun updateLightsPositions(camera: Camera) {
        for (e in lights) {
            val light = this.light.get(e)
            val transform = this.transform.get(e)
            if (!camera.shouldRenderVisualComponent(light))
                continue
            light.native?.setPosition(transform.x, transform.y)
        }
    }

    private fun initWorldLight(world: World, worldLight: WorldLight) {

        RayHandler.isDiffuse = true
        val fboDivisor = 8
        val rayHandler = RayHandler(
            world.native,
            Gdx.graphics.width / fboDivisor,
            Gdx.graphics.height / fboDivisor
        )
        rayHandler.setAmbientLight(worldLight.ambientLight)
        rayHandler.setAmbientLight(worldLight.ambientLightIntensity)
        rayHandler.setBlurNum(2)

        worldLight.world = world
        worldLight.native = rayHandler
    }

    private fun destroyWorldLight(worldLight: WorldLight) {
        worldLight.native?.dispose()
        worldLight.native = null
        worldLight.world = null
    }

    private fun initLight(light: Light, transform: Transform) {

        val world = light.world

        if (world == null) {
            Gdx.app.error("Light", "Missing world configuration in light $light")
            return
        }

        val worldLight = findWorldLight(world)

        if (worldLight == null) {
            Gdx.app.error("Light", "WorldLight not found for world ${light.world}")
            return
        }

        val native = PointLight(worldLight.native, light.rays, light.color, light.distance, transform.x, transform.y)
        light.native = native
    }

    private fun findWorldLight(world: World): WorldLight? {
        return worldLights.map { e -> worldLight.get(e) }.find { worldLight -> worldLight.world == world }
    }

    private fun destroyLight(light: Light) {
        light.native?.remove(true)
        light.native = null
    }

    inner class WorldLightsListener : EntityListener {

        private val world = mapperFor<World>()
        private val worldLight = mapperFor<WorldLight>()

        override fun entityAdded(entity: Entity) {
            val world = this.world.get(entity)
            val worldLight = this.worldLight.get(entity)
            initWorldLight(world, worldLight)
        }

        override fun entityRemoved(entity: Entity) {
            val worldLight = this.worldLight.get(entity)
            destroyWorldLight(worldLight)
        }
    }

    inner class LightsListener : EntityListener {

        private val transform = mapperFor<Transform>()
        private val light = mapperFor<Light>()

        override fun entityAdded(entity: Entity) {
            val light = this.light.get(entity)
            val transform = this.transform.get(entity)

            initLight(light, transform)
        }

        override fun entityRemoved(entity: Entity) {
            val light = this.light.get(entity)
            destroyLight(light)
        }
    }
}