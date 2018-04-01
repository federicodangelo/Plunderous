package com.fangelo.libraries.input

object InputInfo {

    var zooming = false
        private set

    var zoomingJustStarted = false
        private set
    var zoomingInitialDistance = 0f
        private set
    var zoomingDistance = 0f
        private set

    var scrolling = false
        private set
    var scrollingAmount = 0
        private set

    var touching = false
        private set
    var touchingX = 0
        private set
    var touchingY = 0
        private set
    var touchingTime = 0f
        private set

    internal fun startTouching(screenX: Int, screenY: Int) {
        touching = true
        touchingTime = 0f
        touchingX = screenX
        touchingY = screenY
    }

    internal fun updateTouching(screenX: Int, screenY: Int) {
        touchingX = screenX
        touchingY = screenY
    }

    internal fun stopTouching(screenX: Int, screenY: Int) {
        touchingX = screenX
        touchingY = screenY
        touching = false
    }

    internal fun scrolling(amount: Int) {
        scrolling = true
        scrollingAmount += amount
    }

    internal fun startZooming() {
        zooming = true
        zoomingJustStarted = true
    }

    internal fun updateZooming(initialDistance: Float, distance: Float) {
        zoomingInitialDistance = initialDistance
        zoomingDistance = distance
    }

    internal fun stopZooming() {
        zooming = false
    }

    internal fun update(deltaTime: Float) {
        zoomingJustStarted = false
        scrolling = false
        scrollingAmount = 0
        if (touching)
            touchingTime += deltaTime
    }
}