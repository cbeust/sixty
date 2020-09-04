package com.beust.sixty.op

import com.beust.sixty.*

abstract class IncBase(override val opCode: Int, override val size: Int, override val timing: Int,
        override val addressing: Addressing)
    : InstructionBase("INC", opCode, size, timing, addressing)
{
    override fun run(c: Computer, op: Operand) = with(c) {
        op.set(op.get() + 1)
        cpu.P.setNZFlags(op.get())
    }
}

/** 0xe6, INC $12 */
class IncZp: IncBase(INC_ZP, 2, 5, Addressing.ZP)

/** 0xf6, INC $12,X */
class IncZpX: IncBase(INC_ZP_X, 2, 6, Addressing.ZP_X)

/** 0xce, INC $1234 */
class IncAbsolute: IncBase(INC_ABS, 3, 6, Addressing.ABSOLUTE)

/** 0xfe, INC $1234,X */
class IncAbsoluteX: IncBase(INC_ABS_X, 3, 7, Addressing.ABSOLUTE_X)


