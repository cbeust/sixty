package com.beust.app

import com.beust.sixty.bit
import com.beust.sixty.h
import com.beust.sixty.hh
import java.io.File
import java.io.InputStream

class WozDisk(ins: InputStream,
        val bitStreamFactory: (bytes: List<Byte>) -> IBitStream = { bytes -> BitStream(bytes) }) {
    private val MAX_TRACK = 160

    var position: Int = 0
    private var saved: Int = 0

    var track = 0
//    var bitPosition = 0
    private val bytes: ByteArray = ins.readAllBytes()
    private val woz = Woz(bytes)
    private val bitStreams = mutableMapOf<Int, IBitStream>()

    private fun save() { saved = position }
    private fun restore() { position = saved }

    fun createBits(bytes: ByteArray): List<Int> {
        val result = arrayListOf<Int>()
        bytes.forEach {
            for (i in 0..8) {
                result.add(it.bit(7 - i))
            }
        }
        return result
    }

    fun incTrack() {
        track++
        if (track >= MAX_TRACK) track = MAX_TRACK - 1
        track++
        if (track >= MAX_TRACK) track = MAX_TRACK - 1
    }

    fun decTrack() {
        if (track > 0) track--
        if (track > 0) track--
    }

    fun peekBytes(count: Int): ArrayList<Int> {
        val result = arrayListOf<Int>()
        val b = bitStream
        save()
        repeat(count) {
            result.add(nextByte())
        }
        restore()
        return result
    }

    fun peekByte() = nextByte(true)

    fun nextBytes(count: Int) : List<Int> {
        val result = arrayListOf<Int>()
        repeat(count) {
            result.add(nextByte())
        }
        return result
    }

    val bitStream: IBitStream
        get() {
            return bitStreams.getOrPut(track) {
                val tmapOffset = woz.tmap.offsetFor(track)
                if (tmapOffset == -1) {
                    TODO("-1 tmap offset")
                }
                val trk = woz.trks.trks[tmapOffset]
                val streamSizeInBytes = (trk.bitCount / 8)
                val trackOffset = trk.startingBlock * 512
                val slice = bytes.slice(trackOffset.. (trackOffset + streamSizeInBytes))
                println("Track ${track / 4.0} starts at ${trackOffset.hh()}")
                bitStreamFactory(slice)
            }
        }

    fun nextByte(peek: Boolean = false): Int {
        var result = 0
        if (peek) {
            save()
        }
        while (result and 0x80 == 0) {
            val (newPosition, nb) = bitStream.next(position)
            position = newPosition
//            val nb = nextBit(peek, ahead * 8)
            result = result.shl(1).or(nb)
        }
        if (peek) {
            restore()
        }
        val rh = result.h()
        return result
    }

    /**
     * @return the next byte and the stream size in bits
     */
//    fun calculateCurrentByte(currentBitPosition: Int): Pair<Int, Int> {
//        val tmapOffset = woz.tmap.offsetFor(track)
//        val trk = woz.trks.trks[tmapOffset]
//        val streamSizeInBytes = (trk.bitCount / 8)
//        val relativeOffset = currentBitPosition / 8
//        val byteOffset = (trk.startingBlock * 512 + relativeOffset) % streamSizeInBytes
//        val byte = bytes[byteOffset].toInt().and(0xff)
//        return byte to trk.bitCount
//    }
}

class Woz(bytes: ByteArray) {
    lateinit var info: ChunkInfo
    lateinit var tmap: ChunkTmap
    lateinit var trks: ChunkTrks

