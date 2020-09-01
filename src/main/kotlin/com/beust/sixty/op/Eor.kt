package com.beust.sixty.op

import com.beust.sixty.*

abstract class EorBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : InstructionBase(c)
{
    abstract var value: Int
    abstract val name: String
    override fun run() {
        cpu.A = cpu.A.xor(value)
        cpu.P.setNZFlags(value)
    }
    override fun toString(): String = "EOR${name}"
}

/** 0x49, EOR #$12 */
class EorImmediate(c: Computer): EorBase(c, EOR_IMM, 2, 2) {
    override var value by ValImmediate()
    override val name = nameImmediate()
}

/** 0x45, EOR $12 */
class EorZp(c: Computer): EorBase(c, EOR_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** 0x55, EOR $12,X */
class EorZpX(c: Computer): EorBase(c, EOR_ZP_X, 2, 4) {
    override var value by ValZpX()
    override val name = nameZpX()
}

/** 0x4d, EOR $1234 */
class EorAbsolute(c: Computer): EorBase(c, EOR_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** 0x5d, EOR $1234,X */
class EorAbsoluteX(c: Computer): EorBase(c, EOR_ABS_X, 3, 4) {
    override var value by ValAbsoluteX()
    override val name = nameAbsX()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** 0x59, EOR $1234,Y */
class EorAbsoluteY(c: Computer): EorBase(c, EOR_ABS_Y, 3, 4) {
    override var value by ValAbsoluteY()
    override val name = nameAbsY()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** 0x41, EOR ($12,X) */
class EorIndX(c: Computer): EorBase(c, EOR_IND_X, 2, 6) {
    override var value by ValIndirectX()
    override val name = nameIndirectX()
}

/** 0x51, EOR ($12),Y */
class EorIndY(c: Computer): EorBase(c, EOR_IND_Y, 2, 5) {
    override var value by ValIndirectY()
    override val name = nameIndirectY()
    override var timing = 5
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

