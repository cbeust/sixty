package com.beust.sixty.op

import com.beust.sixty.*

abstract class IncBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        val op: Operand)
    : InstructionBase(c)
{
    override fun run() {
        op.set(op.get() + 1)
        cpu.P.setNZFlags(op.get())
    }
    override fun toString(): String = "INC${op.name}"
}

/** 0xe6, INC $12 */
class IncZp(c: Computer): IncBase(c, INC_ZP, 2, 5, OperandZp(c))

/** 0xf6, INC $12,X */
class IncZpX(c: Computer): IncBase(c, INC_ZP_X, 2, 6, OperandZpX(c))

/** 0xce, INC $1234 */
class IncAbsolute(c: Computer): IncBase(c, INC_ABS, 3, 6, OperandAbsolute(c))

/** 0xfe, INC $1234,X */
class IncAbsoluteX(c: Computer): IncBase(c, INC_ABS_X, 3, 7, OperandAbsoluteX(c))


