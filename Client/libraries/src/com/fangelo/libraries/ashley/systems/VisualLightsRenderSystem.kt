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
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class VisualLightsRenderSystem : EntitySystem(), EntityListener {

    private lateinit var cameras: ImmutableArray<Entity>
    private lateinit var lights: ImmutableArray<Entity>
    private var rayHandler: RayHandler? = null

    private val camera = mapperFor<Camera>()

    private val transform = mapperFor<Transform>()
    private val light = mapperFor<Light>()

    var ambientLight: Color = Color.BLACK
        set(value) {
            field = value
            field.a = ambientLightIntensity
            rayHandler?.setAmbientLight(value)
        }

    var ambientLightIntensity: Float = 0.0f
        set(value) {
            field = value
            rayHandler?.setAmbientLight(value)
        }

    override fun addedToEngine(engine: Engine) {
        val world = engine.getSystem(PhysicsSystem::class.java).world
        RayHandler.isDiffuse = true
        val fboDivisor = 8
        val rayHandler = RayHandler(
            world,
            Gdx.graphics.width / fboDivisor,
            Gdx.graphics.height / fboDivisor
        )
        rayHandler.setAmbientLight(ambientLight)
        rayHandler.setAmbientLight(ambientLightIntensity)
        rayHandler.setBlurNum(2)

        this.rayHandler = rayHandler
        cameras = engine.getEntitiesFor(allOf(Camera::class).get())
        lights = engine.getEntitiesFor(allOf(Transform::class, Light::class).get())

        engine.addEntityListener(allOf(Transform::class, Light::class).get(), this)
    }

    override fun removedFromEngine(engine: Engine) {
        rayHandler?.dispose()
    }

    override fun update(deltaTime: Float) {
        val rayHandler = this.rayHandler ?: return

        updateLightsPositions()

        var camera: Camera
        for (ec in cameras) {
            camera = this.camera.get(ec)
            rayHandler.setCombinedMatrix(camera.native)
            rayHandler.updateAndRender()
        }
    }

    private fun updateLightsPositions() {
        for (e in lights) {
            val light = this.light.get(e)
            val transform = this.transform.get(e)

            light.native?.setPosition(transform.x, transform.y)
        }
    }

    override fun entityAdded(entity: Entity) {
        val light = this.light.get(entity)
        val transform = this.transform.get(entity)

        val native = PointLight(rayHandler, light.rays, light.color, light.distance, transform.x, transform.y)

        light.native = native
    }

    override fun entityRemoved(entity: Entity) {
        val light = this.light.get(entity)
        light.native?.remove(true)
        light.native = null
    }
}