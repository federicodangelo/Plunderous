package com.fangelo.libraries.ashley.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.physics.box2d.World

class PhysicsSystem(var world: World) : EntitySystem()