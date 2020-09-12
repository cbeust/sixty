package com.beust.app

import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

class TextScreenPanel: JPanel() {
    data class DW(val string: String, val x: Int, val y: Int)
    private val content = Array(TextScreenPanel.width * TextScreenPanel.height) { 0x20 }
    private val fontWidth = 10
    private val fontHeight = 10
    private val gap = 2
    private val fullWidth = (fontWidth + gap) * TextScreenPanel.width + 40
    private val fullHeight = (fontHeight + gap) * TextScreenPanel.height + 40

    init {
        setBounds(0, 0, fullWidth, fullHeight)
    }

    companion object {
        val width = 40
        val height = 24
    }

    private val calculator = LineCalculator()

    fun drawMemoryLocation(location: Int, value: Int) {
        calculator.coordinatesFor(location)?.let { (x, y) ->
            drawCharacter(x, y, value)
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.color = Color.black
        g.fillRect(0, 0, fullWidth, fullHeight)
        g.color = Color.green
        repeat(TextScreenPanel.height) { y ->
            repeat(TextScreenPanel.width) { x ->
                val xx = x * (fontWidth + gap)
                val yy = y * (fontHeight + gap) + 15
                val c = content[y * TextScreenPanel.width + x].toChar().toString()
//                println("Drawing at ($x, $y) ($xx,$yy): $character")
                g.drawString(c, xx, yy)
            }
        }
    }

    fun drawCharacter(x: Int, y: Int, value: Int) {
        if (value in 0xa0..0xfe) {
            content[y * TextScreenPanel.width + x] = value and 0x7f
            repaint()
        }
    }

}