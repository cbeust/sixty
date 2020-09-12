package com.beust.app

import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

class Board(val w: Int, val h: Int) {
    private val DEFAULT = Color.white
    private val blockWidth = 6
    private val blockHeight = 3
    private val gap = 0
    private val WIDTH = 280
    private val HEIGHT = 192
    private val content = Array<Color>(WIDTH * HEIGHT) { DEFAULT }

    init {
        println("BOARD")
    }
    private fun index(x: Int, y: Int) = y * WIDTH + x

    fun redraw(g: Graphics) {
        repeat(HEIGHT) { y ->
            repeat(WIDTH) { x ->
                g.color = content[index(x, y)]
                g.fillRect(x, y, blockWidth, blockHeight)
                if (x == 0 && y == 3) {
                    println("PAINTING " + g.color + " " + this)
                }
            }
        }
    }

    fun draw(x: Int, y: Int, color: Color) {
        if (x == 0 && y == 3) {
            println("0-3: " + color + " " + this)
        }
        content[index(x, y)] = color
    }
}