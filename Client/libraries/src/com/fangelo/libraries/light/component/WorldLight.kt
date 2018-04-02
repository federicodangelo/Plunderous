package com.fangelo.libraries.light.component

import box2dLight.RayHandler
import com.badlogic.gdx.graphics.Color
import com.fangelo.libraries.physics.component.World
import com.fangelo.libraries.render.component.VisualComponent

class WorldLight : VisualComponent() {

    var ambientLight: Color = Color.BLACK
        set(value) {
            field = value
            field.a = ambientLightIntensity
            native?.setAmbientLight(value)
        }

    var ambientLightIntensity: Float = 0.0f
        set(value) {
            field = value
            native?.setAmbientLight(value)
        }

    internal var world: World? = null
    internal var native: RayHandler? = null
}