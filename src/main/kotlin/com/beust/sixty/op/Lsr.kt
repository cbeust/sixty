package com.beust.sixty.op

import com.beust.sixty.*

abstract class LsrBase(override val opCode: Int, override val size: Int, override val timing: Int,
        override val addressing: Addressing)
    : InstructionBase("LSR", opCode, size, timing, addressing)
{
    override fun run(c: Computer, op: Operand) = with(c) {
        val value = op.get()
        cpu.P.C = value.and(1) != 0
        val result = value.shr(1)
        cpu.P.setNZFlags(result)
        op.set(result)
    }
}

/** 0x4a, LSR */
class Lsr: LsrBase(LSR, 1, 2, Addressing.REGISTER_A)

/** 0x46, LSR $12 */
class LsrZp: LsrBase(LSR_ZP, 2, 6, Addressing.ZP)

/** 0x56, LSR $12,X */
class LsrZpX: LsrBase(LSR_ZP_X, 2, 6, Addressing.ZP_X)

/** 0x4e, LSR $1234 */
class LsrAbsolute: LsrBase(LSR_ABS, 3, 6, Addressing.ABSOLUTE)

/** 0x5e, LSR $1234,X */
class LsrAbsoluteX: LsrBase(LSR_ABS_X, 3, 7, Addressing.ABSOLUTE_X)

