package com.beust.sixty.op

import com.beust.sixty.*

abstract class LdBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : InstructionBase(c)
{
    abstract var register: Int
    abstract val registerName: String
    abstract var value: Int
    abstract val name: String

    override fun run() {
        register = value
        cpu.P.setNZFlags(value)
    }

    override fun toString(): String = "LD${registerName}${name}"
}

abstract class LdaBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : LdBase(c, opCode, size, timing)
{
    override val registerName = "A"
    override var register: Int
        get() = cpu.A
        set(f) { cpu.A = f }
}

/** LDA #$12 */
class LdaImmediate(c: Computer): LdaBase(c, LDA_IMM, 2, 2) {
    override var value by ValImmediate()
    override val name = nameImmediate()
}

/** LDA $12 */
class LdaZp(c: Computer): LdaBase(c, LDA_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** LDA $12,X */
class LdaZpX(c: Computer): LdaBase(c, LDA_ZP_X, 2, 4) {
    override var value by ValZpX()
    override val name = nameZpX()
}

/** LDA $1234 */
class LdaAbsolute(c: Computer): LdaBase(c, LDA_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** LDA $1234,X */
class LdaAbsoluteX(c: Computer): LdaBase(c, LDA_ABS_X, 3, 4) {
    override var value by ValAbsoluteX()
    override val name = nameAbsX()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** LDA $1234,Y */
class LdaAbsoluteY(c: Computer): LdaBase(c, LDA_ABS_Y, 3, 4) {
    override var value by ValAbsoluteY()
    override val name = nameAbsY()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** LDA ($12,X) */
class LdaIndX(c: Computer): LdaBase(c, LDA_IND_X, 2, 6) {
    override var value by ValIndirectX()
    override val name = nameIndirectX()
}

/** LDA ($12),Y */
class LdaIndY(c: Computer): LdaBase(c, LDA_IND_Y, 2, 5) {
    override var value by ValIndirectY()
    override val name = nameIndirectY()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

abstract class LdxBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : LdBase(c, opCode, size, timing)
{
    override val registerName = "X"
    override var register: Int
        get() = cpu.X
        set(f) { cpu.X = f }
}

/** LDX #$12 */
class LdxImm(c: Computer): LdxBase(c, LDX_IMM, 2, 2) {
    override var value by ValImmediate()
    override val name = nameImmediate()
}

/** LDX $12 */
class LdxZp(c: Computer): LdxBase(c, LDX_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** LDX $12,Y */
class LdxZpY(c: Computer): LdxBase(c, LDX_ZP_Y, 2, 4) {
    override var value by ValZpY()
    override val name = nameZpY()
}

/** LDX $1234 */
class LdxAbsolute(c: Computer): LdxBase(c, LDX_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** LDX $1234,Y */
class LdxAbsoluteY(c: Computer): LdxBase(c, LDX_ABS_Y, 3, 4) {
    override var value by ValAbsoluteY()
    override val name = nameAbsY()
}

abstract class LdyBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : LdBase(c, opCode, size, timing)
{
    override val registerName = "Y"
    override var register: Int
        get() = cpu.Y
        set(f) { cpu.Y = f }
}

/** LDY #$12 */
class LdyImm(c: Computer): LdyBase(c, LDY_IMM, 2, 2) {
    override var value by ValImmediate()
    override val name = nameImmediate()
}

/** LDY $12 */
class LdyZp(c: Computer): LdyBase(c, LDY_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** LDY $12,X */
class LdyZpX(c: Computer): LdyBase(c, LDY_ZP_X, 2, 4) {
    override var value by ValZpX()
    override val name = nameZpX()
}

/** LDY $1234 */
class LdyAbsolute(c: Computer): LdyBase(c, LDY_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** LDY $1234,X */
class LdyAbsoluteX(c: Computer): LdyBase(c, LDY_ABS_X, 3, 4) {
    override var value by ValAbsoluteX()
    override val name = nameAbsX()
}
