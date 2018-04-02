package com.fangelo.libraries.ashley.systems.renderers

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.fangelo.libraries.ashley.components.Camera
import com.fangelo.libraries.ashley.components.World
import ktx.ashley.allOf
import ktx.ashley.mapperFor


class VisualDebugPhysicsSystem : VisualCameraRenderer() {
    private lateinit var debugRenderer: Box2DDebugRenderer
    private lateinit var worlds: ImmutableArray<Entity>
    private val world = mapperFor<World>()

    override fun addedToEngine(engine: Engine) {
        debugRenderer = Box2DDebugRenderer()
        worlds = engine.getEntitiesFor(allOf(World::class).get())
    }

    override fun removedFromEngine(engine: Engine) {
        debugRenderer.dispose()
    }

    override fun render(camera: Camera) {
        for (entity in worlds) {
            val world = world.get(entity)
            drawDebug(world, camera)
        }
    }

    private fun drawDebug(world: World, camera: Camera) {
        if (world.native != null)
            debugRenderer.render(world.native, camera.combined)
    }
}