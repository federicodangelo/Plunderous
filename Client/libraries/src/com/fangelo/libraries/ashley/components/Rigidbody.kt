package com.fangelo.libraries.ashley.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import ktx.box2d.BodyDefinition

class Rigidbody(var definition: BodyDefinition? = null) : Component {
    var body: Body? = null

    fun set(definition: BodyDefinition) {
        this.definition = definition
    }
}