package com.beust.app

import com.beust.sixty.bit
import com.beust.sixty.h
import java.io.File
import java.io.InputStream

fun main() {
    DskDisk("Sherwood.dsk", File("d:/pd/Apple Disks/Sherwood Forest.dsk").inputStream())
}

class DskDisk(override val name: String, ins: InputStream): BaseDisk() {
    companion object {
        private const val TRACK_MAX = 70
        const val DISK_IMAGE_SIZE_BYTES = TRACK_MAX * 16 * 256
        const val TRACK_SIZE_BYTES = 16 * 256
        val LOGICAL_SECTORS = listOf(0, 7, 14, 6, 13, 5, 12, 4, 11, 3, 10, 2, 9, 1, 8, 15)
        private val DEST_SIZE = 343
    }
    private val WOZ_IMAGE_SIZE = 256 - 12 + 35* 6656
    private val source = arrayListOf<Int>()

    /** Bit buffers for each track */
    private val bitBuffers = arrayListOf<List<Int>>()
    private var positionInTrack = 0

    private val isProdos = false
    private val TRACK_SIZE_ENCODED = 6028
    private var track: Int = 0

    override fun nextBit(): Int {
        bitBuffers[track / 2].let { bitBuffer ->
            val result = bitBuffer[positionInTrack]
            positionInTrack = (positionInTrack + 1) % bitBuffer.size
            return result
        }
    }

    override fun incTrack() {
        if (track < TRACK_MAX - 1) {
            track++
        }
    }

    override fun decTrack() {
        if (track > 0) {
            track--
        }
    }

    override fun peekBytes(n: Int): List<Int> {
        var saved = positionInTrack
        val result = arrayListOf<Int>()
        repeat(n) {
            result.add(nextByte())
        }
        positionInTrack = saved
        return result
    }

    override fun phaseSizeInBytes(phase: Int): Int {
        return 16 * 256
    }

    //    private val byteStream: IByteStream
    init {
        val bytes = ins.readAllBytes()
        bytes.forEach { source.add(it.toInt()) }
        repeat(35) { track ->
            val start = track * TRACK_SIZE_BYTES
            encodeTrack(track, source.slice(start until start + TRACK_SIZE_BYTES))

            val startEncoded = TRACK_SIZE_ENCODED * track
        }
//        byteStream = ByteStream(bitBuffer)
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
        val bb = arrayListOf<Int>()
        bitBuffers.add(bb)
        // Gap one
        writeSync(bb, 16)
        repeat(16) { s ->
//            val logicalSector = s
            write8(bb, 0xd5, 0xaa, 0x96)
            write4And4(bb, 0xfe)
            write4And4(bb, track)
            write4And4(bb, s)
            write4And4(bb, 0xfe xor track xor s)
            write8(bb, 0xde, 0xaa, 0xeb)

            writeSync(bb, 7)

            write8(bb, 0xd5, 0xaa, 0xad)
            val logicalSector = LOGICAL_SECTORS[s]
            val start = logicalSector * 256
//            println("ENCODING SECTOR $logicalSector")
            val encoded = encode6And2(bytes.slice(start until start + 256))
            encoded.forEach {
                write8(bb, it)
            }
            write8(bb, 0xde, 0xaa, 0xeb)

            writeSync(bb, 16)
        }

        // Add the track suffix.
        val trackPosition = bb.size
        write8(bb, (trackPosition + 7).shr(3).and(0xff))
        write8(bb, (trackPosition + 7).shr(11).and(0xff))
        write8(bb, trackPosition.and(0xff))
        write8(bb, trackPosition.shr(8).and(0xff))
        write8(bb, 0)
        write8(bb, 0)
        write8(bb, 0xff)
        write8(bb, 10)
    }

    private fun writeSync(bitBuffer: ArrayList<Int>, count: Int) {
        repeat(count) {
            write8(bitBuffer, 0xff)
            write1(bitBuffer, 0, 0)
        }
    }

    private fun write1(bitBuffer: ArrayList<Int>, vararg v: Int) {
        v.forEach { bitBuffer.add(it) }
    }

    private fun write8(bitBuffer: ArrayList<Int>, vararg values: Int): Int {
        values.forEach { value ->
            repeat(8) {
                bitBuffer.add(value.bit(7 - it))
            }
        }
        return 8 * values.size
    }

    private fun write4And4(bitBuffer: ArrayList<Int>, value: Int) {
        write8(bitBuffer, value.shr(1).or(0xaa))
        write8(bitBuffer, value.or(0xaa))
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