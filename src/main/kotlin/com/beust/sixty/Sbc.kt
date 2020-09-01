package com.beust.sixty

abstract class SbcBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : AdcSbcBase(c, opCode, size, timing)
{
    abstract var value: Int
    abstract val name: String

    override fun run() {
        if (cpu.P.D) {
            TODO("Decimal mode not supported")
        } else {
            // Call ADC with the one complement of the operand
            add(value.inv())
        }
    }
    override fun toString(): String = "SBC${name}"
}

/** SBC #$12 */
class SbcImmediate(c: Computer): SbcBase(c, SBC_IMM, 2, 2) {
    override var value by ValImmediate()
    override val name = nameImmediate()
}

/** SBC $12 */
class SbcZp(c: Computer): SbcBase(c, SBC_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** SBC $12,X */
class SbcZpX(c: Computer): SbcBase(c, SBC_ZP_X, 2, 4) {
    override var value by ValZpX()
    override val name = nameZpX()
}

/** SBC $1234 */
class SbcAbsolute(c: Computer): SbcBase(c, SBC_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** SBC $1234,X */
class SbcAbsoluteX(c: Computer): SbcBase(c, SBC_ABS_X, 3, 4) {
    override var value by ValAbsoluteX()
    override val name = nameAbsX()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** SBC $1234,Y */
class SbcAbsoluteY(c: Computer): SbcBase(c, SBC_ABS_Y, 3, 4) {
    override var value by ValAbsoluteY()
    override val name = nameAbsY()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** 0x21, SBC ($12,X) */
class SbcIndX(c: Computer): SbcBase(c, SBC_IND_X, 2, 6) {
    override var value by ValIndirectX()
    override val name = nameIndirectX()
}

/** 0x31, SBC ($12),Y */
class SbcIndY(c: Computer): SbcBase(c, SBC_IND_Y, 2, 5) {
    override var value by ValIndirectY()
    override val name = nameIndirectY()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

