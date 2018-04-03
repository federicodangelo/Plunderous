package com.fangelo.libraries.physics.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.fangelo.libraries.physics.component.Rigidbody
import com.fangelo.libraries.physics.component.World
import com.fangelo.libraries.transform.Transform
import ktx.ashley.allOf
import ktx.ashley.mapperFor

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

    inner class RigidbodyListener : EntityListener {
        private val transform = mapperFor<Transform>()
        private val rigidbody = mapperFor<Rigidbody>()

        override fun entityAdded(entity: Entity) {
            val rigidbody = this.rigidbody.get(entity)
            val transform = this.transform.get(entity)

            val world = rigidbody.world

            if (world == null) {
                Gdx.app.error("World", "Missing rigidbody world configuration")
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
            world.get(entity).initNative()
        }

        override fun entityRemoved(entity: Entity) {
            world.get(entity).destroyNative()
        }
    }
}