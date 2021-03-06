package com.beust.app

import com.beust.sixty.bit
import java.io.File
import java.io.InputStream

fun main() {
    DskDisk("Sherwood.dsk", File("d:/pd/Apple Disks/Sherwood Forest.dsk").inputStream())
}

class DskDisk(override val name: String, ins: InputStream, override val sizeInBits: Int = TRACK_SIZE_BITS): BaseDisk() {
    override var bitPosition = 0

    companion object {
        const val TRACK_SIZE_BYTES = 16 * 256
        const val TRACK_SIZE_BITS = TRACK_SIZE_BYTES * 8
        val LOGICAL_SECTORS = listOf(0, 7, 14, 6, 13, 5, 12, 4, 11, 3, 10, 2, 9, 1, 8, 15)
        private val DEST_SIZE = 343
    }
    private val WOZ_IMAGE_SIZE = 256 - 12 + 35* 6656
    private val source = arrayListOf<Int>()

    /** Bit buffers for each track */
    private val bitBuffers = arrayListOf<List<Int>>()

    private val isProdos = false
    private val TRACK_SIZE_ENCODED = 6028
    override var phase: Int = 0

    override fun peekZeroBitCount() = 0

    override fun peekBytes(n: Int): List<Int> {
        var saved = bitPosition
        val result = arrayListOf<Int>()
        repeat(n) {
            result.add(nextByte())
        }
        bitPosition = saved
        return result
    }

    override fun phaseSizeInBits(phase: Int): Int {
        return 6800 * 8
    }

    override fun nextByte(): Int {
        var result = 0
        repeat(8) {
            result = result.shl(1).or(nextBit())
        }
        return result
    }

    private var saved = 0
    override fun save() { saved = bitPosition
    }
    override fun restore() { bitPosition = saved }

    override fun nextBit(): Int {
        val bitBuffer = bitBuffers[phase / 2]
        val result = bitBuffer[bitPosition]
        bitPosition = (bitPosition + 1) % bitBuffer.size
        return result
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