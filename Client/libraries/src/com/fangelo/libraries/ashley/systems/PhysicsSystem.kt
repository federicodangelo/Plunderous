package com.fangelo.libraries.ashley.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.fangelo.libraries.ashley.components.Rigidbody
import com.fangelo.libraries.ashley.components.Transform
import com.fangelo.libraries.ashley.components.World
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import ktx.box2d.createWorld

class PhysicsSystem : EntitySystem() {

    private val rigidbodyListener = RigidbodyListener()
    private val worldListener = WorldListener()

    override fun addedToEngine(engine: Engine) {
        engine.addEntityListener(allOf(Transform::class, Rigidbody::class).get(), rigidbodyListener)
        engine.addEntityListener(allOf(World::class).get(), worldListener)
    }

    override fun removedFromEngine(engine: Engine) {
        engine.removeEntityListener(rigidbodyListener)
        engine.removeEntityListener(worldListener)
    }

    private fun initWorld(world: World) {
        world.native = createWorld(gravity = Vector2())
    }

    private fun destroyWorld(world: World) {

        if (!world.isEmpty)
            Gdx.app.error("World", "Destroying non-empty world, bad things can happen!!")

        world.native?.dispose()
        world.native = null
    }

    inner class RigidbodyListener : EntityListener {
        private val transform = mapperFor<Transform>()
        private val rigidbody = mapperFor<Rigidbody>()

        override fun entityAdded(entity: Entity) {
            val rigidbody = this.rigidbody.get(entity)
            val transform = this.transform.get(entity)

            val world = rigidbody.world

            if (world == null) {
                Gdx.app.error("World", "Missing rigidbody world configuration in entity $entity")
                return
            }

            world.addRigidbody(rigidbody, transform, entity)
        }

        override fun entityRemoved(entity: Entity) {
            val rigidbody = this.rigidbody.get(entity)
            rigidbody.world?.removeRigidbody(rigidbody, entity)
        }
    }

    inner class WorldListener : EntityListener {
        private val world = mapperFor<World>()

        override fun entityAdded(entity: Entity) {
            initWorld(world.get(entity))
        }

        override fun entityRemoved(entity: Entity) {
            destroyWorld(world.get(entity))
        }
    }
}