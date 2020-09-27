package com.beust.app

import com.beust.sixty.ERROR
import com.beust.swt.*
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.widgets.Display

enum class SColor {
    BLACK, WHITE, GREEN, ORANGE, MAGENTA, BLUE;

    fun toSwtColor(display: Display): Color = when (this) {
            BLACK -> black(display)
            WHITE -> white(display)
            GREEN -> green(display)
            ORANGE -> orange(display)
            MAGENTA -> magenta(display)
            BLUE -> blue(display)
            else -> ERROR("Unexpected color: $this")
    }
}

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
        fun color(group: Int, bits: Int, x: Int): SColor {
            val result = when(bits) {
                0 -> SColor.BLACK
                3 -> SColor.WHITE
                2 -> /* if (x%2==0) Color.BLACK else */ if (group == 0) SColor.GREEN  else SColor.ORANGE
                else -> /* if (x%2==1) Color.BLACK else */ if (group == 0) SColor.MAGENTA else SColor.BLUE
            }
            return result // if (result == Color.ORANGE) result else Color.BLACK
        }
    }

    fun colors(d: Display, x: Int) : List<SColor> =
        listOf(color(p0, aa, x), color(p0, bb, x+1), color(p0, cc, x+2), color(p0, dd, x+3),
                color(p1, ee, x+4), color(p1, ff, x+5), color(p1, gg, x+6))

    fun colorStrings(d: Display, x: Int): String = colors(d, x).map { it.toString() }.joinToString(" ")
}