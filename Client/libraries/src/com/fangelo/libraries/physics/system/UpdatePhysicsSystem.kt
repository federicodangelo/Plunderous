package com.fangelo.libraries.physics.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.fangelo.libraries.physics.component.World
import ktx.ashley.allOf
import ktx.ashley.mapperFor


class UpdatePhysicsSystem : IteratingSystem(allOf(World::class).get()) {

    private val world = mapperFor<World>()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val world = this.world.get(entity)
        world.updatePhysics(deltaTime)
    }
}