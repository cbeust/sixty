package com.beust.app

import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

class Board {
    private val DEFAULT = Color.black
    private val blockWidth = 3
    private val blockHeight = 3
    private val gap = 0
    private val WIDTH = 280
    private val HEIGHT = 192
    private val content = Array<Color>(WIDTH * HEIGHT) { DEFAULT }

    private fun index(x: Int, y: Int) = y * WIDTH + x

    fun redraw(g: Graphics) {
        repeat(HEIGHT) { y ->
            repeat(WIDTH) { x ->
                g.color = content[index(x, y)]
                val xx = x * (blockWidth + gap)
                val yy = y * (blockHeight + gap)
                g.fillRect(xx, yy, blockWidth, blockHeight)
            }
        }
    }

    fun draw(x: Int, y: Int, color: Color) {
        if (index(x,y) >= WIDTH*HEIGHT) {
            println("ERROR")
        }
        content[index(x, y)] = color
    }
}