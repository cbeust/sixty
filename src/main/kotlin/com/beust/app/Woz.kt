package com.beust.app

import com.beust.sixty.bit
import com.beust.sixty.h
import com.beust.sixty.hh
import com.beust.sixty.logDisk
import kotlin.random.Random

class Woz(private val bytes: ByteArray,
        val bitStreamFactory: (bytes: List<Byte>) -> IBitStream = { bytes -> BitStream(bytes) }) {
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

        fun read2(): Int = read1().toUByte().toUInt().or(read1().toUByte().toUInt().shl(8)).toInt()

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
        readHeader(stream)

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

    private val bitStreams = mutableMapOf<Int, IBitStream>()

    fun bitStreamForTrack(phase: Int): IBitStream {
        val trackNumber = phase * 2
        return bitStreams.getOrPut(trackNumber) {
            val tmapOffset = tmap.offsetFor(trackNumber)
            if (tmapOffset == -1) {
                val trk = trks.trks[trackNumber]
                val streamSizeInBytes = (trk.bitCount / 8)
                FakeBitStream()
            } else {
                val trk = trks.trks[tmapOffset]
                val streamSizeInBytes = (trk.bitCount / 8)
                val trackOffset = trk.startingBlock * 512
                try {
                    val slice = bytes.slice(trackOffset..(trackOffset + streamSizeInBytes))
                    logDisk("Phase $phase (track $trackNumber) starts at block ${trk.startingBlock} " +
                            "offset ${trackOffset.hh()} size ${streamSizeInBytes.h()}")
                    bitStreamFactory(slice)
                } catch (ex: Exception) {
                    println("PROBLEM")
                    throw ex
                }
            }
        }
    }


}

abstract class IBitStream {
    abstract val sizeInBytes: Int

    /**
     * @return a pair of the new index and the returned bit.
     */
    abstract fun next(position: Int): Pair<Int, Int>
}

class BitStream(val bytes: List<Byte>): IBitStream() {
    private val bits = arrayListOf<Int>()
    init {
        bytes.forEach { byte ->
            repeat(8) {
                val bit = byte.bit(7 - it)
                bits.add(bit)
            }
        }
    }

    override val sizeInBytes: Int
        get() = bits.size / 8

    override fun next(position: Int): Pair<Int, Int> {
        return Pair((position + 1) % bits.size, bits[position])
//        val byteIndex = position / 8
//        val bitIndex = position % 8
//        if (byteIndex > bytes.size) {
//            TODO("ERROR")
//        }
//        val byte = bytes[byteIndex]
//        val result = byte.bit(7 - bitIndex)
//        return Pair((position + 1) % (bytes.size * 8), result)
    }
}


class _BitStream(val bytes: List<Byte>): IBitStream() {
    override val sizeInBytes: Int
        get() = bytes.size

    override fun next(position: Int): Pair<Int, Int> {
        val byteIndex = position / 8
        val bitIndex = position % 8
        if (byteIndex > bytes.size) {
            TODO("ERROR")
        }
        val byte = bytes[byteIndex]
        val result = byte.bit(7 - bitIndex)
        return Pair((position + 1) % (bytes.size * 8), result)
    }
}

class FakeBitStream(override val sizeInBytes: Int = 0) : IBitStream() {
    override fun next(position: Int): Pair<Int, Int> {
        val result = if (Random.nextInt() % 10 < 3) 1 else 0
        return Pair(position, result)
    }

    fun next(): Int {
        val index = 0
        val p = next(index)
        return p.second
    }
}



val WRITE_TABLE = listOf(
        0x96, 0x97, 0x9a, 0x9b, 0x9d, 0x9e, 0x9f, 0xa6,
        0xa7, 0xab, 0xac, 0xad, 0xae, 0xaf, 0xb2, 0xb3,
        0xb4, 0xb5, 0xb6, 0xb7, 0xb9, 0xba, 0xbb, 0xbc,
        0xbd, 0xbe, 0xbf, 0xcb, 0xcd, 0xce, 0xcf, 0xd3,
        0xd6, 0xd7, 0xd9, 0xda, 0xdb, 0xdc, 0xdd, 0xde,
        0xdf, 0xe5, 0xe6, 0xe7, 0xe9, 0xea, 0xeb, 0xec,
        0xed, 0xee, 0xef, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6,
        0xf7, 0xf9, 0xfa, 0xfb, 0xfc, 0xfd, 0xfe, 0xff
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
