package com.beust.sixty

abstract class DecBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : InstructionBase(c)
{
    abstract var value: Int
    abstract val name: String
    override fun run() {
        value = value - 1
        cpu.P.setNZFlags(value)
    }
    override fun toString(): String = "DEC${name}"
}

/** 0xc6, DEC $12 */
class DecZp(c: Computer): DecBase(c, DEC_ZP, 2, 5) {
    override var value by ValZp()
    override val name = nameZp()
}

/** 0xd6, Dec $12,X */
class DecZpX(c: Computer): DecBase(c, DEC_ZP_X, 2, 6) {
    override var value by ValZpX()
    override val name = nameZpX()
}

/** 0xce, Dec $1234 */
class DecAbsolute(c: Computer): DecBase(c, DEC_ABS, 3, 6) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** 0xde, Dec $1234,X */
class DecAbsoluteX(c: Computer): DecBase(c, DEC_ABS_X, 3, 7) {
    override var value by ValAbsoluteX()
    override val name = nameAbsX()
}


