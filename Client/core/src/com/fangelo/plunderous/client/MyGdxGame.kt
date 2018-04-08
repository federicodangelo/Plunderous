package com.fangelo.plunderous.client

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.input.GestureDetector
import com.fangelo.libraries.input.InputHandler
import com.fangelo.libraries.input.UIInputHandler
import com.fangelo.libraries.ui.ScreenManager
import com.fangelo.plunderous.client.ui.ExitInputHandler
import com.fangelo.plunderous.client.ui.screen.mainmenu.MainMenuScreen
import ktx.app.KtxApplicationAdapter

private const val REF_HEIGHT_IN_PIXELS = 1366

class MyGdxGame : KtxApplicationAdapter {

    override fun create() {
        initInput()
        showFirstScreen()
    }

    private fun showFirstScreen() {
        ScreenManager.show(MainMenuScreen())
    }

    private fun initInput() {
        Gdx.input.isCatchBackKey = true
        Gdx.input.inputProcessor = buildInputProcessor()
    }

    private fun buildInputProcessor(): InputProcessor {
        val inputMultiplexer = InputMultiplexer()

        inputMultiplexer.addProcessor(ScreenManager.stage)
        inputMultiplexer.addProcessor(GestureDetector(InputHandler))
        inputMultiplexer.addProcessor(InputHandler)
        inputMultiplexer.addProcessor(UIInputHandler())
        inputMultiplexer.addProcessor(ExitInputHandler())

        return inputMultiplexer
    }

    override fun resize(width: Int, height: Int) {

        val screenScale = REF_HEIGHT_IN_PIXELS.toFloat() / height.toFloat()

        ScreenManager.resize(width, height, screenScale)

        Context.activeGame?.resize(width, height)
    }

    override fun render() {
        clearScreen()

        drawGame()

        drawUI()
    }

    private fun drawUI() {
        ScreenManager.updateAndDraw()
    }

    private fun drawGame() {
        Context.activeGame?.update(Gdx.graphics.deltaTime)
    }

    private fun clearScreen() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }

    override fun dispose() {
        ScreenManager.dispose()
        PlatformAdapter.dispose()
    }
}
