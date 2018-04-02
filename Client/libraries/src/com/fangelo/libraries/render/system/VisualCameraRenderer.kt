package com.fangelo.libraries.render.system

import com.badlogic.ashley.core.Engine
import com.fangelo.libraries.camera.component.Camera


abstract class VisualCameraRenderer {

    var enabled = true

    open fun addedToEngine(engine: Engine) {
    }

    open fun removedFromEngine(engine: Engine) {
    }

    abstract fun render(camera: Camera)
}