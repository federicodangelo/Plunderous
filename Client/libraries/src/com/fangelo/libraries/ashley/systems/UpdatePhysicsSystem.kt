package com.fangelo.libraries.ashley.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.physics.box2d.World
import com.fangelo.libraries.ashley.components.Rigidbody
import com.fangelo.libraries.ashley.components.Transform
import ktx.ashley.allOf
import ktx.ashley.mapperFor


class UpdatePhysicsSystem : IteratingSystem(allOf(Rigidbody::class, Transform::class).get()) {

    private val maxStepTime = 1 / 45f

    private var accumulator = 0f
    private val bodiesQueue = mutableListOf<Entity>()

    private val transform = mapperFor<Transform>()
    private val rigidbody = mapperFor<Rigidbody>()
    private lateinit var world: World

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        world = engine.getSystem(PhysicsSystem::class.java).world
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        val frameTime = Math.min(deltaTime, 0.25f)
        accumulator += frameTime
        if (accumulator >= maxStepTime) {
            world.step(maxStepTime, 6, 2)
            accumulator -= maxStepTime

            //Entity Queue
            for (entity in bodiesQueue) {
                val tfm = transform.get(entity)
                val bodyComp = rigidbody.get(entity)
                val body = bodyComp.body
                if (body != null) {
                    val position = body.position
                    tfm.x = position.x
                    tfm.y = position.y
                    tfm.rotation = body.angle
                }
            }
        }

        bodiesQueue.clear()
    }

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        if (entity != null)
            bodiesQueue.add(entity)
    }
}