package com.fangelo.plunderous.client.game.ship.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.fangelo.plunderous.client.game.ship.component.MainShip
import com.fangelo.plunderous.client.game.ship.component.Ship
import com.fangelo.plunderous.client.game.ship.component.ShipInput
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class UpdateMainShipInputSystem : IteratingSystem {

    private val shipInputProvider: ShipInputProvider
    private val shipInput = mapperFor<ShipInput>()

    constructor(shipInputProvider: ShipInputProvider) : super(allOf(Ship::class, ShipInput::class, MainShip::class).get()) {
        this.shipInputProvider = shipInputProvider
    }

    public override fun processEntity(entity: Entity, deltaTime: Float) {
        val shipInput = shipInput.get(entity)
        updateFromInputProvider(shipInput)
    }

    private fun updateFromInputProvider(shipInput: ShipInput) {
        shipInput.targetSpeed = shipInputProvider.getShipTargetSpeed()
        shipInput.targetRudderRotation = shipInputProvider.getShipTargetRudderRotation()
    }
}