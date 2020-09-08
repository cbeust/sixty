package com.beust.app

import com.beust.sixty.h
import com.beust.sixty.hh
import java.io.File
import java.io.InputStream


fun main() {
    val ins = Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz")!!.openStream()
    val ins2 = File("d:\\pd\\Apple DIsks\\woz2\\The Apple at Play.woz").inputStream()
    val disk = WozDisk(ins)

    fun pair() = disk.nextByte().shl(1).and(disk.nextByte()).and(0xff)

    repeat(13) {
        run {
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
        }

        val buffer = IntArray(342)
        var checksum = 0
        for (i in buffer.indices) {
            val b = disk.nextByte()
            if (READ_TABLE[b] == null) {
                println("INVALID NIBBLE")
            }
            checksum = checksum xor READ_TABLE[b]!!
            if (i < 86) {
                buffer[buffer.size - i - 1] = checksum
            } else {
                buffer[i - 86] = checksum
            }
        }
        checksum = checksum xor READ_TABLE[disk.nextByte()]!!
        if (checksum != 0) {
            TODO("BAD CHECKSUM")
        }

        val sectorData = IntArray(256)
        for (i in sectorData.indices) {
            val b1: Int = buffer[i]
            val lowerBits: Int = buffer.size - i % 86 - 1
            val b2: Int = buffer[lowerBits]
            val shiftPairs = i / 86 * 2
            // shift b1 up by 2 bytes (contains bits 7-2)
            // align 2 bits in b2 appropriately, mask off anything but
            // bits 0 and 1 and then REVERSE THEM...
            val reverseValues = intArrayOf(0x0, 0x2, 0x1, 0x3)
            val b = b1 shl 2 or reverseValues[b2 shr shiftPairs and 0x03]
            sectorData[i] = b
        }

        if (disk.nextBytes(3) != listOf(0xde, 0xaa, 0xeb)) {
            TODO("Didn't find closing for data")
        }
        println("  Successfully read track")
    }

    repeat(100) {
        print(disk.nextByte().h() + " ")
    }
    println("")

//    fun readTest() {
//        var sectorsRead = 0
//        while (sectorsRead < 13) {
//            var b = disk.nextByte()
//            start@ while (true) {
//                while (b != 0xd5) b = disk.nextByte()
//                if (disk.nextByte() != 0xaa) break@start
//                if (disk.nextByte() != 0x96) break@start
//                val volume = pair()
//                val track = pair()
//                val sector = pair()
//                val checksum = pair()
//                val expected = volume.xor(track).xor(sector)
//                println("Offset " + (0x600 + (disk.bitPosition / 8)).hh()
//                        + " volume: ${volume.h()} track: ${track.h()} sector: ${sector.h()}"
//                        + " checksum: $checksum expected: $expected")
//                if (disk.nextByte() != 0xde) {
//                    println("PROBLEM")
//                }
//                if (disk.nextByte() != 0xaa) {
//                    println("PROBLEM")
//                }
//            }
//            b = disk.nextByte()
//            println("next: " + disk.peekBytes(10))
//            start2@ while (true) {
//                while (b != 0xd5) b = disk.nextByte()
//                if (disk.nextByte() != 0xaa)
//                    break@start2
//                if (disk.nextByte() != 0xad)
//                    break@start2
//                var c = 0
//                repeat(342) {
//                    val nb = disk.nextByte()
//                    c = c.xor(nb)
//                }
//                val checksum1 = disk.nextByte()
//                //            val checksum2 = disk.nextByte()
//                println("  Data checksum: $checksum1")
//                if (disk.nextByte() != 0xde) {
//                    println("PROBLEM")
//                }
//                if (disk.nextByte(peek = true) != 0xaa) {
//                    println("PROBLEM: expected \$aa but got " + disk.nextByte(peek = true))
//                }
//                disk.nextByte()
//                if (disk.nextByte(peek = false) != 0xeb) {
//                    println("PROBLEM")
//                }
//                sectorsRead++
//            }
//        }
//    }
}

fun Byte.bit(n: Int) = this.toInt().bit(n)
fun Int.bit(n: Int) = this.and(1.shl(n)).shr(n)

