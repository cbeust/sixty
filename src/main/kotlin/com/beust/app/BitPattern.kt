package com.beust.app

import com.beust.sixty.s
import javafx.scene.paint.Color

class BitPattern(private val byte0: Int, private val byte1: Int) {
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
        dd = byte1.and(1).shl(1).or(byte0.and(0x40).shr(6))
        ee = byte1.and(6).shr(1)
        ff = byte1.and(0x18).shr(3)
        gg = byte1.and(0x60).shr(5)
        p1 = byte1.and(0x80).shr(7)
    }

    companion object {
        //
        // Now we have x,y and 7 pixels to write
        //
        // Finally, another quirk of Wozniak's design is that while any pixel could be black or white,
        // only pixels with odd X-coordinates could be green or orange. Likewise, only even-numbered pixels
        // could be violet or blue.[4]
        //
        fun color(group: Int, bits: Int, x: Int): Color {
            val result = when(bits) {
                0 -> Color.BLACK
                3 -> Color.WHITE
                2 -> if (group == 0) Color.GREEN else Color.ORANGE
                else -> if (group == 0) Color.MAGENTA else Color.BLUE
            }
            return result // if (result == Color.ORANGE) result else Color.BLACK
        }
    }

    fun colors(x: Int) : List<Color> =
        listOf(color(p0, aa, x), color(p0, bb, x), color(p0, cc, x), color(p0, dd, x),
                color(p1, ee, x), color(p1, ff, x), color(p1, gg, x))

    fun colorStrings(x: Int): String = colors(x).map { it -> it.s() }.joinToString(" ")
}