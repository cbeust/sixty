package com.beust.sixty.op

import com.beust.sixty.BIT_ABS
import com.beust.sixty.BIT_ZP
import com.beust.sixty.Computer
import com.beust.sixty.InstructionBase

abstract class BitBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : InstructionBase(c)
{
    abstract var value: Int
    abstract val name: String

    override fun run() {
        val value = cpu.A.and(value)
        cpu.P.setNZFlags(value)
        cpu.P.V = if (value.and(1.shl(6)) != 0) true else false
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

