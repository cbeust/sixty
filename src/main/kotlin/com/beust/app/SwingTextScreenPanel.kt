package com.beust.app

import com.beust.app.app.ITextScreen

class LineCalculator(val start: Int) {
    private val lineMap = hashMapOf<Int, Int>()

    init {
        var line = 0
        listOf(0, 0x28, 0x50).forEach { m ->
            repeat(8) {
                val address = start + it * 0x80 + m
//                println("Address: " + address.hh() + " line: $line")
                lineMap[address] = line++
            }
        }
    }

    private fun lineFor(location: Int): Pair<Int, Int>? {
        val result = lineMap.filter { (k, v) ->
            location in k until k + ITextScreen.WIDTH
        }
        return if (result.isEmpty()) null else  result.iterator().next().let { it.key to it.value }
    }

    fun coordinatesFor(location: Int): Pair<Int, Int>?  {
        val p = lineFor(location)
        return if (p != null) {
            val y = p.second
            val x = (location - p.first) % ITextScreen.WIDTH
            x to y
        } else {
            null

        }
    }
}
