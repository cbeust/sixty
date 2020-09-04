package com.beust.sixty.op

import com.beust.sixty.*

abstract class DecBase(override val opCode: Int, override val size: Int, override val timing: Int,
        val a: Addressing)
    : InstructionBase("DEC", opCode, size, timing, a)
{
    override fun run(c: Computer, op: Operand) = with(c) {
        op.set(op.get() - 1)
        cpu.P.setNZFlags(op.get())
    }
}

/** 0xc6, DEC $12 */
class DecZp: DecBase(DEC_ZP, 2, 5, Addressing.ZP)

/** 0xd6, Dec $12,X */
class DecZpX: DecBase(DEC_ZP_X, 2, 6, Addressing.ZP_X)

/** 0xce, Dec $1234 */
class DecAbsolute: DecBase(DEC_ABS, 3, 6, Addressing.ABSOLUTE)

/** 0xde, Dec $1234,X */
class DecAbsoluteX: DecBase(DEC_ABS_X, 3, 7, Addressing.ABSOLUTE_X)


