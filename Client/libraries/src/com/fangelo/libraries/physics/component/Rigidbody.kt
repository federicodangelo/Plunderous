package com.fangelo.libraries.physics.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.physics.box2d.Body
import ktx.box2d.BodyDefinition

class Rigidbody : Component {

    var definition: BodyDefinition? = null
    var world: World? = null
    var native: Body? = null

    fun set(world: World, definition: BodyDefinition) {
        this.world = world
        this.definition = definition
    }
}