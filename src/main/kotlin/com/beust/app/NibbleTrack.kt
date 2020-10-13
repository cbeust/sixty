package com.beust.app

import com.beust.swt.ByteBufferWindow

fun main() {
    val disk = WozDisk("", DISKS[10].inputStream())
    NibbleTrack(disk.bitStream, disk.phaseSizeInBits(0))
}

class NibbleTrack(bitStream: IBitStream, sizeInBits: Int) {
    private val tmpBytes = arrayListOf<ByteBufferWindow.TimedByte>()
    private val bytes = arrayListOf<ByteBufferWindow.TimedByte>()
    init {
        var position = 0
        var index = 0

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
                    println("Wrapping around")
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
        return bytes.slice(pos..pos + count - 1).map { it.byte }
    }

    private fun analyze() {
        var i = 0
        var sectors = 0
        while (sectors <= 16) {
            var p3 = peek(i, 3)
            while (p3 != listOf(0xd5, 0xaa, 0xad)) {
                i = (i + 1) % bytes.size
                p3 = peek(i, 3)
            }
            val start = i
            while (peek(i, 2) != listOf(0xda, 0xaa)) i = (i + 1) % bytes.size
            println("Found sector $sectors at " + start + "-" + i)
            sectors++
        }
    }
}