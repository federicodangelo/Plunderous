package com.fangelo.plunderous.client.game.components.ship

import com.badlogic.ashley.core.Component

class Ship : Component {
    var rotationAcc = 2f
    var maxRotationSpeed = 1f
    var rotationFric = 1.0f

    var moveAcc = 2f
    var maxMoveSpeed = 2f
}