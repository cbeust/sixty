package com.beust.sixty

abstract class RolBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : InstructionBase(c)
{
    abstract var value: Int
    abstract val name: String

    override fun run() {
        val bit7 = if (value.and(1.shl(7)) != 0) 1 else 0
        val result = value.shl(1).or(cpu.P.C.int())
        cpu.P.setNZFlags(result)
        cpu.P.C = bit7.toBoolean()
        value = result
    }
    override fun toString(): String = "ROL${name}"
}

/** 0x2A, ROL */
class Rol(c: Computer): RolBase(c, ROL, 1, 2) {
    override var value by ValRegisterA()
    override val name = nameA()
}

/** 0x26, ROL $12 */
class RolZp(c: Computer): RolBase(c, ROL_ZP, 2, 5) {
    override var value by ValZp()
    override val name = nameZp()
}

/** 0x36, ROL $12,X */
class RolZpX(c: Computer): RolBase(c, ROL_ZP_X, 2, 6) {
    override var value by ValZp()
    override val name = nameZpX()
}

/** 0x2e, ROL $1234 */
class RolAbsolute(c: Computer): RolBase(c, ROL_ABS, 3, 6) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** 0x3e, ROL $1234,X */
class RolAbsoluteX(c: Computer): RolBase(c, ROL_ABS_X, 3, 7) {
    override var value by ValAbsoluteX()
    override val name = nameAbsX()
}
