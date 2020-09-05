package com.beust.app

import com.beust.sixty.h
import com.beust.sixty.hh
import java.io.File
import java.io.InputStream

fun main() {
    Woz().read(Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz").openStream())
}

class Woz {
    class Stream(val bytes: ByteArray) {
        var i = 0

        fun hasNext() = i < bytes.size

        fun read1(): Long {
            return bytes[i++].toUByte().toLong()
        }

        fun read4Bytes(): ByteArray {
            val result = bytes.slice(i .. i + 3)
            i += 4
            return result.toByteArray()
        }

        fun read4(): Long {
            val result = bytes[i].toUByte().toULong()
                    .or(bytes[i + 1].toUByte().toULong().shl(8))
                    .or(bytes[i + 2].toUByte().toULong().shl(16))
                    .or(bytes[i + 3].toUByte().toULong().shl(24))
            i += 4
            return result.toLong()

        }

        fun read2(): Long = read1().or(read1().shl(8))

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

    open class Chunk(val name: String, val size: Long) {
        override fun toString(): String {
            return "{$name size=$size}"
        }
    }

    class ChunkInfo(private val stream: Stream, size: Long): Chunk("INFO", size) {
        init {
            stream.readN(size.toInt())
        }
    }

    class ChunkTmap(private val stream: Stream, size: Long): Chunk("TMAP", size) {
        private val map = hashMapOf<Int, Long>()
        init {
            repeat(160) {
                map[it] = stream.read1()
            }
        }

        /**
         * Track number multiplied by 100:  0 -> 0, 0.25 -> 25, 0.50 -> 500
         */
        fun offsetFor(trackNumber: Int): Long {
            return map[trackNumber / 25]!!
        }

        override fun toString(): String {
            return "{TMAP size=$size 0:" + offsetFor(0) + " 1:" + offsetFor(25) + " 2:" + offsetFor(50) + "...}"
        }
    }

    class ChunkTrks(private val stream: Stream, size: Long): Chunk("TRKS", size) {
        data class Trk(val startingBlock: Long, val blockCount: Long, val bitCount: Long)
        val trks = arrayListOf<Trk>()
        init {
            repeat(160) {
                val sb = stream.read2()
                val bc = stream.read2()
                val bitCount = stream.read4()
                val trk = Trk(sb, bc, bitCount)
                trks.add(trk)
                println("Added TRK #$it: " + trk)
            }
            println("BITS now")
        }
    }

    class BitStream(val bytes: ByteArray, start: Long) {
        var i = start.toInt()
        var currentBit = 7

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

    fun read(file: File) = read(file.inputStream())

    fun read(ins: InputStream) {
        val bytes = ins.readBytes()
        val stream = Stream(bytes)
        println("Header: " + readHeader(stream))

        var info: ChunkInfo? = null
        var tmap: ChunkTmap? = null
        var trks: ChunkTrks? = null
        while (info == null || tmap == null || trks == null) {
            val name = stream.read4String()
            val size = stream.read4()
            val result = when (name) {
                "INFO" -> info = ChunkInfo(stream, size)
                "TMAP" -> tmap = ChunkTmap(stream, size)
                "TRKS" -> trks = ChunkTrks(stream, size)
                else -> Chunk(name, size)
            }
        }
        val offset = tmap.offsetFor(30)
        val trackInfo = trks.trks[offset.toInt()]
        val bs = BitStream(bytes, trackInfo.startingBlock * 512)
        repeat(32) {
            bs.nextBit()
        }
        println(trackInfo)
    }
}
