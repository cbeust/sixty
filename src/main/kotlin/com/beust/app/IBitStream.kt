package com.beust.app

import com.beust.sixty.bit
import com.beust.sixty.hh

/**
 * A circular buffer of bits
 */
interface IBitStream {
    var bitPosition: Int
    val sizeInBits: Int
    fun save()
    fun restore()
    fun nextBit(): Int
}

interface IPhasedBitStream: IBitStream {
    val phase: Int  // 0..159
    val mappedTrack: Int  // 0..39
}

/**
 * An IBitStream backed up by a list of bits.
 */
class BitBitStream(private val bytes: List<Byte>, override val phase: Int, override val mappedTrack: Int,
        val bitCount: Int = bytes.size * 8): IPhasedBitStream {
    override fun toString(): String = "{BitBitStream phase:$phase mappedTrack:$mappedTrack"

    override var bitPosition = 0
    private var saved = -1
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
                byteIndex = (byteIndex + 1) % bytes.size
                currentByte = bytes[byteIndex]
            }
            i++
        }
        ""
    }

    override val sizeInBits: Int
        get() = bits.size

    override fun save() { saved = bitPosition}
    override fun restore() { bitPosition = saved }
    override fun nextBit(): Int = bits[bitPosition].let {
            bitPosition = (bitPosition + 1) % bits.size
        if (DEBUG_BITS) println("NEW BIT POSITION: " + bitPosition.hh())
            return it
        }
}
