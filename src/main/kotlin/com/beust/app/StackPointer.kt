package com.beust.app

import com.beust.sixty.Memory
import com.beust.sixty.h

/**
 * Implements the 6502 stack: in memory 0x100-0x1ff, starting at 0xff and decreasing.
 */
class StackPointer(private val memory: Memory) {
    var S: Int = 0xff
    private val address = 0x100

    fun pushByte(a: Byte) {
        memory[address + S--] = a.toInt()
    }

    fun popByte(): Byte {
        return memory[address + ++S].toByte()
    }

    fun pushWord(a: Int) {
        // High byte fist
        memory[address + S--] = a.and(0xff00).shr(8)
        memory[address + S--] = a.and(0xff)
    }

    fun popWord(): Int {
        val result = memory[address + ++S].or(memory[address + ++S].shl(8))
        return result
    }

    fun isEmpty(): Boolean = S == 0xff

    override fun toString(): String {
        val result = StringBuffer("{$${S.h()} stack:[")
        (0xff downTo S + 1).forEach {
            val ad = address + it
            result.append("$${ad.h()}:$" + memory[ad].h())
            result.append(" ")
        }
        result.append("]}")
        return result.toString()
    }
}
