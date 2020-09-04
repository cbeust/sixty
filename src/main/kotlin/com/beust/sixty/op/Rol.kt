package com.beust.sixty.op

import com.beust.sixty.*

abstract class RolBase(override val opCode: Int, override val size: Int, override val timing: Int,
        override val addressing: Addressing)
    : InstructionBase("ROL", opCode, size, timing, addressing)
{

    override fun run(c: Computer, op: Operand) = with(c) {
        val value = op.get()
        val result = (value.shl(1).or(cpu.P.C.int())) and 0xff
        cpu.P.C = value.and(0x80) != 0
        op.set(result)
        cpu.P.setNZFlags(result)
    }
}

/** 0x2A, ROL */
class Rol: RolBase(ROL, 1, 2, Addressing.REGISTER_A)

/** 0x26, ROL $12 */
class RolZp: RolBase(ROL_ZP, 2, 5, Addressing.ZP)

/** 0x36, ROL $12,X */
class RolZpX: RolBase(ROL_ZP_X, 2, 6, Addressing.ZP_X)

/** 0x2e, ROL $1234 */
class RolAbsolute: RolBase(ROL_ABS, 3, 6, Addressing.ABSOLUTE)

/** 0x3e, ROL $1234,X */
class RolAbsoluteX: RolBase(ROL_ABS_X, 3, 7, Addressing.ABSOLUTE_X)

