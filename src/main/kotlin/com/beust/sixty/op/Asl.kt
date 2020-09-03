package com.beust.sixty.op

import com.beust.sixty.*

abstract class AslBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        val op: Operand)
    : InstructionBase(c)
{
    override fun run() {
        cpu.P.C = if (op.get().and(0x80) != 0) true else false
        val newValue = op.get().shl(1).and(0xff)
        cpu.P.setNZFlags(newValue)
        op.set(newValue)
    }
    override fun toString(): String = "ASL${op.name}"
}

/** 0x0a, ASL */
class Asl(c: Computer): AslBase(c, ASL, 1, 2, OperandRegisterA(c))

/** 0x06, ASL $12 */
class AslZp(c: Computer): AslBase(c, ASL_ZP, 2, 5, OperandZp(c))

/** 0x16, ASL $12,X */
class AslZpX(c: Computer): AslBase(c, ASL_ZP_X, 2, 6, OperandZpX(c))

/** 0x0e, ASL $1234 */
class AslAbsolute(c: Computer): AslBase(c, ASL_ABS, 3, 6, OperandAbsolute(c))

/** 0x1e, ASL $1234 */
class AslAbsoluteX(c: Computer): AslBase(c, ASL_ABS_X, 3, 7, OperandAbsoluteX(c))

