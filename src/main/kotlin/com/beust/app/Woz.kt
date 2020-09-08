package com.beust.app

import com.beust.sixty.h
import com.beust.sixty.hh
import java.io.File
import java.io.InputStream

fun main() {
    val ins = Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz").openStream()
    val ins2 = File("d:\\pd\\Apple DIsks\\woz2\\The Apple at Play.woz").inputStream()
    val disk = WozDisk(ins)
    var carry = 1

    fun rol(v: Int): Int {
        val result = (v.shl(1).or(carry)) and 0xff
//        carry = if (v.and(0x80) != 0) 1 else 0
        return result
    }

    fun pair(): Int {
        val b1 = disk.nextByte()
        val b2 = disk.nextByte()
        return rol(b1).and(b2).and(0xff)
    }

    repeat(13) {
        while (disk.peekBytes(3) != listOf(0xd5, 0xaa, 0x96)) {
            disk.nextByte()
        }
        val s = disk.nextBytes(3)
        val volume = pair()
        val track = pair()
        val sector = pair()
        val checksum = pair()
        if (volume.xor(track).xor(sector) != checksum) {
            TODO("Checksum doesn't match")
        }
        println("Volume: $volume Track: $track Sector: $sector")
        if (disk.nextBytes(3) != listOf(0xde, 0xaa, 0xeb)) {
            TODO("Didn't find closing for address")
        }

        while (disk.peekBytes(3) != listOf(0xd5, 0xaa, 0xad)) {
            disk.nextByte()
        }
        disk.nextBytes(3)
        val bb00 = ArrayList<Int>(86)
        var ind0 = 0
        val bc00 = ArrayList<Int>(256)
        var acc = 0
        var ind1 = 0
        repeat(86) {
            val nb = READ_TABLE[disk.nextByte()]!!
            val x = nb.xor(acc)
            bb00.add(x)
            acc = x
        }
        repeat(256) {
            val nb = READ_TABLE[disk.nextByte()]!!
            val x = nb.xor(acc)
            bc00.add(x)
            acc = x
        }
        val checksum1 = disk.nextByte()
        if (sector == 12) {
            println("PROBLEM")
        }
        if (disk.nextBytes(3) != listOf(0xde, 0xaa, 0xeb)) {
            TODO("Didn't find closing for data")
        }
    }

    repeat(100) {
        println(disk.nextByte().h() + " ")
    }

    fun readTest() {
        var sectorsRead = 0
        while (sectorsRead < 13) {
            var b = disk.nextByte()
            start@ while (true) {
                while (b != 0xd5) b = disk.nextByte()
                if (disk.nextByte() != 0xaa) break@start
                if (disk.nextByte() != 0x96) break@start
                val volume = pair()
                val track = pair()
                val sector = pair()
                val checksum = pair()
                val expected = volume.xor(track).xor(sector)
                println("Offset " + (0x600 + (disk.bitPosition / 8)).hh()
                        + " volume: ${volume.h()} track: ${track.h()} sector: ${sector.h()}"
                        + " checksum: $checksum expected: $expected")
                if (disk.nextByte() != 0xde) {
                    println("PROBLEM")
                }
                if (disk.nextByte() != 0xaa) {
                    println("PROBLEM")
                }
            }
            b = disk.nextByte()
            println("next: " + disk.peekBytes(10))
            start2@ while (true) {
                while (b != 0xd5) b = disk.nextByte()
                if (disk.nextByte() != 0xaa)
                    break@start2
                if (disk.nextByte() != 0xad)
                    break@start2
                var c = 0
                repeat(342) {
                    val nb = disk.nextByte()
                    c = c.xor(nb)
                }
                val checksum1 = disk.nextByte()
                //            val checksum2 = disk.nextByte()
                println("  Data checksum: $checksum1")
                if (disk.nextByte() != 0xde) {
                    println("PROBLEM")
                }
                if (disk.nextByte(peek = true) != 0xaa) {
                    println("PROBLEM: expected \$aa but got " + disk.nextByte(peek = true))
                }
                disk.nextByte()
                if (disk.nextByte(peek = false) != 0xeb) {
                    println("PROBLEM")
                }
                sectorsRead++
            }
        }
    }
}

