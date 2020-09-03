package com.beust.sixty.op

import com.beust.sixty.*

abstract class LsrBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        val op: Operand)
    : InstructionBase(c)
{
    override fun run() {
        val value = op.get()
        cpu.P.C = value.and(1) != 0
        val result = value.shr(1)
        cpu.P.setNZFlags(result)
        op.set(result)
    }
    override fun toString(): String = "LSR${op.name}"
}

/** 0x4a, LSR */
class Lsr(c: Computer): LsrBase(c, LSR, 1, 2, OperandRegisterA(c))

/** 0x46, LSR $12 */
class LsrZp(c: Computer): LsrBase(c, LSR_ZP, 2, 6, OperandZp(c))

/** 0x56, LSR $12,X */
class LsrZpX(c: Computer): LsrBase(c, LSR_ZP_X, 2, 6, OperandZpX(c))

/** 0x4e, LSR $1234 */
class LsrAbsolute(c: Computer): LsrBase(c, LSR_ABS, 3, 6, OperandAbsolute(c))

/** 0x5e, LSR $1234,X */
class LsrAbsoluteX(c: Computer): LsrBase(c, LSR_ABS_X, 3, 7, OperandAbsoluteX(c))

