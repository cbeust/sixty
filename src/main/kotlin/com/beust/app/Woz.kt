package com.beust.app

import com.beust.sixty.h
import com.beust.sixty.hh
import com.beust.sixty.int
import java.io.File
import java.io.InputStream
import java.lang.IllegalArgumentException

fun main() {
    val ins = Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz").openStream()
    val ins2 = File("d:\\pd\\Apple DIsks\\woz2\\The Apple at Play.woz").inputStream()
    val disk = WozDisk(ins)
    var carry = 1
    fun rol(v: Int): Int {
        val result = (v.shl(1).or(carry)) and 0xff
        carry = if (v.and(0x80) != 0) 1 else 0
        return result
    }
    repeat(100) {
        var acc = 0
        repeat(4) {
            val b = disk.next2Bits()
//            println("Next 2 bits: $b")
            acc = acc.shl(2).or(b)
        }
//        println(" ==> ${acc.h()}")
    }
    repeat(10000) {
        var b = disk.nextByte()
        fun pair(): Int {
            val b1 = disk.nextByte()
            val b2 = disk.nextByte()
            println("     PAIRING " + b1.h() + " " + b2.h())
            return rol(b1).and(b2).and(0xff)
        }
        start@ while(true) {
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
        start2@ while(true) {
            while (b != 0xd5) b = disk.nextByte()
            if (disk.nextByte() != 0xaa) break@start2
            if (disk.nextByte() != 0xad) break@start2
            var c = 0
            repeat(342) {
                val nb = disk.nextByte()
                c = c.xor(nb)
            }
            val checksum = disk.nextByte()
            println("  Data checksum: $checksum")
            if (disk.nextByte() != 0xde) {
                println("PROBLEM")
            }
            if (disk.nextByte() != 0xaa) {
                println("PROBLEM")
            }
        }
    }
}

class WozDisk(val ins: InputStream) {
    val MAX_TRACK = 160

    var quarterTrack = 0
    var bitPosition = 0
    val bytes = ins.readAllBytes()
    val woz = Woz(bytes)

    fun nextByte(): Int {
        val a = next2Bits().shl(6)
        val b = next2Bits().shl(4)
        val c = next2Bits().shl(2)
        val d = next2Bits()
        val result = a or b or c or d
        return result
    }

    fun next2Bits(): Int {
        val tmapOffset = woz.tmap.offsetFor(quarterTrack)
        val trk = woz.trks.trks[tmapOffset]
        val streamSizeInBytes = (trk.bitCount / 8)
//        if (trk.startingBlock * 512 + position > streamSizeInBytes) {
//            println("Wrapping around")
//        }
        val byteOffset = (trk.startingBlock * 512 + (bitPosition / 8)) % streamSizeInBytes
        val byte = bytes[byteOffset].toInt().and(0xff)
//        println("  current byte: ${byte.h()}")
        val inPosition = bitPosition % 8
        val result = when(inPosition) {
            0 -> (byte and 0xc0).shr(6)
            2 -> (byte and 0x30).shr(4)
            4 -> (byte and 0xc).shr(2)
            6 -> (byte and 3)
            else -> TODO("should never happen")
        }
        bitPosition = (bitPosition + 2) % (streamSizeInBytes * 8)
//        println("Returning ${result.h()}")
        return result
    }

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
