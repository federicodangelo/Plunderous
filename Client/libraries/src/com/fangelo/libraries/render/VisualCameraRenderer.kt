package com.fangelo.libraries.render

import com.badlogic.ashley.core.Engine
import com.fangelo.libraries.camera.Camera


abstract class VisualCameraRenderer {

    var enabled = true

    open fun addedToEngine(engine: Engine) {
    }

    open fun removedFromEngine(engine: Engine) {
    }

    abstract fun render(camera: Camera)
}