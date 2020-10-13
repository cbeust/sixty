package com.beust.app

import com.beust.swt.ByteBufferWindow

fun main() {
    val disk = WozDisk("", DISKS[10].inputStream())
    NibbleTrack(disk.bitStream, disk.phaseSizeInBits(0), BouncingMarkFinders())
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

class NibbleTrack(bitStream: IBitStream, private val sizeInBits: Int,
        private val markFinders: IMarkFinders = DefaultMarkFinders()) {
    private val tmpBytes = arrayListOf<ByteBufferWindow.TimedByte>()
    private val bytes = arrayListOf<ByteBufferWindow.TimedByte>()
    init {
        var position = 0

        fun peekZeros(): Int {
            var result = 0
            var p = position
            while (bitStream.next(p).second == 0) {
                result++
                p = (p + 1) % sizeInBits
            }
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
                val pair = bitStream.next(position)
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
                byte = byte.shl(1).or(pair.second)
                position = pair.first
            }
            val zeros = peekZeros()
            tmpBytes.add(ByteBufferWindow.TimedByte(byte, zeros))
        }
        bytes.addAll(tmpBytes.slice(start..end))
        println("Final track: " + bytes.size*8 + " bitCount:" + sizeInBits)
        tmpBytes.clear()
        analyze()
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
            isPrologue: (List<Int>) -> Boolean, isEpilogue: (List<Int>) -> Boolean): IntRange
    {
        var i = position
        while (! isPrologue(peek(i, 3))) i = (i + 1) % bytes.size
        val start = i + 3
        while (! isEpilogue(peek(i, 2))) i = (i + 1) % bytes.size
        return IntRange(start, i - 1)
    }

    private fun analyze() {
        var i = 0
        var sectors = 0
        while (sectors < 16) {
            val addressRange = findNextChunk(i, markFinders::isAddressPrologue, markFinders::isAddressEpilogue)
            i = addressRange.endInclusive
            val dataRange = findNextChunk(i, markFinders::isDataPrologue, markFinders::isDataEpilogue)
            i = addressRange.endInclusive
            var chesksum = 0
            val start = dataRange.start
            val end = dataRange.endInclusive
            for (j in start..end) {
                chesksum = chesksum.xor(READ_TABLE[bytes[j].byte]!!)
            }
            println("Sector $sectors, address: $addressRange, data: $dataRange checksum: $chesksum")
            sectors++
        }
    }

}