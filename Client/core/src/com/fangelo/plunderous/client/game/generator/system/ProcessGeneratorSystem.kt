package com.fangelo.plunderous.client.game.generator.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.ashley.utils.ImmutableArray
import com.fangelo.plunderous.client.game.generator.component.Generator
import com.fangelo.plunderous.client.game.generator.component.GeneratorAreaSource
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class ProcessGeneratorSystem : IteratingSystem(allOf(Generator::class).get()) {
    private val generator = mapperFor<Generator>()
    private val generatorAreaSource = mapperFor<GeneratorAreaSource>()

    private lateinit var areaSources : ImmutableArray<Entity>

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        areaSources = engine.getEntitiesFor(allOf(GeneratorAreaSource::class).get())
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val generator = generator.get(entity)
        generate(generator, areaSources.map { e -> generatorAreaSource.get(e) } )
    }

    private fun generate(generator: Generator, areaSources: List<GeneratorAreaSource>) {
        generator.generate(areaSources)
    }
}