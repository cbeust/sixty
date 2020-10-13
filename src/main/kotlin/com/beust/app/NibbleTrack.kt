package com.beust.app

import com.beust.swt.ByteBufferWindow

class NibbleTrack(bitStream: IBitStream, sizeInBits: Int) {
    private val bytes = arrayListOf<ByteBufferWindow.TimedByte>()
    init {
        var position = 0
        var index = 0

        fun peekZeros(): Int {
            var result = 0
            var p = position
            while (bitStream.next(p).second == 0) {
                result++
                p++
            }
            return result
        }

        repeat(sizeInBits) {
            var byte = 0
            var newPosition = -1
            while (byte and 0x80 == 0) {
                val pair = bitStream.next(position)
                byte = byte.shl(1).or(pair.second)
                newPosition = pair.first
            }
            position = newPosition
            val zeros = peekZeros()
            bytes.add(ByteBufferWindow.TimedByte(byte, zeros))
        }
    }

}