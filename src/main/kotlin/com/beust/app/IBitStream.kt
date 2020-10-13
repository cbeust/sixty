package com.beust.app

import com.beust.sixty.bit

interface IBitStream {
    val sizeInBits: Int

    /**
     * @return a pair of the new index and the returned bit.
     */
    fun next(position: Int): Pair<Int, Int>
}

class BitBitStream(val bytes: List<Byte>, bitCount: Int = bytes.size * 8): IBitStream {
    private val bits = arrayListOf<Int>()
    init {
        var i = 0
        var bitIndex = 0
        var byteIndex = 0
        var currentByte = bytes[0]
        while (i < bitCount) {
            bits.add(currentByte.bit(7 - bitIndex))
            bitIndex++
            if (bitIndex == 8) {
                bitIndex = 0
                byteIndex++
                currentByte = bytes[byteIndex]
            }
            i++
        }
        ""
    }

    override val sizeInBits: Int
        get() = bits.size

    override fun next(position: Int): Pair<Int, Int> {
        return Pair((position + 1) % bits.size, bits[position])
    }
}
