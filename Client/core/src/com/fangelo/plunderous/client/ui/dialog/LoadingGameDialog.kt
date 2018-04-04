package com.fangelo.plunderous.client.ui.dialog

import com.fangelo.libraries.ui.Dialog
import com.fangelo.libraries.ui.DialogResult
import com.fangelo.libraries.ui.ScreenManager
import com.fangelo.plunderous.client.Context
import com.fangelo.plunderous.client.game.Game
import com.fangelo.plunderous.client.ui.screen.InGameScreen

class LoadingGameDialog : Dialog("Loading") {

    //private World world;
    private val hidding: Boolean = false
    private val waitFramesBeforeBlocking = 5

    init {

        text("Loading..")

        //this.world = world;
    }/*World world*/

    override fun onUpdate(deltaTime: Float) {
        //if (!hidding && world.getWorldGenerator().isTasksQueueEmpty()) {
        //	hidding = true;
        //	result(DialogResult.Ok);
        //	internalHide();
        //} else {
        //	waitFramesBeforeBlocking--;
        //	if (waitFramesBeforeBlocking < 0)
        //		world.getWorldGenerator().waitTasksQueueEmpty();
        //}

        finishLoadingGame()
    }


    private fun finishLoadingGame() {

        val inGameScreen = InGameScreen()

        Context.activeGame = Game(inGameScreen)

        ScreenManager.show(inGameScreen)

        close(DialogResult.Ok)
    }
}