fun Byte.bit(n: Int) = this.toInt().bit(n)
fun Int.bit(n: Int) = this.and(1.shl(n)).shr(n)

class WozDisk(val ins: InputStream) {
    val MAX_TRACK = 160

    var quarterTrack = 0
    var bitPosition = 0
    val bytes = ins.readAllBytes()
    val woz = Woz(bytes)

    fun createBits(bytes: ByteArray): List<Int> {
        val result = arrayListOf<Int>()
        bytes.forEach {
            for (i in 0..8) {
                result.add(it.bit(7 - i))
            }
        }
        return result
    }

    fun peekByte(ahead: Int) = nextByte(true, ahead)

    fun nextBytes(count: Int) : List<Int> {
        val result = arrayListOf<Int>()
        repeat(count) {
            result.add(nextByte())
        }
        return result
    }

    fun peekBytes(count: Int, ahead: Int = 0): List<Int> {
        val position = bitPosition
        val result = arrayListOf<Int>()
        repeat(count) {
            result.add(nextByte())
        }
        bitPosition = position
        return result // .joinToString { it.h() }
    }

    fun nextByte(peek: Boolean = false, ahead: Int = 0): Int {
        var result = 0
        while (result and 0x80 == 0) {
            val nb = nextBit(peek, ahead * 8)
            result = result.shl(1).or(nb)
        }
        val rh = result.h()
        return result
    }

    var head_window = 0

    /**
     * @return the next byte and the stream size in bits
     */
    fun calculateCurrentByte(currentBitPosition: Int): Pair<Int, Int> {
        val tmapOffset = woz.tmap.offsetFor(quarterTrack)
        val trk = woz.trks.trks[tmapOffset]
        val streamSizeInBytes = (trk.bitCount / 8)
//        if (trk.startingBlock * 512 + position > streamSizeInBytes) {
//            println("Wrapping around")
//        }
        val relativeOffset = currentBitPosition / 8
        val byteOffset = (trk.startingBlock * 512 + relativeOffset) % streamSizeInBytes
        val byte = bytes[byteOffset].toInt().and(0xff)
//        println("  bitPosition $bitPosition: $relativeOffset byteOffset: $byteOffset byte: ${byte.h()}")
        return byte to trk.bitCount
    }

    fun peekBits(n: Int): List<Int> {
        val result = arrayListOf<Int>()
        repeat(n) {
            result.add(peekBit(it))
        }
        return result
    }

    fun peekBit(ahead: Int = 0) = nextBit(true, ahead)

    fun nextBit(peek: Boolean = false, ahead: Int = 0): Int {
        val (byte, streamSizeInBits) = calculateCurrentByte(bitPosition + ahead)
        head_window = head_window shl 1
        val bp = (bitPosition + ahead) % 8
        val nextWozBit = byte.bit(7 - bp)
//        head_window = head_window or nextWozBit
        if (! peek) bitPosition = (bitPosition + 1) % streamSizeInBits
//        val result = if (head_window and 0x0f !== 0x00) {
//            (head_window and 0x02) shr 1
//        } else {
//            1
//        }
//        println("Current byte ${byte.h()}, bit: $result ($bitPosition)")
        return nextWozBit
    }

//    fun next2Bits(): Int {
//        val (byte, streamSizeInBits) = calculateCurrentByte()
////        println("  current byte: ${byte.h()}")
//        val inPosition = bitPosition % 8
//        val result = when(inPosition) {
//            0 -> (byte and 0xc0).shr(6)
//            2 -> (byte and 0x30).shr(4)
//            4 -> (byte and 0xc).shr(2)
//            6 -> (byte and 3)
//            else -> TODO("should never happen")
//        }
//        bitPosition = (bitPosition + 2) % streamSizeInBits
////        println("Returning ${result.h()}")
//        return result
//    }

