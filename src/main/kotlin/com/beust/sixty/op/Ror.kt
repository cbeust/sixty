package com.beust.sixty.op

import com.beust.sixty.*

abstract class RorBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        val op: Operand)
    : InstructionBase(c)
{
    override fun run() {
        val value = op.get()
        val bit0 = value.and(0x1)
        val result = value.shr(1).or(cpu.P.C.int().shl(7))
        cpu.P.setNZFlags(result)
        cpu.P.C = bit0.toBoolean()
        op.set(result)
    }
    override fun toString(): String = "ROR${op.name}"
}

/** 0x6a, ROR */
class Ror(c: Computer): RorBase(c, ROR, 1, 2, OperandRegisterA(c))

/** 0x66, ROR $12 */
class RorZp(c: Computer): RorBase(c, ROR_ZP, 2, 5, OperandZp(c))

/** 0x76, ROR $12,X */
class RorZpX(c: Computer): RorBase(c, ROR_ZP_X, 2, 6, OperandZpX(c))

/** 0x6e, ROR $1234 */
class RorAbsolute(c: Computer): RorBase(c, ROR_ABS, 3, 6, OperandAbsolute(c))

/** 0x7e, ROR $1234,X */
class RorAbsoluteX(c: Computer): RorBase(c, ROR_ABS_X, 3, 7, OperandAbsoluteX(c))


