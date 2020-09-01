package com.beust.sixty

abstract class AndBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : InstructionBase(c)
{
    abstract var value: Int
    abstract val name: String
    override fun run() {
        cpu.A = cpu.A.and(value)
        cpu.P.setNZFlags(value)
    }
    override fun toString(): String = "AND${name}"
}

/** 0x29, AND #$12 */
class AndImmediate(c: Computer): AndBase(c, AND_ZP, 2, 2) {
    override var value by ValImmediate()
    override val name = nameImmediate()
}

/** 0x25, AND $12 */
class AndZp(c: Computer): AndBase(c, AND_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** 0x35, AND $12,X */
class AndZpX(c: Computer): AndBase(c, AND_ZP_X, 2, 4) {
    override var value by ValZpX()
    override val name = nameZpX()
}

/** 0x2d, AND $1234 */
class AndAbsolute(c: Computer): AndBase(c, AND_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** 0x3d, AND $1234,X */
class AndAbsoluteX(c: Computer): AndBase(c, AND_ABS_X, 3, 4) {
    override var value by ValAbsoluteX()
    override val name = nameAbsX()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** 0x39, AND $1234,Y */
class AndAbsoluteY(c: Computer): AndBase(c, AND_ABS_Y, 3, 4) {
    override var value by ValAbsoluteY()
    override val name = nameAbsY()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** 0x21, AND ($12,X) */
class AndIndX(c: Computer): AndBase(c, AND_IND_X, 2, 6) {
    override var value by ValIndirectX()
    override val name = nameIndirectX()
}

/** 0x31, AND ($12),Y */
class AndIndY(c: Computer): AndBase(c, AND_IND_Y, 2, 5) {
    override var value by ValIndirectY()
    override val name = nameIndirectY()
    override var timing = 4
    override fun run() {
        super.run()
        println("Adjust timing")
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

