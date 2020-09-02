package com.beust.sixty.op

import com.beust.sixty.*

abstract class CompareBase(c: Computer, override val opCode: Int, override val size: Int,
        override val timing: Int)
    : InstructionBase(c)
{
    abstract val value: Int
    abstract val name: String
    abstract val register: Int

    override fun run() {
        val tmp: Int = (register - value) and 0xff
        cpu.P.C = register >= value
        cpu.P.Z = tmp == 0
        cpu.P.N = (tmp and 0x80) != 0
    }

    override fun toString(): String = "CMP${name}"
}

abstract class CmpBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : CompareBase(c, opCode, size, timing)
{
    override val register = cpu.A
}

/** CMP #$12 */
class CmpImmediate(c: Computer): CmpBase(c, CMP_IMM, 2, 2) {
    override var value by ValImmediate()
    override val name = nameImmediate()
}

/** CMP $12 */
class CmpZp(c: Computer): CmpBase(c, CMP_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** CMP $12,X */
class CmpZpX(c: Computer): CmpBase(c, CMP_ZP_X, 2, 4) {
    override var value by ValZpX()
    override val name = nameZpX()
}

/** CMP $1234 */
class CmpAbsolute(c: Computer): CmpBase(c, CMP_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** CMP $1234,X */
class CmpAbsoluteX(c: Computer): CmpBase(c, CMP_ABS_X, 3, 4) {
    override var value by ValAbsoluteX()
    override val name = nameAbsX()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** CMP $1234,Y */
class CmpAbsoluteY(c: Computer): CmpBase(c, CMP_ABS_Y, 3, 4) {
    override var value by ValAbsoluteY()
    override val name = nameAbsY()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** CMP ($12,X) */
class CmpIndX(c: Computer): CmpBase(c, CMP_IND_X, 2, 6) {
    override var value by ValIndirectX()
    override val name = nameIndirectX()
}

/** CMP ($12),Y */
class CmpIndY(c: Computer): CmpBase(c, CMP_IND_Y, 2, 5) {
    override var value by ValIndirectY()
    override val name = nameIndirectY()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

abstract class CpxBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : CompareBase(c, opCode, size, timing)
{
    override val register = cpu.X
}

/** CPX #$12 */
class CpxImm(c: Computer): CpxBase(c, CPX_IMM, 2, 2) {
    override var value by ValImmediate()
    override val name = nameImmediate()
}

/** CPX $12 */
class CpxZp(c: Computer): CpxBase(c, CPX_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** CPX $1234 */
class CpxAbsolute(c: Computer): CpxBase(c, CPX_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

abstract class CpyBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : CompareBase(c, opCode, size, timing)
{
    override val register = cpu.Y
}

/** CPY #$12 */
class CpyImm(c: Computer): CpyBase(c, CPY_IMM, 2, 2) {
    override var value by ValImmediate()
    override val name = nameImmediate()
}

/** CPY $12 */
class CpyZp(c: Computer): CpyBase(c, CPY_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** CPY $1234 */
class CpyAbsolute(c: Computer): CpyBase(c, CPY_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

