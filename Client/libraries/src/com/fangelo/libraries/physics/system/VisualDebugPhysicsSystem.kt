package com.fangelo.libraries.physics.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.physics.component.VisualWorld
import com.fangelo.libraries.physics.component.World
import com.fangelo.libraries.render.system.VisualCameraRenderer
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class VisualDebugPhysicsSystem : VisualCameraRenderer() {
    private lateinit var debugRenderer: Box2DDebugRenderer
    private lateinit var worlds: ImmutableArray<Entity>
    private val world = mapperFor<World>()
    private val visualWorld = mapperFor<VisualWorld>()

    override fun addedToEngine(engine: Engine) {
        debugRenderer = Box2DDebugRenderer()
        worlds = engine.getEntitiesFor(allOf(World::class, VisualWorld::class).get())
    }

    override fun removedFromEngine(engine: Engine) {
        debugRenderer.dispose()
    }

    override fun render(camera: Camera) {
        for (entity in worlds) {
            val world = world.get(entity)
            var visualWorld = visualWorld.get(entity)

            if (camera.shouldRenderVisualComponent(visualWorld))
                drawDebug(world, camera)
        }
    }

    private fun drawDebug(world: World, camera: Camera) {
        if (world.native != null)
            debugRenderer.render(world.native, camera.combined)
    }
}