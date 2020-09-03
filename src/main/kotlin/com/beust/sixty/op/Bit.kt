package com.beust.sixty.op

import com.beust.sixty.*

abstract class BitBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        val op: Operand)
    : InstructionBase(c)
{
    override fun run() {
        cpu.P.Z = (cpu.A and op.get()) == 0
        cpu.P.N = (op.get() and 0x80) != 0
        cpu.P.V = (op.get() and 0x40) != 0
    }
    override fun toString(): String = "BIT${op.name}"
}

/** 0x24, BIT $12 */
class BitZp(c: Computer): BitBase(c, BIT_ZP, 2, 3, OperandZp(c))

/** 0x2c, BIT $1234 */
class BitAbsolute(c: Computer): BitBase(c, BIT_ABS, 3, 4, OperandAbsolute(c))

