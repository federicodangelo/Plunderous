package com.fangelo.plunderous.client.game.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.fangelo.libraries.camera.Camera
import com.fangelo.libraries.transform.Transform
import com.fangelo.libraries.render.VisualCameraRenderer
import com.fangelo.plunderous.client.game.components.island.Island
import com.fangelo.plunderous.client.game.components.island.VisualIsland
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class VisualIslandRenderSystem : VisualCameraRenderer() {
    private lateinit var entities: ImmutableArray<Entity>

    private val transform = mapperFor<Transform>()
    private val island = mapperFor<Island>()
    private val visualIsland = mapperFor<VisualIsland>()

    private lateinit var shapeRenderer: ShapeRenderer

    override fun addedToEngine(engine: Engine) {
        shapeRenderer = ShapeRenderer()
        entities = engine.getEntitiesFor(allOf(Transform::class, Island::class, VisualIsland::class).get())
    }

    override fun removedFromEngine(engine: Engine) {
        shapeRenderer.dispose()
    }

    override fun render(camera: Camera) {
        beginRender(camera)
        drawEntities(camera)
        endRender()
    }

    private fun drawEntities(camera: Camera) {
        var transform: Transform
        var visualIsland: VisualIsland
        var island: Island

        for (e in entities) {

            transform = this.transform.get(e)
            island = this.island.get(e)
            visualIsland = this.visualIsland.get(e)

            if (!camera.shouldRenderVisualComponent(visualIsland))
                continue

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