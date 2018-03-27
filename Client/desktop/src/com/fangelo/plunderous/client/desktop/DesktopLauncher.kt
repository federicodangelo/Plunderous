package com.fangelo.plunderous.client.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.fangelo.plunderous.client.MyGdxGame

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration()

        //config.width = 1366 //landscape!
        //config.height = 768

        config.width = 768 //portrait!
        config.height = 1366
        config.forceExit = false //prevents error report by gradle after finishing running / debugging

        DesktopPlatformAdapter()
        LwjglApplication(MyGdxGame(), config)
    }
}
