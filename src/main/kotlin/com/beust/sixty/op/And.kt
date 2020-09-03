package com.beust.sixty.op

import com.beust.sixty.*

abstract class AndBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        val op: Operand)
    : InstructionBase(c)
{
    override fun run() {
        cpu.A = cpu.A.and(op.get())
        cpu.P.setNZFlags(cpu.A)
    }
    override fun toString(): String = "AND${op.name}"
}

/** AND #$12 */
class AndImmediate(c: Computer): AndBase(c, AND_IMM, 2, 2, OperandImmediate(c))

/** AND $12 */
class AndZp(c: Computer): AndBase(c, AND_ZP, 2, 3, OperandZp(c))

/** AND $12,X */
class AndZpX(c: Computer): AndBase(c, AND_ZP_X, 2, 4, OperandZpX(c))

/** AND $1234 */
class AndAbsolute(c: Computer): AndBase(c, AND_ABS, 3, 4, OperandAbsolute(c))

/** AND $1234,X */
class AndAbsoluteX(c: Computer): AndBase(c, AND_ABS_X, 3, 4, OperandAbsoluteX(c)) {
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** AND $1234,Y */
class AndAbsoluteY(c: Computer): AndBase(c, AND_ABS_Y, 3, 4, OperandAbsoluteY(c)) {
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** AND ($12,X) */
class AndIndX(c: Computer): AndBase(c, AND_IND_X, 2, 6, OperandIndirectX(c))

/** AND ($12),Y */
class AndIndY(c: Computer): AndBase(c, AND_IND_Y, 2, 5, OperandIndirectY(c)) {
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

