package com.beust.sixty.op

import com.beust.sixty.*

abstract class AndBase(override val opCode: Int, override val size: Int, override val timing: Int,
        override val addressing: Addressing)
    : InstructionBase("AND", opCode, size, timing, addressing)
{
    override fun run(c: Computer, op: Operand) = with(c) {
        cpu.A = cpu.A.and(op.get())
        cpu.P.setNZFlags(cpu.A)
    }
}

/** AND #$12 */
class AndImmediate: AndBase(AND_IMM, 2, 2, Addressing.IMMEDIATE)

/** AND $12 */
class AndZp: AndBase(AND_ZP, 2, 3, Addressing.ZP)

/** AND $12,X */
class AndZpX: AndBase(AND_ZP_X, 2, 4, Addressing.ZP_X)

/** AND $1234 */
class AndAbsolute: AndBase(AND_ABS, 3, 4, Addressing.ABSOLUTE)

/** AND $1234,X */
class AndAbsoluteX: AndBase(AND_ABS_X, 3, 4, Addressing.ABSOLUTE_X) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, op.word + cpu.X)
    }
}

/** AND $1234,Y */
class AndAbsoluteY: AndBase(AND_ABS_Y, 3, 4, Addressing.ABSOLUTE_Y) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, op.word + cpu.Y)
    }
}

/** AND ($12,X) */
class AndIndX: AndBase(AND_IND_X, 2, 6, Addressing.INDIRECT_X)

/** AND ($12),Y */
class AndIndY: AndBase(AND_IND_Y, 2, 5, Addressing.INDIRECT_Y) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, memory[op.word] + cpu.Y)
    }
}

