package com.beust.sixty

abstract class AdcBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : AdcSbcBase(c, opCode, size, timing)
{
    abstract var value: Int
    abstract val name: String

    override fun run() {
        if (cpu.P.D) {
            TODO("Decimal mode not supported")
        } else {
            add(value)
        }
    }
    override fun toString(): String = "ADC${name}"
}

/** ADC #$12 */
class AdcImmediate(c: Computer): AdcBase(c, ADC_IMM, 2, 2) {
    override var value by ValImmediate()
    override val name = nameImmediate()
}

/** ADC $12 */
class AdcZp(c: Computer): AdcBase(c, ADC_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** ADC $12,X */
class AdcZpX(c: Computer): AdcBase(c, ADC_ZP_X, 2, 4) {
    override var value by ValZpX()
    override val name = nameZpX()
}

/** ADC $1234 */
class AdcAbsolute(c: Computer): AdcBase(c, ADC_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** ADC $1234,X */
class AdcAbsoluteX(c: Computer): AdcBase(c, ADC_ABS_X, 3, 4) {
    override var value by ValAbsoluteX()
    override val name = nameAbsX()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** ADC $1234,Y */
class AdcAbsoluteY(c: Computer): AdcBase(c, ADC_ABS_Y, 3, 4) {
    override var value by ValAbsoluteY()
    override val name = nameAbsY()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** 0x21, ADC ($12,X) */
class AdcIndX(c: Computer): AdcBase(c, ADC_IND_X, 2, 6) {
    override var value by ValIndirectX()
    override val name = nameIndirectX()
}

/** 0x31, ADC ($12),Y */
class AdcIndY(c: Computer): AdcBase(c, ADC_IND_Y, 2, 5) {
    override var value by ValIndirectY()
    override val name = nameIndirectY()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

