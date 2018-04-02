package com.fangelo.libraries.sprite

import com.fangelo.libraries.render.VisualComponent

class VisualSprite(sprite: Sprite? = null) : VisualComponent() {

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

}