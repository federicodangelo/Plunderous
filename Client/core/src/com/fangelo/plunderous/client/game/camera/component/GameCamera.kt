package com.fangelo.plunderous.client.game.camera.component

import com.badlogic.ashley.core.Component

class GameCamera : Component {
    var startingZoom = 0f
    var state: GameCameraState = GameCameraState.FollowingShip
}