package com.beust.app

import com.beust.sixty.bit
import com.beust.sixty.h
import java.io.File

fun main() {
    DskDisk()
}

class DskDisk {
    private val DISK_IMAGE_SIZE = 35 * 16 * 256
    private val TRACK_SIZE = 16 * 256
    private val WOZ_IMAGE_SIZE = 256 - 12 + 35* 6656
    private val src = arrayListOf<Int>()
    private val DEST_SIZE = 343
    private val bitBuffer = arrayListOf<Int>()
    private val isProdos = false
    private val TRACK_SIZE_ENCODED = 6028

    init {
        val bytes = File("src/test/resources/audit.dsk").inputStream().readAllBytes()
        bytes.forEach { src.add(it.toInt()) }
        repeat(2) { track ->
            val start = track * TRACK_SIZE
            encodeTrack(track, src.slice(start until start + TRACK_SIZE))

            val startEncoded = TRACK_SIZE_ENCODED * track
        }
        val byteStream = ByteStream(bitBuffer)
        testByteStream(byteStream)
//        var i = 0
//        while (i < bitBuffer.size) {
//            var byte = 0
//            repeat(8) {
//                byte = byte.shl(1).or(bitBuffer[it + i])
//            }
//            print(byte.h() + " ")
//            if (i > 0 && i % (16*8) == 0) println("")
//            i += 8
//
//        }

        ""
    }

    private fun encodeTrack(track: Int, bytes: List<Int>) {
        println("Encoding track $track")
        repeat(16) { sector ->
            writeSync(6)
            write8(0xd5, 0xaa, 0x96)
            write4And4(0xfe)
            write4And4(track)
            write4And4(sector)
            write4And4(0xfe xor track xor sector)
            write8(0xde, 0xaa, 0xeb)

            writeSync(2)

            write8(0xd5, 0xaa, 0xad)
            val logicalSector = if (sector == 15) 15 else ((sector * (if (isProdos) 8 else 7)) % 15);
            val encoded = encode6And2(bytes)
            encoded.forEach {
                write8(it)
            }
            write8(0xde, 0xaa, 0xeb)

            writeSync(3)
        }

    }

    private fun writeSync(count: Int) {
        repeat(count) {
            write8(0xff)
            write1(0, 0)
        }
    }

    private fun write1(vararg v: Int) {
        v.forEach { bitBuffer.add(it) }
    }

    private fun write8(vararg values: Int): Int {
        values.forEach { value ->
            repeat(8) {
                bitBuffer.add(value.bit(7 - it))
            }
        }
        return 8 * values.size
    }

    private fun write4And4(value: Int) {
        var n = 0
        println(value.shr(1).or(0xaa))
        println(value.or(0xaa))
        n = 1
        println(value.shr(1).or(0xaa))
        println(value.or(0xaa))
        write8(value.shr(1).or(0xaa))
        write8(value.or(0xaa))
    }

    private fun encode6And2(src: List<Int>): IntArray {
        val result = IntArray(DEST_SIZE) { 0 }
        val bitReverse = listOf(0, 2, 1, 3)
        repeat(84) { c ->
            val b = bitReverse[src[c] and 3] or
                    (bitReverse[src[c + 86] and 3] shl 2) or
                    (bitReverse[src[c + 172] and 3] shl 4)
            result[c] =b
        }

        result[84] =
                (bitReverse[src[84] and 3] shl 0) or
                (bitReverse[src[170] and 3] shl 2)
        result[85] =
                (bitReverse[src[85] and 3] shl 0) or
                (bitReverse[src[171] and 3] shl 2)

        repeat(256) { c ->
            result[86 + c] = src[c].and(0xff) shr 2
        }

        // Exclusive OR each byte with the one before it.
        result[342] = result[341]
        var location = 342
        while (location > 1) {
            --location
            result[location] = result[location] xor result[location - 1]
        }

        result.all { it >= 0 }

        // Map six-bit values up to full bytes.
        for (c in 0..342) {
            result[c] = WRITE_TABLE[result[c]]
        }
        return result
    }
}