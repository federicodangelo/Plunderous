package com.fangelo.plunderous.client.game.water.component

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Pool
import com.fangelo.libraries.render.component.VisualComponent

class VisualWater : VisualComponent(), Pool.Poolable {
    var waterTileTexture: TextureRegion? = null

    override fun reset() {
        waterTileTexture = null
    }
}