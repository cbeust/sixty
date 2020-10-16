package com.beust.app

import com.beust.sixty.ERROR
import java.io.File
import java.io.InputStream

interface IDisk: IBitStream {
    companion object {
        fun create(name: String, ins: InputStream?): IDisk? = when {
            ins == null -> null
            name.endsWith(".woz") -> WozDisk(name, ins)
            name.endsWith(".dsk") -> DskDisk(name, ins)
            else -> ERROR("Unsupported disk format: $name")
        }

        fun create(file: File?): IDisk? = create(file?.name ?: "<unknown>", file?.inputStream())
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


