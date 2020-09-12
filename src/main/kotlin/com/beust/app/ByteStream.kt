package com.beust.app

import java.util.ArrayList

class ByteStream(private val bitBuffer: List<Int>) : IByteStream {
    override var position: Int = 0

    override fun nextBytes(n: Int): List<Int> {
        val result = arrayListOf<Int>()
        repeat(n) {
            result.add(next())
        }
        return result
    }

    private fun next(): Int {
        var result = 0
        var i = position
        while (result < 0x80) {
            result = result.shl(1).or(bitBuffer[i])
            i = (i + 1) % bitBuffer.size
        }
        position = i
        return result
    }
}
