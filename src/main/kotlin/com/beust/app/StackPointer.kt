package com.beust.app

import com.beust.sixty.IStackPointer
import com.beust.sixty.Memory
import com.beust.sixty.h

class StackPointer(private val memory: Memory) : IStackPointer {
    override var S: Int = 0xff
    private val address = 0x100

    override fun pushByte(a: Byte) {
        memory[address + S--] = a.toInt()
    }

    override fun popByte(): Byte {
        return memory[address + ++S].toByte()
    }

    override fun pushWord(a: Int) {
        memory[address + S--] = a.and(0xff)
        memory[address + S--] = a.and(0xff00).shr(8)
    }

    override fun popWord(): Int {
        val result = memory[address + ++S].shl(8).or(memory[address + ++S])
        return result
    }

    override fun isEmpty(): Boolean = S == 0xff

    override fun toString(): String {
        val result = StringBuffer("{Stack pointer: ${S.h()} stack:[")
        (0xff downTo S + 1).forEach {
            val ad = address + it
            result.append("$${ad.h()}: " + memory[ad].h())
            result.append(" ")
        }
        result.append("]}")
        return result.toString()
    }
}
