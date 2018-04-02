package com.fangelo.libraries.tilemap.component

import com.fangelo.libraries.render.component.VisualComponent
import com.fangelo.libraries.tilemap.Tileset

class Tilemap(width: Int = 0, height: Int = 0, tiles: Array<Int> = arrayOf(), tileset: Tileset? = null) : VisualComponent() {
    var width: Int = width
        private set

    var height: Int = height
        private set

    var tiles: Array<Int> = tiles
        private set

    var tileset: Tileset? = tileset
        private set

    fun getTile(x: Int, y: Int) = tiles[y * width + x]

    fun set(width: Int, height: Int, tiles: Array<Int>, tileset: Tileset): Tilemap {
        this.width = width
        this.height = height
        this.tiles = tiles
        this.tileset = tileset
        if (tiles.size != width * height)
            throw Exception("Invalid tiles array size, it's ${tiles.size} and should be ${width * height}")

        return this
    }

    init {
        if (tiles.size != width * height)
            throw Exception("Invalid tiles array size, it's ${tiles.size} and should be ${width * height}")
    }
}