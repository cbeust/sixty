package com.beust.sixty.op

import com.beust.sixty.*

abstract class CompareBase(c: Computer, override val opCode: Int, override val size: Int,
        override val timing: Int, val op: Operand)
    : InstructionBase(c)
{
    abstract val opName: String
    abstract val register: Int

    override fun run() {
        val tmp: Int = (register - op.get()) and 0xff
        cpu.P.C = register >= op.get()
        cpu.P.Z = tmp == 0
        cpu.P.N = (tmp and 0x80) != 0
    }

    override fun toString(): String = "$opName${op.name}"
}

abstract class CmpBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        op: Operand)
    : CompareBase(c, opCode, size, timing, op)
{
    override val register = cpu.A
    override val opName = "CMP"
}

/** CMP #$12 */
class CmpImmediate(c: Computer): CmpBase(c, CMP_IMM, 2, 2, OperandImmediate(c))

/** CMP $12 */
class CmpZp(c: Computer): CmpBase(c, CMP_ZP, 2, 3, OperandZp(c))

/** CMP $12,X */
class CmpZpX(c: Computer): CmpBase(c, CMP_ZP_X, 2, 4, OperandZpX(c))

/** CMP $1234 */
class CmpAbsolute(c: Computer): CmpBase(c, CMP_ABS, 3, 4, OperandAbsolute(c))

/** CMP $1234,X */
class CmpAbsoluteX(c: Computer): CmpBase(c, CMP_ABS_X, 3, 4, OperandAbsoluteX(c)) {
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** CMP $1234,Y */
class CmpAbsoluteY(c: Computer): CmpBase(c, CMP_ABS_Y, 3, 4, OperandAbsoluteY(c)) {
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** CMP ($12,X) */
class CmpIndX(c: Computer): CmpBase(c, CMP_IND_X, 2, 6, OperandIndirectX(c))

/** CMP ($12),Y */
class CmpIndY(c: Computer): CmpBase(c, CMP_IND_Y, 2, 5, OperandIndirectY(c)) {
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

abstract class CpxBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        op: Operand)
    : CompareBase(c, opCode, size, timing, op)
{
    override val register = cpu.X
    override val opName = "CPX"
}

/** CPX #$12 */
class CpxImm(c: Computer): CpxBase(c, CPX_IMM, 2, 2, OperandImmediate(c))

/** CPX $12 */
class CpxZp(c: Computer): CpxBase(c, CPX_ZP, 2, 3, OperandZp(c))

/** CPX $1234 */
class CpxAbsolute(c: Computer): CpxBase(c, CPX_ABS, 3, 4, OperandAbsolute(c))

abstract class CpyBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        op: Operand)
    : CompareBase(c, opCode, size, timing, op)
{
    override val register = cpu.Y
    override val opName = "CPY"
}

/** CPY #$12 */
class CpyImm(c: Computer): CpyBase(c, CPY_IMM, 2, 2, OperandImmediate(c))

/** CPY $12 */
class CpyZp(c: Computer): CpyBase(c, CPY_ZP, 2, 3, OperandZp(c))

/** CPY $1234 */
class CpyAbsolute(c: Computer): CpyBase(c, CPY_ABS, 3, 4, OperandAbsolute(c))

