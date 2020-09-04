package com.beust.sixty.op

import com.beust.sixty.*

abstract class AslBase(override val opCode: Int, override val size: Int, override val timing: Int,
        override val addressing: Addressing)
    : InstructionBase()
{
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.P.C = if (op.get().and(0x80) != 0) true else false
            val newValue = op.get().shl(1).and(0xff)
            cpu.P.setNZFlags(newValue)
            op.set(newValue)
        }
    }

    override fun toString(): String = "ASL"
}

/** 0x0a, ASL */
class Asl: AslBase(ASL, 1, 2, Addressing.REGISTER_A)

/** 0x06, ASL $12 */
class AslZp: AslBase(ASL_ZP, 2, 5, Addressing.ZP)

/** 0x16, ASL $12,X */
class AslZpX: AslBase(ASL_ZP_X, 2, 6, Addressing.ZP_X)

/** 0x0e, ASL $1234 */
class AslAbsolute: AslBase(ASL_ABS, 3, 6, Addressing.ABSOLUTE)

/** 0x1e, ASL $1234 */
class AslAbsoluteX: AslBase(ASL_ABS_X, 3, 7, Addressing.ABSOLUTE_X)

