package com.beust.app

import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

class TextScreenPanel: JPanel() {
    private val content = Array(WIDTH * HEIGHT) { 0x20 }

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

    fun drawCharacter(x: Int, y: Int, value: Int) {
        if (value in 0xa0..0xfe) {
            content[y * WIDTH + x] = value and 0x7f
            repaint()
        }
    }

}

class LineCalculator {
    private val lineMap = hashMapOf<Int, Int>()

    init {
        var line = 0
        listOf(0, 0x28, 0x50).forEach { m ->
            repeat(8) {
                val address = 0x400 + it * 0x80 + m
//                println("Address: " + address.hh() + " line: $line")
                lineMap[address] = line++
            }
        }
        val l = lineFor(0x7d1)
        ""
    }

    private fun lineFor(location: Int): Pair<Int, Int>? {
        val result = lineMap.filter { (k, v) ->
            location in k until k + TextScreenPanel.WIDTH
        }
        return if (result.isEmpty()) null else  result.iterator().next().let { it.key to it.value }
    }

    fun coordinatesFor(location: Int): Pair<Int, Int>?  {
        val p = lineFor(location)
        return if (p != null) {
            val y = p.second
            val x = (location - p.first) % TextScreenPanel.WIDTH
            x to y
        } else {
            null

        }
    }
}
