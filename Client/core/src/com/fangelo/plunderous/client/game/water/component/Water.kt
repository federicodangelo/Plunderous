package com.fangelo.plunderous.client.game.water.component

import com.badlogic.ashley.core.Component
import com.fangelo.plunderous.client.game.water.utils.OpenSimplexNoiseFloat

class Water : Component {

    private val noise = OpenSimplexNoiseFloat(31415)
    private val frequency = 37.0f

    fun getWaterDepth(mapX: Int, mapY: Int): Float {

        val centerX = mapX - Int.MAX_VALUE / 2
        val centerY = mapY - Int.MAX_VALUE / 2

        //val add = (mapX.toDouble() + mapY.toDouble()) * 0.05
        //return Math.cos(add).toFloat() * 0.5f + 0.5f

        val noise = noise.eval(centerX.toFloat() / frequency, centerY.toFloat() / frequency)

        val depth = noise.toFloat() * 0.5f + 0.5f

        return depth
    }
}