package com.fangelo.libraries.sprite.component

import com.badlogic.gdx.utils.Pool
import com.fangelo.libraries.render.component.VisualComponent
import com.fangelo.libraries.sprite.Sprite

class VisualSprite(sprite: Sprite? = null) : VisualComponent(), Pool.Poolable {
    var layer: Int = 0
    val sprites = mutableListOf<Sprite>()

    val mainSprite: Sprite?
        get() = if (!sprites.isEmpty()) sprites[0] else null

    init {
        if (sprite != null)
            sprites.add(sprite)
    }

    fun add(sprite: Sprite): VisualSprite {
        sprites.add(sprite)
        return this
    }

    override fun reset() {
        layer = 0
        sprites.clear()
    }
}