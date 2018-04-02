package com.fangelo.libraries.tilemap

import com.badlogic.gdx.graphics.g2d.TextureRegion

class Tileset(val tilesTextures: Array<TextureRegion>) {
    fun getTileTexture(tile: Int): TextureRegion = tilesTextures[tile]
}