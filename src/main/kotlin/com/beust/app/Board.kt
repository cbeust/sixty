package com.beust.app

import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

class Board(val w: Int, val h: Int): JPanel() {
    private val blockWidth = 6
    private val blockHeight = 3
    private val gap = 0
    private val WIDTH = 280
    private val HEIGHT = 192
    private val content = ArrayList<Color>(WIDTH * HEIGHT)

    init {
        setBounds(0, 0, w, h)
    }

    private fun index(x: Int, y: Int) = y * WIDTH + x

    fun redraw(g: Graphics) {
        repeat(HEIGHT) { y ->
            repeat(WIDTH) { x ->
                g.color = content[index(x, y)]
                g.fillRect(x, y, blockWidth, blockHeight)
            }
        }
    }

    fun draw(x: Int, y: Int, color: Color) {
        content[index(x, y)] = color
    }
}