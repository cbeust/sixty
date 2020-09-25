package com.beust.app.swing

import com.beust.app.LineCalculator
import com.beust.app.app.ITextScreen
import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

class SwingTextScreenScreen: ITextScreen, JPanel() {
    private val content = Array(SwingTextScreenScreen.WIDTH * SwingTextScreenScreen.HEIGHT) { 0x20 }

    companion object {
        val WIDTH = 40
        val HEIGHT = 24
        private val fontWidth = 10
        private val fontHeight = 10
        private val gap = 2
        val fullWidth = (fontWidth + gap) * WIDTH + 20
        val fullHeight = (fontHeight + gap) * HEIGHT + 20
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
        g.fillRect(0, 0, width, height)
        g.color = Color.green
        repeat(HEIGHT) { y ->
            repeat(WIDTH) { x ->
                val xx = x * (fontWidth + gap)
                val yy = y * (fontHeight + gap) + 15
                val c = content[y * WIDTH + x].toChar().toString()
//                println("Drawing at ($x, $y) ($xx,$yy): $character")
                g.drawString(c, xx, yy)
            }
        }
    }

    override fun drawCharacter(x: Int, y: Int, value: Int) {
        if (value in 0xa0..0xfe) {
            content[y * WIDTH + x] = value and 0x7f
            repaint()
        }
    }

}