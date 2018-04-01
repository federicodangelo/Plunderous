package com.fangelo.libraries.input

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector2

object InputHandler : GestureDetector.GestureListener, InputProcessor {
    private var trackingPointer = -1

    //Fling
    override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
        return false
    }

    //Pan
    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
        return false
    }

    override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return false
    }


    //Tap
    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        return false
    }

    override fun longPress(x: Float, y: Float): Boolean {
        return false
    }

    //Touch
    override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (trackingPointer == -1) {
            trackingPointer = pointer
            InputInfo.startTouching(screenX, screenY)
            return true
        }
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (pointer == trackingPointer) {
            InputInfo.updateTouching(screenX, screenY)
            return true
        }
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (pointer == trackingPointer) {
            InputInfo.stopTouching(screenX, screenY)
            trackingPointer = -1
            return true
        }
        return false
    }

    //Mouse
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        InputInfo.scrolling(amount)
        return false
    }

    //Keyboard
    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    //Pinch
    override fun pinch(initialPointer1: Vector2, initialPointer2: Vector2, pointer1: Vector2, pointer2: Vector2): Boolean {
        if (!InputInfo.zooming) {
            InputInfo.startZooming()
            return true
        }
        return false
    }

    override fun zoom(initialDistance: Float, distance: Float): Boolean {
        if (InputInfo.zooming) {
            InputInfo.updateZooming(initialDistance, distance)
            return true
        }
        return false
    }

    override fun pinchStop() {
        InputInfo.stopZooming()
    }
}