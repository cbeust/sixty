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
    fun nextBit(): Int
    fun incTrack()
    fun decTrack()
}

