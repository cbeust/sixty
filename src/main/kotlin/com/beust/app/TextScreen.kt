package com.beust.app

import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color

class TextScreen(private val canvas: Canvas) {
    private val width = 40
    private val height = 24
    private val fontWidth = 10
    private val fontHeight = 10
    private val gap = 5
    private val fullWidth = (fontWidth + gap) * width + 40
    private val fullHeight = (fontHeight + gap) * height + 40

    init {
        with(canvas.graphicsContext2D) {
            fill = Color.BLACK
            fillRect(0.0, 0.0, fullWidth.toDouble(), fullHeight.toDouble())
        }
    }

    fun drawMemoryLocation(location: Int, value: Int) {
        val z = location - 0x400
        val x = z % width
        val y = z / width
        drawCharacter(x, y, value.toChar())
    }

    private fun drawCharacter(x: Int, y: Int, character: Char) {
        if (x < width && y < height) {
            val xx = x * (fontWidth + gap)
            val yy = y * (fontHeight + gap)
            with(canvas.graphicsContext2D) {
                fill = Color.WHITE
                fillText(character.toString(), xx.toDouble(), yy.toDouble())
            }
        }
    }
}