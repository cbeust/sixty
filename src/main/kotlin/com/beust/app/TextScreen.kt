package com.beust.app

import com.beust.sixty.hh
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color

class TextScreen(private val panel: ScreenPanel) {
    companion object {
        val width = 40
        val height = 24
    }
    val lineMap = hashMapOf<Int, Int>()

    init {
        var line = 0
        listOf(0, 0x28, 0x50).forEach { m ->
            repeat(8) {
                val address = 0x400 + it * 0x80 + m
                println("Address: " + address.hh() + " line: $line")
                lineMap[address] = line++
            }
        }
        val l = lineFor(0x7d1)
        ""
    }

//    init {
//        with(canvas().graphicsContext2D) {
//            fill = Color.BLACK
//            fillRect(0.0, 0.0, fullWidth.toDouble(), fullHeight.toDouble())
//        }
//    }

    private fun lineFor(location: Int): Pair<Int, Int>? {
        val result = lineMap.filter { (k, v) ->
            location in k until k+width
        }
        return if (result.isEmpty()) null else  result.iterator().next().let { it.key to it.value }
    }

    fun drawMemoryLocation(location: Int, value: Int) {
        val p = lineFor(location)
        if (p != null) {
            val y = p.second
            val x = (location - p.first) % width
            if (location == 0x7d1 || location == 0x40b) {
                println("BREAKPOINT")
            }
            println("Location $${location.hh()} ($x,$y) = " + value.and(0x7f).toChar())
            panel.drawCharacter(x, y, value)
        }
    }
}