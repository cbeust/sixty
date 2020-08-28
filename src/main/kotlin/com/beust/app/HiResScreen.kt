package com.beust.app

import com.beust.sixty.Memory
import com.beust.sixty.b
import com.beust.sixty.toHex
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color

/**
 * Bit pattern for two consecutive bytes:
 *
 * PDCCBBAA PGGFFEED
 *
 * P = palette select
 * AA is first bit, then BB, then CC
 * DD is split over two bytes
 * then EE, FF, GG
 */
class HiResScreen(private val canvas: Canvas) {
    private val width = 140
    private val height = 192
    private val blockWidth = 5
    private val blockHeight = 5
    private val gap = 2
    private val fullWidth = (blockWidth + gap) * width + 40
    private val fullHeight = (blockHeight + gap) * height + 40
    private val board = Board(canvas, 600, 400)

    /**
     * 2000-2027
     */
    private val consecutives = listOf(0, 0x400, 0x800, 0xc00, 0x1000, 0x1400, 0x1800, 0x1c00)
    private val interleaving = listOf(
            0, 0x80, 0x100, 0x180, 0x200, 0x280, 0x300, 0x380,
            0x28, 0xa8, 0x128, 0x1a8, 0x228, 0x2a8, 0x328, 0x3a8,
            0x50, 0xd0, 0x150, 0x1d0, 0x250, 0x2d0, 0x350, 0x3d0)
    private val lineMap = hashMapOf<Int, Int>()

    init {
        var l = 0
        interleaving.forEach { il ->
            consecutives.forEach { c ->
                lineMap[il + c] = l++
            }
        }
//        drawMemoryLocation(0x0000, 0x2a)
//        (0..0x1fff).forEach {
//            drawMemoryLocation(it, 1)
//        }
    }

    fun drawMemoryLocation(memory: Memory, location: Int, value: Int) {
        val bitPattern = if (location %2 == 0) {
            val byte0 = memory.byte(location)
            val byte1 = memory.byte(location + 1)
            BitPattern(byte0, byte1)
        } else {
            // DD BB
            // 1101_1101  1011_1011
            // aa:1 bb:3 cc:1 dd:3 ee:1 ff:3 gg: 1
            val byte0 = memory.byte(location - 1)
            val byte1 = memory.byte(location)
            BitPattern(byte0, byte1)
        }

        //
        // Calculate x,y
        //
        val evenLocation = if (location % 2 == 0) location else location - 1
        val loc = evenLocation - 0x2000
        var closest = Integer.MAX_VALUE
        var key = -1
        lineMap.keys.forEach { k ->
            val distance = loc - k
            if (distance in 0 .. closest) {
                closest = distance
                key = k
            }
        }
        val y = lineMap[key]
        val x = loc - key

        //
        // Now we have x,y and 7 pixels to write
        //
        fun color(group: Int, bits: Int): Color {
            val result = when(bits) {
                0 -> Color.BLACK
                3 -> Color.WHITE
                2 -> if (group == 0) Color.GREEN else Color.ORANGE
                else -> if (group == 0) Color.MAGENTA else Color.BLUE
            }
            return result
        }
        var i = 0
        drawPixel(x + i++, y!!, color(bitPattern.p0, bitPattern.aa))
        drawPixel(x + i++, y, color(bitPattern.p0, bitPattern.bb))
        drawPixel(x + i++, y, color(bitPattern.p0, bitPattern.cc))
        drawPixel(x + i++, y, color(bitPattern.p0, bitPattern.dd))
        drawPixel(x + i++, y, color(bitPattern.p1, bitPattern.ee))
        drawPixel(x + i++, y, color(bitPattern.p1, bitPattern.ff))
        drawPixel(x + i++, y, color(bitPattern.p1, bitPattern.gg))

    }

    fun Color.s() = when(this) {
        Color.BLACK -> "black"
        Color.WHITE -> "white"
        Color.GREEN -> "green"
        Color.VIOLET -> "violet"
        Color.ORANGE -> "orange"
        Color.BLUE -> "blue"
        else -> this.toString()
    }

    private fun drawPixel(x: Int, y: Int, color: Color) {
        println("Drawing $x,$y with ${color.s()}")
        board.draw(x, y, color)
    }
}

class BitPattern(byte0: Int, byte1: Int) {
    var p0: Int = 0
    var aa: Int = 0
    var bb: Int = 0
    var cc: Int = 0

    var p1: Int = 0
    var dd: Int = 0
    var ee: Int = 0
    var ff: Int = 0
    var gg: Int = 0

    init {
        p0 = byte0.and(0x80).shr(7)
        aa = byte0.and(0x3)
        bb = byte0.and(0xc).shr(2)
        cc = byte0.and(0x30).shr(4)
        dd = byte0.and(0x40).shr(5).or(byte1.and(1))
        ee = byte1.and(6).shr(1)
        ff = byte1.and(0x18).shr(3)
        gg = byte1.and(0x60).shr(5)
        p1 = byte1.and(0x80).shr(7)
    }
}
