package com.fangelo.plunderous.client.game.ship.component

import com.badlogic.ashley.core.Component

class Ship : Component {

    var maxForwardSpeed = 3.0f
    var maxBackwardSpeed = -0.3f
    var driveForce = 20f

    //Controlls steering
    var rudderRotation = 0f
    var rudderRotationSpeed = 5f
    var maxRudderRotation = 1f
    var maxRudderTorque = 0.5f

}