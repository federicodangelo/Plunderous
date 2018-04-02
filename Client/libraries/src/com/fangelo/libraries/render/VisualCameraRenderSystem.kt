package com.fangelo.libraries.render

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.fangelo.libraries.camera.Camera
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class VisualCameraRenderSystem : IteratingSystem(allOf(Camera::class).get()) {

    private val camera = mapperFor<Camera>()
    private val renderers = mutableListOf<VisualCameraRenderer>()

    fun <T : VisualCameraRenderer> addRenderer(renderer: T): T {
        renderers.add(renderer)
        if (engine != null)
            renderer.addedToEngine(engine)
        return renderer
    }

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        for (renderer in renderers)
            renderer.addedToEngine(engine)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        for (renderer in renderers)
            renderer.removedFromEngine(engine)
    }

    public override fun processEntity(entity: Entity, deltaTime: Float) {
        val camera = camera.get(entity)

        if (!camera.enabled)
            return

        for (renderer in renderers)
            if (renderer.enabled)
                renderer.render(camera)
    }
}