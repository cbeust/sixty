package com.beust.app

import com.beust.sixty.bit
import com.beust.sixty.h
import com.beust.sixty.hh
import java.io.File

fun main() {
    DskDisk()
}

class DskDisk {
    val DISK_IMAGE_SIZE = 35 * 16 * 256
    val WOZ_IMAGE_SIZE = 256 - 12 + 35* 6656
    private val src = arrayListOf<Int>()
    private val dest = arrayListOf<Int>()

    init {
        val bytes = File("src/test/resources/audit.dsk").inputStream().readAllBytes()
        bytes.forEach { src.add(it.toInt()) }
        encode6And2()
        dest.forEachIndexed { index, v ->
            print(v.h() + " ")
            if (index > 0 && index % 16 == 0) println("")
        }
    }

    private fun write8(value: Int): Int {
        repeat(8) {
            dest.add(value.bit(7 - it))
        }
        return 8
    }

    private fun write4And4(value: Int) {
        write8(value.shr(1).or(0xaa))
        write8(value.or(0xaa))
    }

    private fun encode6And2() {
        val bitReverse = listOf(0, 2, 1, 3)
        repeat(84) { c ->
            val b = bitReverse[src[c] and 3] or
                    (bitReverse[src[c + 86] and 3] shl 2) or
                    (bitReverse[src[c + 172] and 3] shl 4)
            dest.add(b)
        }

        dest.add(
                (bitReverse[src[84] and 3] shl 0) or
                (bitReverse[src[170] and 3] shl 2))
        dest.add(
                (bitReverse[src[85] and 3] shl 0) or
                (bitReverse[src[171] and 3] shl 2))

        repeat(256) { c ->
            dest.add(src[c].and(0xff) shr 2)
        }

        // Exclusive OR each byte with the one before it.
        dest.add(dest[341])
        var location = 342
        while (location > 1) {
            --location
            dest[location] = dest[location] xor dest[location - 1]
        }

        dest.all { it >= 0 }

        // Map six-bit values up to full bytes.
        for (c in 0..342) {
            dest[c] = WRITE_TABLE[dest[c]]
        }
    }
}