package com.beust.app

import com.beust.sixty.ERROR
import java.io.File

interface IDisk: IBitStream {
    companion object {
        fun create(file: File?): IDisk? = when {
                file == null -> null
                file.name.endsWith(".woz") -> WozDisk(file.absolutePath, file.inputStream())
                file.name.endsWith(".dsk") -> DskDisk(file.absolutePath, file.inputStream())
                else -> ERROR("Unsupported disk format: $file")
            }
    }

    val name: String

    fun peekZeroBitCount(): Int

    fun incPhase()
    fun decPhase()
    fun peekBytes(n: Int): List<Int>
    /** Phase: 0-159 */
    fun phaseSizeInBits(phase: Int): Int

    fun nextByte(): Int

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


