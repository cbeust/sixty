package com.beust.app

import com.beust.sixty.IStackPointer
import com.beust.sixty.Memory

class Apple2StackPointer(override var S: Int = 0xff, private val memory: Memory) : IStackPointer {
    private val address = 0x100

    override fun pushByte(a: Byte) {
        memory[address + S--] = a.toInt()
    }

    override fun popByte(): Byte = memory[address + S++].toByte()

    override fun pushWord(a: Int) {
        memory[address + S--] = a.and(0xff)
        memory[address + S--] = a.and(0xff00).shr(8)
    }

    override fun popWord(): Int {
        val result = memory[address + ++S].shl(8).or(memory[address + ++S])
        return result
    }

    override fun isEmpty(): Boolean = S == 0xff

    override fun toString() = "{S=$S}"
}
