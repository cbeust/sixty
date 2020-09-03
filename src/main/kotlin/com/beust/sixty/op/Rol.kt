package com.beust.sixty.op

import com.beust.sixty.*

abstract class RolBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        val op: Operand)
    : InstructionBase(c)
{

    override fun run() {
        val value = op.get()
        val result = (value.shl(1).or(cpu.P.C.int())) and 0xff
        cpu.P.C = value.and(0x80) != 0
        op.set(result)
        cpu.P.setNZFlags(result)
    }
    override fun toString(): String = "ROL${op.name}"
}

/** 0x2A, ROL */
class Rol(c: Computer): RolBase(c, ROL, 1, 2, OperandRegisterA(c))

/** 0x26, ROL $12 */
class RolZp(c: Computer): RolBase(c, ROL_ZP, 2, 5, OperandZp(c))

/** 0x36, ROL $12,X */
class RolZpX(c: Computer): RolBase(c, ROL_ZP_X, 2, 6, OperandZpX(c))

/** 0x2e, ROL $1234 */
class RolAbsolute(c: Computer): RolBase(c, ROL_ABS, 3, 6, OperandAbsolute(c))

/** 0x3e, ROL $1234,X */
class RolAbsoluteX(c: Computer): RolBase(c, ROL_ABS_X, 3, 7, OperandAbsoluteX(c))

