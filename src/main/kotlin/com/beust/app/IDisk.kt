package com.beust.app

interface IDisk {
    /** Position in bytes */
    val position: Int
    fun peekBytes(count: Int): List<Int>
    fun nextByte(): Int
    fun incTrack()
    fun decTrack()
    fun nextBytes(n: Int) : List<Int> {
        val result = arrayListOf<Int>()
        repeat(n) { result.add(nextByte())}
        return result
    }
}