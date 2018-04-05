package com.fangelo.libraries.light.component

import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Pool
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.physics.component.World
import com.fangelo.libraries.render.component.VisualComponent

class WorldLight : VisualComponent(), Pool.Poolable {
    var ambientLight: Color = Color.BLACK
        set(value) {
            field = value
            field.a = ambientLightIntensity
            native?.setAmbientLight(value)
        }

    var ambientLightIntensity: Float = 0.0f
        set(value) {
            field = value
            native?.setAmbientLight(value)
        }

    internal var world: World? = null
    internal var native: RayHandler? = null


    internal fun initNative(world: World) {

        RayHandler.isDiffuse = true
        val fboDivisor = 8
        val rayHandler = RayHandler(
            world.native,
            Gdx.graphics.width / fboDivisor,
            Gdx.graphics.height / fboDivisor
        )
        rayHandler.setAmbientLight(ambientLight)
        rayHandler.setAmbientLight(ambientLightIntensity)
        rayHandler.setBlurNum(2)

        this.world = world
        this.native = rayHandler
        this.world?.worldLight = this
    }

    internal fun destroyNative() {
        this.native?.dispose()
        this.native = null
        this.world?.worldLight = null
        this.world = null
    }

    internal fun renderNative(camera: Camera) {
        val native = this.native ?: return
        native.setCombinedMatrix(camera.native)
        native.updateAndRender()
    }

    override fun reset() {
        world = null
        native = null
    }
}