    fun incrementTrack() {
        quarterTrack = (quarterTrack + 1) % MAX_TRACK
    }

    fun decrementTrack() {
        if (quarterTrack == 0) quarterTrack = MAX_TRACK - 1
        else quarterTrack--
    }

}

class Woz(bytes: ByteArray) {
    lateinit var info: ChunkInfo
    lateinit var tmap: ChunkTmap
    lateinit var trks: ChunkTrks

    init {
        read(bytes)
    }

    class Stream(val bytes: ByteArray) {
        var i = 0

        fun hasNext() = i < bytes.size

        fun read1(): Int {
            return bytes[i++].toUByte().toInt()
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
            val result = bytes.slice(i..i + length - 1).toByteArray()
            i += length
            return result
        }

        override fun toString(): String {
            return "{Stream index:$${i.hh()} next:" + bytes[i].h() + " " + bytes[i + 1].h() + "}"
        }
    }

    data class Header(val name: String, val ff: Byte, val crc: ByteArray)

    fun readHeader(s: Stream): Header {
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
            if (! (trackNumber in 0..160)) {
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

    class BitStream(val bytes: ByteArray, start: Long) {
        var i = start.toInt()
        var currentBit = 7

        fun nextByte(): Int {
            if (i >= bytes.size) {
                println("Read a whole track")
                i = 0
            }
            return bytes[i++].toInt() and 0xff
        }

        fun nextBit(): Int {
            if (currentBit < 0) {
                currentBit = 7
                i++
            }
            val result = bytes[i].toInt().and(1.shl(currentBit)).shr(currentBit)
            println("Index: $i Byte: ${bytes[i].h()} Bit: $result")
            currentBit--
            return result
        }
    }

//    fun read(file: File): BitStream = read(file.inputStream())

    fun read(bytes: ByteArray) {
        val stream = Stream(bytes)
        println("Header: " + readHeader(stream))

        while (! this::info.isInitialized || ! this::tmap.isInitialized || ! this::trks.isInitialized) {
            val name = stream.read4String()
            val size = stream.read4()
            val result = when (name) {
                "INFO" -> info = ChunkInfo(stream, size)
                "TMAP" -> tmap = ChunkTmap(stream, size)
                "TRKS" -> trks = ChunkTrks(stream, size)
                else -> Chunk(name, size)
            }
        }
//        val offset = tmap.offsetFor(30)
//        val trackInfo = trks.trks[offset.toInt()]
//        return BitStream(bytes, trackInfo.startingBlock * 512)
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

val READ_TABLE_ = listOf(
        0x00, 0x01, 0x98, 0x99, 0x02, 0x03, 0x9c, 0x04, 0x05, 0x06,
        0xa0, 0xa1, 0xa2, 0xa3, 0xa4, 0xa5, 0x07, 0x08, 0xa8, 0xa9, 0xaa, 0x09, 0x0a, 0x0b, 0x0c, 0x0d,
        0xb0, 0xb1, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0xb8, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a,
        0xc0, 0xc1, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0x1b, 0xcc, 0x1c, 0x1d, 0x1e,
        0xd0, 0xd1, 0xd2, 0x1f, 0xd4, 0xd5, 0x20, 0x21, 0xd8, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28,
        0xe0, 0xe1, 0xe2, 0xe3, 0xe4, 0x29, 0x2a, 0x2b, 0xe8, 0x2c, 0x2d, 0x2e, 0x2f, 0x30, 0x31, 0x32,
        0xf0, 0xf1, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0xf8, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f
)

val INDICES = listOf(
        0x08, 0x9, 0xc,
        0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x18, 0x19, 0x1a,
        0x20, 0x21, 0x28,
        0x30, 0x31, 0x32, 0x34, 0x35, 0x38, 0x39, 0x3a, 0x3c,
        0x40, 0x41, 0x42, 0x44, 0x45, 0x48,
        0x50, 0x51, 0x52, 0x53, 0x54, 0x58,
        0x60, 0x61, 0x68
)