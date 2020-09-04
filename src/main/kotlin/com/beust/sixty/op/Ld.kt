package com.beust.sixty.op

import com.beust.sixty.*

abstract class LdBase(override val name: String, override val opCode: Int, override val size: Int,
        override val timing: Int, override val addressing: Addressing)
    : InstructionBase(name, opCode, size, timing, addressing)
{
    abstract fun getRegister(c: Computer): Int
    abstract fun setRegister(c: Computer, value: Int)
    abstract val registerName: String

    override fun run(c: Computer, op: Operand) = with(c) {
        setRegister(c, op.get())
        cpu.P.setNZFlags(getRegister(c))
    }
}

abstract class LdaBase(override val opCode: Int, override val size: Int, override val timing: Int,
        val a: Addressing)
    : LdBase("LDA", opCode, size, timing, a)
{
    override val registerName = "A"
    override fun getRegister(c: Computer) = c.cpu.A
    override fun setRegister(c: Computer, value: Int) { c.cpu.A = value }
}

/** LDA #$12 */
class LdaImmediate: LdaBase(LDA_IMM, 2, 2, Addressing.IMMEDIATE)

/** LDA $12 */
class LdaZp: LdaBase(LDA_ZP, 2, 3, Addressing.ZP)

/** LDA $12,X */
class LdaZpX: LdaBase(LDA_ZP_X, 2, 4, Addressing.ZP_X)

/** LDA $1234 */
class LdaAbsolute: LdaBase(LDA_ABS, 3, 4, Addressing.ABSOLUTE)

/** LDA $1234,X */
class LdaAbsoluteX: LdaBase(LDA_ABS_X, 3, 4, Addressing.ABSOLUTE_X) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** LDA $1234,Y */
class LdaAbsoluteY: LdaBase(LDA_ABS_Y, 3, 4, Addressing.ABSOLUTE_Y) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** LDA ($12,X) */
class LdaIndX: LdaBase(LDA_IND_X, 2, 6, Addressing.INDIRECT_X)

/** LDA ($12),Y */
class LdaIndY: LdaBase(LDA_IND_Y, 2, 5, Addressing.INDIRECT_Y) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

abstract class LdxBase(override val opCode: Int, override val size: Int, override val timing: Int,
        val a: Addressing)
    : LdBase("LDX", opCode, size, timing, a)
{
    override val registerName = "X"
    override fun getRegister(c: Computer) = c.cpu.X
    override fun setRegister(c: Computer, value: Int) { c.cpu.X = value }
}

/** LDX #$12 */
class LdxImm: LdxBase(LDX_IMM, 2, 2, Addressing.IMMEDIATE)

/** LDX $12 */
class LdxZp: LdxBase(LDX_ZP, 2, 3, Addressing.ZP)

/** LDX $12,Y */
class LdxZpY: LdxBase(LDX_ZP_Y, 2, 4, Addressing.ZP_Y)

/** LDX $1234 */
class LdxAbsolute: LdxBase(LDX_ABS, 3, 4, Addressing.ABSOLUTE)

/** LDX $1234,Y */
class LdxAbsoluteY: LdxBase(LDX_ABS_Y, 3, 4, Addressing.ABSOLUTE_Y)

abstract class LdyBase(override val opCode: Int, override val size: Int, override val timing: Int,
        a: Addressing)
    : LdBase("LDY", opCode, size, timing, a)
{
    override val registerName = "Y"
    override fun getRegister(c: Computer) = c.cpu.Y
    override fun setRegister(c: Computer, value: Int) { c.cpu.Y = value }
}

/** LDY #$12 */
class LdyImm: LdyBase(LDY_IMM, 2, 2, Addressing.IMMEDIATE)

/** LDY $12 */
class LdyZp: LdyBase(LDY_ZP, 2, 3, Addressing.ZP)

/** LDY $12,X */
class LdyZpX: LdyBase(LDY_ZP_X, 2, 4, Addressing.ZP_X)

/** LDY $1234 */
class LdyAbsolute: LdyBase(LDY_ABS, 3, 4, Addressing.ABSOLUTE)

/** LDY $1234,X */
class LdyAbsoluteX: LdyBase(LDY_ABS_X, 3, 4, Addressing.ABSOLUTE_X)
