package com.beust.app

import com.beust.sixty.bit
import com.beust.sixty.h
import java.io.File
import java.io.InputStream

fun main() {
    DskDisk(File("src/test/resources/audit.dsk").inputStream())
}

class DskDisk(val ins: InputStream): IDisk {
    companion object {
        private const val TRACK_MAX = 35
        const val DISK_IMAGE_SIZE_BYTES = TRACK_MAX * 16 * 256
        const val TRACK_SIZE_BYTES = 16 * 256
        val LOGICAL_SECTORS = listOf(0, 7, 14, 6, 13, 5, 12, 4, 11, 3, 10, 2, 9, 1, 8, 15)
    }
    private val WOZ_IMAGE_SIZE = 256 - 12 + 35* 6656
    private val source = arrayListOf<Int>()
    private val DEST_SIZE = 343
    private var bitPosition = 0
    val bitBuffer = arrayListOf<Int>()
    private val isProdos = false
    private val TRACK_SIZE_ENCODED = 6028
    private var track: Int = 0

    override fun nextBit(): Int {
        val result = bitBuffer[bitPosition]
        bitPosition = (bitPosition + 1) % bitBuffer.size
        return result
    }

    override fun incTrack() {
        if (track < TRACK_MAX - 1) {
            track++
            bitPosition = (bitPosition + TRACK_SIZE_BYTES * 8) % bitBuffer.size
        }
    }

    override fun decTrack() {
        if (track > 0) {
            track--
            bitPosition = (bitPosition - TRACK_SIZE_BYTES * 8) % bitBuffer.size
        }
    }

    init {
        val bytes = ins.readAllBytes()
        bytes.forEach { source.add(it.toInt()) }
        repeat(35) { track ->
            val start = track * TRACK_SIZE_BYTES
            encodeTrack(track, source.slice(start until start + TRACK_SIZE_BYTES))

            val startEncoded = TRACK_SIZE_ENCODED * track
        }
//        val byteStream = ByteStream(bitBuffer)
//        getSectorsFromTrack(byteStream)
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
        // Gap one
        writeSync(16)
        repeat(16) { s ->
//            val logicalSector = s
            write8(0xd5, 0xaa, 0x96)
            write4And4(0xfe)
            write4And4(track)
            write4And4(s)
            write4And4(0xfe xor track xor s)
            write8(0xde, 0xaa, 0xeb)

            writeSync(7)

            write8(0xd5, 0xaa, 0xad)
            val logicalSector = LOGICAL_SECTORS[s]
            val start = logicalSector * 256
//            println("ENCODING SECTOR $logicalSector")
            val encoded = encode6And2(bytes.slice(start until start + 256))
            encoded.forEach {
                write8(it)
            }
            write8(0xde, 0xaa, 0xeb)

            writeSync(16)
        }

        // Add the track suffix.
        val trackPosition = bitBuffer.size
        write8((trackPosition + 7).shr(3).and(0xff))
        write8((trackPosition + 7).shr(11).and(0xff))
        write8(trackPosition.and(0xff))
        write8(trackPosition.shr(8).and(0xff))
        write8(0)
        write8(0)
        write8(0xff)
        write8(10)
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
        write8(value.shr(1).or(0xaa))
        write8(value.or(0xaa))
    }

    private fun encode6And2(src: List<Int>): IntArray {
        require(src.size == 256)
//        println("[0] = " + src[0].and(0xff).h())
        val result = IntArray(DEST_SIZE) { 0 }
        val bitReverse = listOf(0, 2, 1, 3)
        repeat(84) { c ->
            val b = bitReverse[src[c] and 3] or
                    (bitReverse[src[c + 86] and 3] shl 2) or
                    (bitReverse[src[c + 172] and 3] shl 4)
            result[c] = b
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