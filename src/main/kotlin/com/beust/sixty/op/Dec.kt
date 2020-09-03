package com.beust.sixty.op

import com.beust.sixty.*

abstract class DecBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        val op: Operand)
    : InstructionBase(c)
{
    override fun run() {
        op.set(op.get() - 1)
        cpu.P.setNZFlags(op.get())
    }
    override fun toString(): String = "DEC${op.name}"
}

/** 0xc6, DEC $12 */
class DecZp(c: Computer): DecBase(c, DEC_ZP, 2, 5, OperandZp(c))

/** 0xd6, Dec $12,X */
class DecZpX(c: Computer): DecBase(c, DEC_ZP_X, 2, 6, OperandZpX(c))

/** 0xce, Dec $1234 */
class DecAbsolute(c: Computer): DecBase(c, DEC_ABS, 3, 6, OperandAbsolute(c))

/** 0xde, Dec $1234,X */
class DecAbsoluteX(c: Computer): DecBase(c, DEC_ABS_X, 3, 7, OperandAbsoluteX(c))


