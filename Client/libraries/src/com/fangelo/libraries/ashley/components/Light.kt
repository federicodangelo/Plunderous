package com.fangelo.libraries.ashley.components

import box2dLight.PointLight
import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color

class Light(val rays: Int = 256) : Component {

    var color: Color = Color.BLACK
        set(value) {
            field = value
            native?.color = value
        }

    var distance: Float = 1.0f
        set(value) {
            field = value
            native?.distance = value
        }

    var native: PointLight? = null
}