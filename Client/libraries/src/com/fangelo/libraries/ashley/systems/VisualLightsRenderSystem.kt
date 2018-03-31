package com.fangelo.libraries.ashley.systems

import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.fangelo.libraries.ashley.components.Camera
import com.fangelo.libraries.ashley.components.Light
import com.fangelo.libraries.ashley.components.Transform
import com.fangelo.libraries.ashley.components.World
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class VisualLightsRenderSystem : EntitySystem() {

    private lateinit var cameras: ImmutableArray<Entity>
    private lateinit var lights: ImmutableArray<Entity>

    private val rayHandlers = HashMap<World, RayHandler>()

    private val camera = mapperFor<Camera>()
    private val light = mapperFor<Light>()
    private val transform = mapperFor<Transform>()

    private val worldsListener = WorldsListener()
    private val lightsListener = LightsListener()

    var ambientLight: Color = Color.BLACK
        set(value) {
            field = value
            field.a = ambientLightIntensity

            rayHandlers.values.forEach { ray -> ray.setAmbientLight(value) }
        }

    var ambientLightIntensity: Float = 0.0f
        set(value) {
            field = value
            rayHandlers.values.forEach { ray -> ray.setAmbientLight(value) }
        }

    override fun addedToEngine(engine: Engine) {
        cameras = engine.getEntitiesFor(allOf(Camera::class).get())
        lights = engine.getEntitiesFor(allOf(Transform::class, Light::class).get())

        engine.addEntityListener(allOf(Transform::class, Light::class).get(), lightsListener)
        engine.addEntityListener(allOf(World::class).get(), worldsListener)
    }

    override fun removedFromEngine(engine: Engine) {
        engine.removeEntityListener(lightsListener)
        engine.removeEntityListener(worldsListener)
    }

    override fun update(deltaTime: Float) {
        updateLightsPositions()

        for (rayHandler in rayHandlers.values) {
            var camera: Camera
            for (ec in cameras) {
                camera = this.camera.get(ec)
                rayHandler.setCombinedMatrix(camera.native)
                rayHandler.updateAndRender()
            }
        }
    }

    private fun updateLightsPositions() {
        for (e in lights) {
            val light = this.light.get(e)
            val transform = this.transform.get(e)
            light.native?.setPosition(transform.x, transform.y)
        }
    }

    private fun addRayHandler(world: World) {

        RayHandler.isDiffuse = true
        val fboDivisor = 8
        val rayHandler = RayHandler(
            world.native,
            Gdx.graphics.width / fboDivisor,
            Gdx.graphics.height / fboDivisor
        )
        rayHandler.setAmbientLight(ambientLight)
        rayHandler.setAmbientLight(ambientLightIntensity)
        rayHandler.setBlurNum(2)

        rayHandlers[world] = rayHandler
    }

    private fun removeRayHandler(world: World) {
        val rayHandler = rayHandlers.remove(world)
        rayHandler?.dispose()
    }

    private fun initLight(light: Light, transform: Transform) {

        val world = light.world

        if (world == null) {
            Gdx.app.error("Light", "Missing world configuration in light $light")
            return
        }

        val rayHandler = rayHandlers[world]

        if (rayHandler == null) {
            Gdx.app.error("Light", "Rayhandler not initialized for world ${light.world}")
            return
        }

        val native = PointLight(rayHandler, light.rays, light.color, light.distance, transform.x, transform.y)
        light.native = native
    }

    private fun destroyLight(light: Light) {
        light.native?.remove(true)
        light.native = null
    }

    inner class WorldsListener : EntityListener {

        private val world = mapperFor<World>()

        override fun entityAdded(entity: Entity) {
            val world = this.world.get(entity)
            addRayHandler(world)
        }

        override fun entityRemoved(entity: Entity) {
            val world = this.world.get(entity)
            removeRayHandler(world)
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