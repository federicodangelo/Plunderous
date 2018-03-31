package com.fangelo.libraries.ashley.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.fangelo.libraries.ashley.components.Camera
import com.fangelo.libraries.ashley.components.World
import ktx.ashley.allOf
import ktx.ashley.mapperFor


class VisualDebugPhysicsSystem : IteratingSystem(allOf(World::class).get()) {
    private lateinit var debugRenderer: Box2DDebugRenderer
    private lateinit var cameras: ImmutableArray<Entity>
    private val camera = mapperFor<Camera>()
    private val world = mapperFor<World>()

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        debugRenderer = Box2DDebugRenderer()
        cameras = engine.getEntitiesFor(allOf(Camera::class).get())
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        debugRenderer.dispose()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {

        val world = this.world.get(entity)

        var camera: Camera
        for (ec in cameras) {
            camera = this.camera.get(ec)
            drawDebug(world, camera)
        }
    }

    private fun drawDebug(world: World, camera: Camera) {
        if (world.native != null)
            debugRenderer.render(world.native, camera.combined)
    }
}