package com.fangelo.plunderous.client.game.components.ship

import com.badlogic.ashley.core.Component

class Ship : Component {

    var maxSpeed = 3.0f
    var driveForce = 30f

    //Controlls steering
    var rudderRotation = 0f
    var rudderRotationSpeed = 5f
    var maxRudderRotation = 1f
    var maxRudderTorque = 0.5f

}