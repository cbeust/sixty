package com.beust.sixty.op

import com.beust.sixty.*

abstract class RorBase(override val opCode: Int, override val size: Int, override val timing: Int,
        override val addressing: Addressing)
    : InstructionBase("ROR", size, timing, addressing)
{
    override fun run(c: Computer, op: Operand) = with(c) {
        val value = op.get()
        val bit0 = value.and(0x1)
        val result = value.shr(1).or(cpu.P.C.int().shl(7))
        cpu.P.setNZFlags(result)
        cpu.P.C = bit0.toBoolean()
        op.set(result)
    }
}

/** 0x6a, ROR */
class Ror: RorBase(ROR, 1, 2, Addressing.REGISTER_A)

/** 0x66, ROR $12 */
class RorZp: RorBase(ROR_ZP, 2, 5, Addressing.ZP)

/** 0x76, ROR $12,X */
class RorZpX: RorBase(ROR_ZP_X, 2, 6, Addressing.ZP_X)

/** 0x6e, ROR $1234 */
class RorAbsolute: RorBase(ROR_ABS, 3, 6, Addressing.ABSOLUTE)

/** 0x7e, ROR $1234,X */
class RorAbsoluteX: RorBase(ROR_ABS_X, 3, 7, Addressing.ABSOLUTE_X)


