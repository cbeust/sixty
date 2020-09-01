package com.beust.sixty

abstract class LsrBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : InstructionBase(c)
{
    abstract var value: Int
    abstract val name: String

    override fun run() {
        cpu.P.C = value.and(1.shl(7)).shr(7).toBoolean()
        val result = value.shr(1)
        cpu.P.setNZFlags(result)
        value = result
    }
    override fun toString(): String = "LSR${name}"
}

/** 0x46, LSR $12 */
class LsrZp(c: Computer): LsrBase(c, LSR_ZP, 2, 6) {
    override var value by ValZp()
    override val name = nameZp()
}

/** 0x4a, LSR */
class Lsr(c: Computer): LsrBase(c, LSR, 1, 2) {
    override var value by ValRegisterA()
    override val name = nameA()
}

/** 0x4e, LSR $1234 */
class LsrAbsolute(c: Computer): LsrBase(c, LSR_ABS, 3, 7) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** 0x56, LSR $12,X */
class LsrZpX(c: Computer): LsrBase(c, LSR_ZP_X, 2, 6) {
    override var value by ValZpX()
    override val name = nameZpX()
}

