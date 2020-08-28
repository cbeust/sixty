package com.beust.app

import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color

class Board(val canvas: Canvas, val width: Int, val height: Int) {
    val blockWidth = 10.0
    val blockHeight = 2.0
    val gap = 0

    init {
        val fullWidth = (blockWidth + gap) * width
        val fullHeight = (blockHeight + gap) * height
        canvas.widthProperty().set(fullWidth)
        canvas.heightProperty().set(fullHeight)
        with(canvas.graphicsContext2D) {
            fill = Color.WHITE
            fillRect(0.0, 0.0, fullWidth, fullHeight)
        }
    }

    fun draw(x: Int, y: Int, color: Color) {
        val xx = x.toDouble() * (blockWidth + gap)
        val yy = y.toDouble() * (blockHeight + gap)
        with(canvas.graphicsContext2D) {
            fill = color
            fillRect(xx, yy, blockWidth, blockHeight)
        }
    }
}