package com.fangelo.libraries.light.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.utils.ImmutableArray
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.light.component.Light
import com.fangelo.libraries.light.component.WorldLight
import com.fangelo.libraries.physics.component.World
import com.fangelo.libraries.render.system.VisualCameraRenderer
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

    inner class WorldLightsListener : EntityListener {

        private val world = mapperFor<World>()
        private val worldLight = mapperFor<WorldLight>()

        override fun entityAdded(entity: Entity) {
            val world = this.world.get(entity)
            val worldLight = this.worldLight.get(entity)
            worldLight.initNative(world)
        }

        override fun entityRemoved(entity: Entity) {
            val worldLight = this.worldLight.get(entity)
            worldLight.destroyNative()
        }
    }

    inner class LightsListener : EntityListener {

        private val transform = mapperFor<Transform>()
        private val light = mapperFor<Light>()

        override fun entityAdded(entity: Entity) {
            val light = this.light.get(entity)
            val transform = this.transform.get(entity)

            light.initNative(transform)
        }

        override fun entityRemoved(entity: Entity) {
            val light = this.light.get(entity)
            light.destroyNative()
        }
    }
}