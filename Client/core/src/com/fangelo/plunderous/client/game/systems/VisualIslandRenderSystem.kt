package com.fangelo.plunderous.client.game.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.fangelo.libraries.ashley.components.Camera
import com.fangelo.libraries.ashley.components.Transform
import com.fangelo.plunderous.client.game.components.island.Island
import com.fangelo.plunderous.client.game.components.island.VisualIsland
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class VisualIslandRenderSystem : EntitySystem() {
    private lateinit var entities: ImmutableArray<Entity>
    private lateinit var cameras: ImmutableArray<Entity>

    private val camera = mapperFor<Camera>()
    private val transform = mapperFor<Transform>()
    private val island = mapperFor<Island>()
    private val visualIsland = mapperFor<VisualIsland>()

    private lateinit var shapeRenderer: ShapeRenderer

    override fun addedToEngine(engine: Engine) {
        shapeRenderer = ShapeRenderer()
        entities = engine.getEntitiesFor(allOf(Transform::class, Island::class, VisualIsland::class).get())
        cameras = engine.getEntitiesFor(allOf(Camera::class).get())
    }

    override fun removedFromEngine(engine: Engine) {
        shapeRenderer.dispose()
    }

    override fun update(deltaTime: Float) {
        var camera: Camera
        for (ec in cameras) {
            camera = this.camera.get(ec)
            beginRender(camera)
            drawEntities()
            endRender()
        }
    }

    private fun drawEntities() {
        var transform: Transform
        var visualIsland: VisualIsland
        var island: Island

        for (e in entities) {

            transform = this.transform.get(e)
            island = this.island.get(e)
            visualIsland = this.visualIsland.get(e)

            shapeRenderer.color = visualIsland.color
            shapeRenderer.circle(transform.x, transform.y, island.radius, 20)
        }
    }

    private fun beginRender(camera: Camera) {
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
    }

    private fun endRender() {
        shapeRenderer.end()
    }
}