class WozDisk(ins: InputStream) {
    private val MAX_TRACK = 160

    private var quarterTrack = 0
//    var bitPosition = 0
    val bytes = ins.readAllBytes()
    val woz = Woz(bytes)
    val bitStream = BitStream(bytes.slice(0x600 until bytes.size).toByteArray())

    fun createBits(bytes: ByteArray): List<Int> {
        val result = arrayListOf<Int>()
        bytes.forEach {
            for (i in 0..8) {
                result.add(it.bit(7 - i))
            }
        }
        return result
    }

    fun peekBytes(count: Int): ArrayList<Int> {
        val result = arrayListOf<Int>()
        bitStream.save()
        repeat(count) {
            result.add(nextByte())
        }
        bitStream.restore()
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

//    fun peekBytes(count: Int, ahead: Int = 0): List<Int> {
//        val position = bitPosition
//        val result = arrayListOf<Int>()
//        repeat(count) {
//            result.add(nextByte())
//        }
//        bitPosition = position
//        return result // .joinToString { it.h() }
//    }

    fun nextByte(peek: Boolean = false, ahead: Int = 0): Int {
        var result = 0
        if (peek) {
            bitStream.save()
        }
        while (result and 0x80 == 0) {
            val nb = bitStream.next()
//            val nb = nextBit(peek, ahead * 8)
            result = result.shl(1).or(nb)
        }
        if (peek) {
            bitStream.restore()
        }
        val rh = result.h()
        return result
    }

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

//    fun peekBits(n: Int): List<Int> {
//        val result = arrayListOf<Int>()
//        repeat(n) {
//            result.add(peekBit(it))
//        }
//        return result
//    }
//
//    fun peekBit(ahead: Int = 0) = nextBit(true, ahead)
//
//    var head_window = 0
//
//    fun nextBit(peek: Boolean = false, ahead: Int = 0): Int {
//        val (byte, streamSizeInBits) = calculateCurrentByte(bitPosition + ahead)
//        val bp = (bitPosition + ahead) % 8
//        val nextWozBit = byte.bit(7 - bp)
//        head_window = head_window shl 1
//        head_window = head_window or nextWozBit
//        if (! peek) bitPosition = (bitPosition + 1) % streamSizeInBits
//        val result = if ((head_window and 0x0f) !== 0x00) {
//            (head_window and 0x02) shr 1
//        } else {
//            1
//        }
////        println("Current byte ${byte.h()}, bit: $result ($bitPosition)")
//        return nextWozBit
////        return result
//    }

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
            return bytes[i++].toInt()
        }

        fun read4Bytes(): ByteArray {
            val result = bytes.slice(i .. i + 3)
            i += 4
            return result.toByteArray()
        }
        
        fun read4(): Int {
            val result = bytes[i].toInt()
                    .or(bytes[i + 1].toInt().shl(8))
                    .or(bytes[i + 2].toInt().shl(16))
                    .or(bytes[i + 3].toInt().shl(24))
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

        fun next(): Int {
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
            when (name) {
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

class BitStream(val bytes: ByteArray) {
    var bitPosition = 0
    var saved = 0

    fun next(): Int {
        val byteIndex = bitPosition / 8
        val bitIndex = bitPosition % 8
        val byte = bytes[byteIndex]
        val result = byte.bit(7 - bitIndex)
        bitPosition = (bitPosition + 1) % bytes.size
        return result
    }

    fun save() { saved = bitPosition }
    fun restore() { bitPosition = saved }
}

class _BitStream(val bytes: ByteArray) {
    var position = 0
    val bits = arrayListOf<Int>()
    var saved = 0

    init {
        bytes.forEach { b ->
            var i = 7
            repeat(8) {
                bits.add(b.bit(i--))
            }
        }
        ""
    }

    fun next(): Int {
        val result = bits[position]
        position = (position + 1) % bytes.size
        return result
    }

    fun save() { saved = position }
    fun restore() { position = saved }

    fun peek(count: Int): List<Int> {
        var saved = position
        val result = arrayListOf<Int>()
        repeat(count) {
            result.add(next())
        }
        position = saved
        return result
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
