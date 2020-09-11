package com.beust.app

import com.beust.sixty.hh
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color

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
            location in k until k + TextScreen.width
        }
        return if (result.isEmpty()) null else  result.iterator().next().let { it.key to it.value }
    }

    fun coordinatesFor(location: Int): Pair<Int, Int>?  {
        val p = lineFor(location)
        return if (p != null) {
            val y = p.second
            val x = (location - p.first) % TextScreen.width
            x to y
        } else {
            null

        }
    }
}

class TextScreen(private val panel: ScreenPanel) {
    companion object {
        val width = 40
        val height = 24
    }

    private val calculator = LineCalculator()

    fun drawMemoryLocation(location: Int, value: Int) {
        calculator.coordinatesFor(location)?.let { (x, y) ->
            panel.drawCharacter(x, y, value)
        }
    }
}