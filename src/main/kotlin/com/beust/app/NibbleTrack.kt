package com.beust.app

import com.beust.swt.ByteBufferWindow

fun main() {
    val disk = WozDisk("", DISKS[1].inputStream())
    NibbleTrack(disk.bitStream, disk.phaseSizeInBits(0))
}

interface IMarkFinders {
    fun isAddressPrologue(bytes: List<Int>): Boolean = bytes == listOf(0xd5, 0xaa, 0x96)
    fun isAddressEpilogue(bytes: List<Int>): Boolean = bytes == listOf(0xde, 0xaa)
    fun isDataPrologue(bytes: List<Int>): Boolean = bytes == listOf(0xd5, 0xaa, 0xad)
    fun isDataEpilogue(bytes: List<Int>): Boolean = bytes == listOf(0xde, 0xaa)
}

class DefaultMarkFinders: IMarkFinders

class BouncingMarkFinders: IMarkFinders {
    override fun isAddressPrologue(bytes: List<Int>)
        = (bytes[0] == 0xd4 || bytes[0] == 0xd5) && bytes[1] == 0xaa && bytes[2] == 0x96
    override fun isAddressEpilogue(bytes: List<Int>) = bytes == listOf(0xda, 0xaa)
    override fun isDataEpilogue(bytes: List<Int>) = bytes == listOf(0xda, 0xaa)
}

class NibbleTrack(bitStream: IBitStream, val sizeInBits: Int) {
    val bytes = arrayListOf<ByteBufferWindow.TimedByte>()

    private val tmpBytes = arrayListOf<ByteBufferWindow.TimedByte>()

    init {
        fun peekZeros(): Int {
            var result = 0
            bitStream.save()
            while (bitStream.nextBit() == 0) {
                result++
            }
            bitStream.restore()
            return result
        }

        var done = false
        var bitsRead = 0
        var readCounts = 0
        var start = -1
        var end = -1
        while(! done) {
            var byte = 0
            while (byte and 0x80 == 0) {
                val pair = bitStream.nextBit()
                bitsRead++
                if (bitsRead % sizeInBits == 0) {
                    readCounts++
                    if (readCounts == 1) {
                        start = tmpBytes.size
                    } else if (readCounts == 2) {
                        end = tmpBytes.size
                        done = true
                    }
                }
                byte = byte.shl(1).or(pair)
            }
            val zeros = peekZeros()
            tmpBytes.add(ByteBufferWindow.TimedByte(byte, zeros))
        }
        bytes.addAll(tmpBytes.slice(start..end))
        println("Final track: " + bytes.size*8 + " bitCount:" + sizeInBits)
        tmpBytes.clear()
    }

    private fun peek(pos: Int, count: Int): List<Int> {
        val result = arrayListOf<Int>()
        var i = pos % bytes.size
        repeat(count) {
            result.add(bytes[i].byte)
            i = (i + 1) % bytes.size

        }
        return result
    }

    private fun findNextChunk(position: Int,
            isPrologue: (List<Int>) -> Boolean, isEpilogue: (List<Int>) -> Boolean): IntRange?
    {
        var i = position
        var tries = 0
        while (tries < bytes.size && ! isPrologue(peek(i, 3))) {
            i = (i + 1) % bytes.size
            tries++
        }
        return if (tries < bytes.size) {
            tries = 0
            val start = i + 3
            while (tries < bytes.size && !isEpilogue(peek(i, 2))) {
                i = (i + 1) % bytes.size
                tries++
            }
            if (tries < bytes.size) {
                IntRange(start, i - 1)
            } else {
                null
            }
        } else {
            null
        }
    }

    data class TrackRange(val isAddress: Boolean, val range: IntRange)

    fun analyze(markFinders: IMarkFinders = DefaultMarkFinders()): List<TrackRange> {
        val result = arrayListOf<TrackRange>()
        var i = 0
        var sectors = 0
        while (sectors < 16) {
            val addressRange = findNextChunk(i, markFinders::isAddressPrologue, markFinders::isAddressEpilogue)
            if (addressRange != null) {
                i = addressRange.endInclusive
                result.add(TrackRange(true, addressRange))
            }

            val dataRange = findNextChunk(i, markFinders::isDataPrologue, markFinders::isDataEpilogue)
            if (dataRange != null) {
                i = dataRange.endInclusive
                result.add(TrackRange(false, dataRange))

                // Verify checksum
                var chesksum = 0
                val start = dataRange.start
                val end = dataRange.endInclusive
                for (j in start..end) {
                    chesksum = chesksum.xor(READ_TABLE[bytes[j].byte]!!)
                }
                println("Sector $sectors, address: $addressRange, data: $dataRange checksum: $chesksum")
            }
            sectors++
        }

        return result
    }

    private var byteIndex = 0
    fun nextByte(): ByteBufferWindow.TimedByte {
        val result = bytes[byteIndex]
        byteIndex = (byteIndex + 1) % bytes.size
        return result
    }
}