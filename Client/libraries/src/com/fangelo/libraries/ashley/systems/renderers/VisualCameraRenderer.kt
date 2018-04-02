package com.fangelo.libraries.ashley.systems.renderers

import com.badlogic.ashley.core.Engine
import com.fangelo.libraries.ashley.components.Camera


abstract class VisualCameraRenderer {

    var enabled: Boolean = true

    open fun addedToEngine(engine: Engine) {
    }

    open fun removedFromEngine(engine: Engine) {
    }

    abstract fun render(camera: Camera)
}