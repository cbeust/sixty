package com.beust.sixty.op

import com.beust.sixty.*

abstract class CompareBase(override val name: String, override val opCode: Int, override val size: Int,
        override val timing: Int, override val addressing: Addressing)
    : InstructionBase(name, opCode, size, timing, addressing)
{
    abstract fun register(c: Computer): Int

    override fun run(c: Computer, op: Operand) = with(c) {
        val register = register(c)
        val tmp: Int = (register - op.get()) and 0xff
        cpu.P.C = register >= op.get()
        cpu.P.Z = tmp == 0
        cpu.P.N = (tmp and 0x80) != 0
    }
}

abstract class CmpBase(override val opCode: Int, override val size: Int,
        override val timing: Int, addressing: Addressing)
    : CompareBase("CMP", opCode, size, timing, addressing)
{
    override fun register(c: Computer) = c.cpu.A
}

/** CMP #$12 */
class CmpImmediate: CmpBase(CMP_IMM, 2, 2, Addressing.IMMEDIATE)

/** CMP $12 */
class CmpZp: CmpBase(CMP_ZP, 2, 3, Addressing.ZP)

/** CMP $12,X */
class CmpZpX: CmpBase(CMP_ZP_X, 2, 4, Addressing.ZP_X)

/** CMP $1234 */
class CmpAbsolute: CmpBase(CMP_ABS, 3, 4, Addressing.ABSOLUTE)

/** CMP $1234,X */
class CmpAbsoluteX: CmpBase(CMP_ABS_X, 3, 4, Addressing.ABSOLUTE_X) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, op.word + cpu.X)
    }
}

/** CMP $1234,Y */
class CmpAbsoluteY: CmpBase(CMP_ABS_Y, 3, 4, Addressing.ABSOLUTE_Y) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, op.word + cpu.Y)
    }
}

/** CMP ($12,X) */
class CmpIndX: CmpBase(CMP_IND_X, 2, 6, Addressing.INDIRECT_X)

/** CMP ($12),Y */
class CmpIndY: CmpBase(CMP_IND_Y, 2, 5, Addressing.INDIRECT_Y) {
    override var timing = 5
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, memory[op.word] + cpu.Y)
    }
}

abstract class CpxBase(override val opCode: Int, override val size: Int, override val timing: Int,
        addressing: Addressing)
    : CompareBase("CPX", opCode, size, timing, addressing)
{
    override fun register(c: Computer) = c.cpu.X
}

/** CPX #$12 */
class CpxImm: CpxBase(CPX_IMM, 2, 2, Addressing.IMMEDIATE)

/** CPX $12 */
class CpxZp: CpxBase(CPX_ZP, 2, 3, Addressing.ZP)

/** CPX $1234 */
class CpxAbsolute: CpxBase(CPX_ABS, 3, 4, Addressing.ABSOLUTE)

abstract class CpyBase(override val opCode: Int, override val size: Int, override val timing: Int,
        addressing: Addressing)
    : CompareBase("CPY", opCode, size, timing, addressing)
{
    override fun register(c: Computer) = c.cpu.Y
}

/** CPY #$12 */
class CpyImm: CpyBase(CPY_IMM, 2, 2, Addressing.IMMEDIATE)

/** CPY $12 */
class CpyZp: CpyBase(CPY_ZP, 2, 3, Addressing.ZP)

/** CPY $1234 */
class CpyAbsolute: CpyBase(CPY_ABS, 3, 4, Addressing.ABSOLUTE)

