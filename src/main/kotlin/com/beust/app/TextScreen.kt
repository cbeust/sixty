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
    }

//    init {
//        with(canvas().graphicsContext2D) {
//            fill = Color.BLACK
//            fillRect(0.0, 0.0, fullWidth.toDouble(), fullHeight.toDouble())
//        }
//    }

    private fun lineFor(location: Int): Int? {
        val result = lineMap.filter { (k, v) ->
            location in k..k+width
        }.values.firstOrNull()
        return result
    }

    fun drawMemoryLocation(location: Int, value: Int) {
        val y = lineFor(location)
        if (y != null) {
            val x = (location - 0x400) % width
            println("Location $${location.hh()} ($x,$y) = " + value.and(0x7f).toChar())
            panel.drawCharacter(x, y, value)
        }
    }
}