package com.fangelo.libraries.ashley.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.fangelo.libraries.ashley.components.Camera
import ktx.ashley.allOf
import ktx.ashley.mapperFor


class VisualDebugPhysicsSystem : EntitySystem() {

    private var debugRenderer: Box2DDebugRenderer
    private lateinit var cameras: ImmutableArray<Entity>
    private val camera = mapperFor<Camera>()
    private var world: World? = null

    init {
        debugRenderer = Box2DDebugRenderer()
        //debugRenderer.isDrawVelocities = true
    }

    override fun addedToEngine(engine: Engine) {
        cameras = engine.getEntitiesFor(allOf(Camera::class).get())
        world = engine.getSystem(PhysicsSystem::class.java).world
    }

    override fun removedFromEngine(engine: Engine) {
    }

    override fun update(deltaTime: Float) {
        var camera: Camera
        for (ec in cameras) {
            camera = this.camera.get(ec)
            drawDebug(camera)
        }
    }

    private fun drawDebug(camera: Camera) {
        if (world != null)
            debugRenderer.render(world, camera.combined)
    }
}