package com.fangelo.plunderous.client.game.ship.system

interface ShipInputProvider {
    fun getShipTargetSpeed(): Float
    fun getShipTargetRudderRotation(): Float
}