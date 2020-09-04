package com.beust.sixty.op

import com.beust.sixty.*

abstract class BitBase(override val opCode: Int, override val size: Int, override val timing: Int,
        val a: Addressing)
    : InstructionBase("BIT", opCode, size, timing, a)
{
    override fun run(c: Computer, op: Operand) = with(c) {
        cpu.P.Z = (cpu.A and op.get()) == 0
        cpu.P.N = (op.get() and 0x80) != 0
        cpu.P.V = (op.get() and 0x40) != 0
    }
}

/** 0x24, BIT $12 */
class BitZp: BitBase(BIT_ZP, 2, 3, Addressing.ZP)

/** 0x2c, BIT $1234 */
class BitAbsolute: BitBase(BIT_ABS, 3, 4, Addressing.ABSOLUTE)

