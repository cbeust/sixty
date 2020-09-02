package com.beust.sixty.op

import com.beust.sixty.*

abstract class BitBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : InstructionBase(c)
{
    abstract var value: Int
    abstract val name: String

    override fun run() {
        cpu.P.Z = (cpu.A and value) == 0
        cpu.P.N = (value and 0x80) != 0
        cpu.P.V = (value and 0x40) != 0
    }
    override fun toString(): String = "BIT${name}"
}

/** 0x24, BIT $12 */
class BitZp(c: Computer): BitBase(c, BIT_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** 0x2c, BIT $1234 */
class BitAbsolute(c: Computer): BitBase(c, BIT_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

