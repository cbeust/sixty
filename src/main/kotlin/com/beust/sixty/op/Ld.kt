package com.beust.sixty.op

import com.beust.sixty.*

abstract class LdBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        val op: Operand)
    : InstructionBase(c)
{
    abstract var register: Int
    abstract val registerName: String

    override fun run() {
        register = op.get()
        cpu.P.setNZFlags(register)
    }

    override fun toString(): String = "LD${registerName}${op.name}"
}

abstract class LdaBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        op: Operand)
    : LdBase(c, opCode, size, timing, op)
{
    override val registerName = "A"
    override var register: Int
        get() = cpu.A
        set(f) { cpu.A = f }
}

/** LDA #$12 */
class LdaImmediate(c: Computer): LdaBase(c, LDA_IMM, 2, 2, OperandImmediate(c))

/** LDA $12 */
class LdaZp(c: Computer): LdaBase(c, LDA_ZP, 2, 3, OperandZp(c))

/** LDA $12,X */
class LdaZpX(c: Computer): LdaBase(c, LDA_ZP_X, 2, 4, OperandZpX(c))

/** LDA $1234 */
class LdaAbsolute(c: Computer): LdaBase(c, LDA_ABS, 3, 4, OperandAbsolute(c))

/** LDA $1234,X */
class LdaAbsoluteX(c: Computer): LdaBase(c, LDA_ABS_X, 3, 4, OperandAbsoluteX(c)) {
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** LDA $1234,Y */
class LdaAbsoluteY(c: Computer): LdaBase(c, LDA_ABS_Y, 3, 4, OperandAbsoluteY(c)) {
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** LDA ($12,X) */
class LdaIndX(c: Computer): LdaBase(c, LDA_IND_X, 2, 6, OperandIndirectX(c))

/** LDA ($12),Y */
class LdaIndY(c: Computer): LdaBase(c, LDA_IND_Y, 2, 5, OperandIndirectY(c)) {
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

abstract class LdxBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        op: Operand)
    : LdBase(c, opCode, size, timing, op)
{
    override val registerName = "X"
    override var register: Int
        get() = cpu.X
        set(f) { cpu.X = f }
}

/** LDX #$12 */
class LdxImm(c: Computer): LdxBase(c, LDX_IMM, 2, 2, OperandImmediate(c))

/** LDX $12 */
class LdxZp(c: Computer): LdxBase(c, LDX_ZP, 2, 3, OperandZp(c))

/** LDX $12,Y */
class LdxZpY(c: Computer): LdxBase(c, LDX_ZP_Y, 2, 4, OperandZpY(c))

/** LDX $1234 */
class LdxAbsolute(c: Computer): LdxBase(c, LDX_ABS, 3, 4, OperandAbsolute(c))

/** LDX $1234,Y */
class LdxAbsoluteY(c: Computer): LdxBase(c, LDX_ABS_Y, 3, 4, OperandAbsoluteY(c))

abstract class LdyBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        op: Operand)
    : LdBase(c, opCode, size, timing, op)
{
    override val registerName = "Y"
    override var register: Int
        get() = cpu.Y
        set(f) { cpu.Y = f }
}

/** LDY #$12 */
class LdyImm(c: Computer): LdyBase(c, LDY_IMM, 2, 2, OperandImmediate(c))

/** LDY $12 */
class LdyZp(c: Computer): LdyBase(c, LDY_ZP, 2, 3, OperandZp(c))

/** LDY $12,X */
class LdyZpX(c: Computer): LdyBase(c, LDY_ZP_X, 2, 4, OperandZpX(c))

/** LDY $1234 */
class LdyAbsolute(c: Computer): LdyBase(c, LDY_ABS, 3, 4, OperandAbsolute(c))

/** LDY $1234,X */
class LdyAbsoluteX(c: Computer): LdyBase(c, LDY_ABS_X, 3, 4, OperandAbsoluteX(c))