    init {
        read(bytes)
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    class Stream(val bytes: ByteArray) {
        var i = 0

        fun hasNext() = i < bytes.size

        fun read1(): Int {
            return bytes[i++].toInt()
        }

        fun read4Bytes(): ByteArray {
            val result = bytes.slice(i .. i + 3)
            i += 4
            return result.toByteArray()
        }
        
        fun read4(): Int {
            val result = bytes[i].toUByte().toUInt()
                    .or(bytes[i + 1].toUByte().toUInt().shl(8))
                    .or(bytes[i + 2].toUByte().toUInt().shl(16))
                    .or(bytes[i + 3].toUByte().toUInt().shl(24))
            i += 4
            return result.toInt()

        }

        fun read2(): Int = read1().or(read1().shl(8))

        fun read4String() = String(read4Bytes())

        fun readN(length: Int): ByteArray {
            val result = bytes.slice(i until i + length).toByteArray()
            i += length
            return result
        }

        override fun toString(): String {
            return "{Stream index:$${i.hh()} next:" + bytes[i].h() + " " + bytes[i + 1].h() + "}"
        }
    }

    data class Header(val name: String, val ff: Byte, private val crc: ByteArray)

    private fun readHeader(s: Stream): Header {
        val name = s.read4String()
        val ff = s.read1()
        repeat(3) {
            s.read1()
        }
        val crc = s.read4Bytes()
        return Header(name, ff.toByte(), crc)
    }

    open class Chunk(val name: String, val size: Int) {
        override fun toString(): String {
            return "{$name size=$size}"
        }
    }

    class ChunkInfo(stream: Stream, size: Int): Chunk("INFO", size) {
        init {
            stream.readN(size)
        }
    }

    class ChunkTmap(private val stream: Stream, size: Int): Chunk("TMAP", size) {
        private val map = hashMapOf<Int, Int>()
        init {
            repeat(160) {
                map[it] = stream.read1()
            }
        }

        /**
         * Track number, each increment is a quarter track: 0 -> 0, 1 -> 0.25, 2 -> 0.5, ...
         */
        fun offsetFor(trackNumber: Int): Int {
            if (trackNumber !in 0..160) {
                throw IllegalArgumentException("Quarter track illegal: $trackNumber")
            }
            return map[trackNumber]!!
        }

        override fun toString(): String {
            return "{TMAP size=$size 0:" + offsetFor(0) + " 1:" + offsetFor(1) + " 2:" + offsetFor(2) + "...}"
        }
    }

    class ChunkTrks(private val stream: Stream, size: Int): Chunk("TRKS", size) {
        data class Trk(val startingBlock: Int, val blockCount: Int, val bitCount: Int)
        val trks = arrayListOf<Trk>()
        init {
            repeat(160) {
                val sb = stream.read2()
                val bc = stream.read2()
                val bitCount = stream.read4()
                val trk = Trk(sb, bc, bitCount)
                trks.add(trk)
            }
        }
    }

    fun read(bytes: ByteArray) {
        val stream = Stream(bytes)
        println("Header: " + readHeader(stream))

        while (! this::info.isInitialized || ! this::tmap.isInitialized || ! this::trks.isInitialized) {
            val name = stream.read4String()
            val size = stream.read4()
            when (name) {
                "INFO" -> info = ChunkInfo(stream, size)
                "TMAP" -> tmap = ChunkTmap(stream, size)
                "TRKS" -> trks = ChunkTrks(stream, size)
                else -> Chunk(name, size)
            }
        }
    }
}

abstract class IBitStream() {
    /**
     * @return a pair of the new index and the returned bit.
     */
    abstract fun next(position: Int): Pair<Int, Int>
}

class BitStream(val bytes: List<Byte>): IBitStream() {
    override fun next(position: Int): Pair<Int, Int> {
        val byteIndex = position / 8
        val bitIndex = position % 8
        val byte = bytes[byteIndex]
        val result = byte.bit(7 - bitIndex)
        return Pair((position + 1) % (bytes.size * 8), result)
    }
}

val WRITE_TABLE = listOf(
    0x96, 0x97, 0x9A, 0x9B, 0x9D, 0x9E, 0x9F, 0xA6, 0xA7, 0xAB, 0xAC, 0xAD, 0xAE, 0xAF, 0xB2, 0xB3,
        0xB4, 0xB5, 0xB6, 0xB7, 0xB9, 0xBA, 0xBB, 0xBC, 0xBD, 0xBE, 0xBF, 0xCB, 0xCD, 0xCE, 0xCF, 0xD3,
        0xD6, 0xD7, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF, 0xE5, 0xE6, 0xE7, 0xE9, 0xEA, 0xEB, 0xEC,
        0xED, 0xEE, 0xEF, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE, 0xFF
)

val READ_TABLE = hashMapOf<Int, Int>().apply {
    WRITE_TABLE.withIndex().forEach {  (ind, v) ->
        this[v] = ind
    }
}

/**
 * Found at $36c during stage 1, used by decoding routine a $c6a6.
 */
val A36C = listOf(
        0x00, 0x01, 0x00, 0x00, 0x02, 0x03, 0x00, 0x04, 0x05, 0x06, 0x00, 0x00, 0xff, 0xff, 0x00, 0x00,
        0x07, 0x08, 0x00, 0x00, 0xff, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x00, 0x00, 0x0e, 0x0f, 0x10, 0x11,
        0x12, 0x13, 0x00, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x00, 0x00, 0xff, 0xff, 0x00, 0x00,
        0xff, 0xff, 0x00, 0x00, 0xff, 0x1b, 0x00, 0x1c, 0x1d, 0x1e, 0x00, 0x00, 0xff, 0x1f, 0x00, 0x00,
        0x20, 0x21, 0x00, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x38, 0x00, 0x00, 0xff, 0xff, 0x00, 0x29,
        0x2a, 0x2b, 0x00, 0x2c, 0x2d, 0x2e, 0x2f, 0x30, 0x31, 0x32, 0x00, 0x00, 0x33, 0x34, 0x35, 0x36,
        0x37, 0x38, 0x00, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f
)
