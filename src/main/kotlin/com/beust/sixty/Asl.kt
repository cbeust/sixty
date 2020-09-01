package com.beust.sixty

abstract class AslBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : InstructionBase(c)
{
    abstract var value: Int
    abstract val name: String

    override fun run() {
        cpu.P.C = if (value.and(0x80) != 0) true else false
        val newValue = value.shl(1).and(0xff)
        cpu.P.setNZFlags(newValue)
        value = newValue
    }
    override fun toString(): String = "ASL${name}"
}

/** 0x0a, ASL */
class Asl(c: Computer): AslBase(c, ASL, 1, 2) {
    override var value by RegisterAVal()
    override val name = nameA()
}

/** 0x0e, ASL $1234 */
class AslAbsolute(c: Computer): AslBase(c, ASL_ABS, 3, 6) {
    override var value by AbsoluteVal()
    override val name = nameAbs()
}

/** 0x06, ASL $12 */
class AslZp(c: Computer): AslBase(c, ASL_ZP, 2, 5) {
    override var value by ZpVal()
    override val name = nameZp()
}

