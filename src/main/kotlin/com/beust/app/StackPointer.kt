package com.beust.app

import com.beust.sixty.Memory
import com.beust.sixty.h
import kotlin.math.max

/**
 * Implements the 6502 stack: in memory 0x100-0x1ff, starting at 0xff and decreasing.
 */
class StackPointer(private val memory: Memory) {
    var S: Int = 0xff
    private val address = 0x100

    private fun inc() { S = (S + 1) and 0xff }
    private fun dec() { S = (S - 1) and 0xff }

    fun pushByte(a: Byte) {
        memory[address + S] = a.toInt()
        dec()
    }

    fun popByte(): Byte {
        inc()
        return memory[address + S].toByte()
    }

    fun pushWord(a: Int) {
        // High byte fist
        memory[address + S] = a.and(0xff00).shr(8)
        dec()
        memory[address + S] = a.and(0xff)
        dec()
    }

    fun popWord(): Int {
        inc()
        val low = memory[address + S]
        inc()
        val high = memory[address + S]
        val result = low.or(high.shl(8))
        return result
    }

    fun isEmpty(): Boolean = S == 0xff

    override fun toString(): String {
        val result = StringBuffer("{$${S.h()} stack:[")
        (0xff downTo max(S + 1, 0xf8)).forEach {
            val ad = address + it
            result.append("$${ad.h()}:$" + memory[ad].h())
            result.append(" ")
        }
        result.append("]}")
        return result.toString()
    }
}
