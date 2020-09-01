package com.beust.sixty

abstract class IncBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : InstructionBase(c)
{
    abstract var value: Int
    abstract val name: String
    override fun run() {
        value = value + 1
        cpu.P.setNZFlags(value)
    }
    override fun toString(): String = "INC${name}"
}

/** 0xe6, INC $12 */
class IncZp(c: Computer): IncBase(c, INC_ZP, 2, 5) {
    override var value by ValZp()
    override val name = nameZp()
}

/** 0xf6, INC $12,X */
class IncZpX(c: Computer): IncBase(c, INC_ZP_X, 2, 6) {
    override var value by ValZpX()
    override val name = nameZpX()
}

/** 0xce, INC $1234 */
class IncAbsolute(c: Computer): IncBase(c, INC_ABS, 3, 6) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** 0xfe, INC $1234,X */
class IncAbsoluteX(c: Computer): IncBase(c, INC_ABS_X, 3, 7) {
    override var value by ValAbsoluteX()
    override val name = nameAbsX()
}


