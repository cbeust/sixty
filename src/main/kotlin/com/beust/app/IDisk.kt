package com.beust.app

interface IByteStream {
    /** Position in bytes */
    var position: Int

    fun nextBytes(n: Int): List<Int>

    fun peekBytes(n: Int): List<Int> {
        val saved = position
        val result = nextBytes(n)
        position = saved
        return result
    }

    fun nextByte() = nextBytes(1).first()
}
interface IDisk {
    val path: String

    fun nextBit(): Int
    fun incTrack()
    fun decTrack()
    fun peekBytes(n: Int): List<Int>

    fun nextByte(): Int {
        var result = 0
        while (result < 0x80) {
            result = result.shl(1).or(nextBit()).and(0xff)
        }
        return result
    }

    fun nextBytes(n: Int): List<Int> {
        val result = arrayListOf<Int>()
        repeat(n) {
            result.add(nextByte())
        }
        return result
    }

}

