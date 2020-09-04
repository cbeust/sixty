package com.beust.sixty.op

import com.beust.sixty.*

abstract class EorBase(override val opCode: Int, override val size: Int, override val timing: Int,
        override val addressing: Addressing)
    : InstructionBase("EOR", opCode, size, timing, addressing)
{
    override fun run(c: Computer, op: Operand) = with(c) {
        cpu.A = cpu.A.xor(op.get())
        cpu.P.setNZFlags(cpu.A)
    }
}

/** 0x49, EOR #$12 */
class EorImmediate: EorBase(EOR_IMM, 2, 2, Addressing.IMMEDIATE)

/** 0x45, EOR $12 */
class EorZp: EorBase(EOR_ZP, 2, 3, Addressing.ZP)

/** 0x55, EOR $12,X */
class EorZpX: EorBase(EOR_ZP_X, 2, 4, Addressing.ZP_X)

/** 0x4d, EOR $1234 */
class EorAbsolute: EorBase(EOR_ABS, 3, 4, Addressing.ABSOLUTE)

/** 0x5d, EOR $1234,X */
class EorAbsoluteX: EorBase(EOR_ABS_X, 3, 4, Addressing.ABSOLUTE_X) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, op.word + cpu.X)
    }
}

/** 0x59, EOR $1234,Y */
class EorAbsoluteY: EorBase(EOR_ABS_Y, 3, 4, Addressing.ABSOLUTE_Y) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, op.word + cpu.Y)
    }
}

/** 0x41, EOR ($12,X) */
class EorIndX: EorBase(EOR_IND_X, 2, 6, Addressing.INDIRECT_X)

/** 0x51, EOR ($12),Y */
class EorIndY: EorBase(EOR_IND_Y, 2, 5, Addressing.INDIRECT_Y) {
    override var timing = 5
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, memory[op.word] + cpu.Y)
    }
}

