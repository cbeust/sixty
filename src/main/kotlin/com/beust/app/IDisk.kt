package com.beust.app

import com.beust.sixty.ERROR
import java.io.File

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
    companion object {
        fun create(file: File?): IDisk? = when {
                file == null -> null
                file.name.endsWith(".woz") -> WozDisk(file.absolutePath, file.inputStream())
                file.name.endsWith(".dsk") -> DskDisk(file.absolutePath, file.inputStream())
                else -> ERROR("Unsupported disk format: $file")
            }
    }

    val name: String

    fun nextBit(): Int
    fun peekZeroBitCount(): Int

    fun incTrack()
    fun decTrack()
    fun peekBytes(n: Int): List<Int>
    /** Phase: 0-159 */
    fun phaseSizeInBits(phase: Int): Int

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

abstract class BaseDisk: IDisk {
    override fun toString(): String = name
}


