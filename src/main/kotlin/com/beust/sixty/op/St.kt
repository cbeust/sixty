package com.beust.sixty.op

import com.beust.sixty.*

abstract class StBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : InstructionBase(c)
{
    abstract val register: Int
    abstract val registerName: String
    abstract var value: Int
    abstract val name: String

    override fun run() {
        value = register
    }

    override fun toString(): String = "ST${registerName}${name}"
}

abstract class StaBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : StBase(c, opCode, size, timing)
{
    override val registerName = "A"
    override var register = cpu.A
}

/** STA $12 */
class StaZp(c: Computer): StaBase(c, STA_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** STA $12,X */
class StaZpX(c: Computer): StaBase(c, STA_ZP_X, 2, 4) {
    override var value by ValZpX()
    override val name = nameZpX()
}

/** STA $1234 */
class StaAbsolute(c: Computer): StaBase(c, STA_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** STA $1234,X */
class StaAbsoluteX(c: Computer): StaBase(c, STA_ABS_X, 3, 5) {
    override var value by ValAbsoluteX()
    override val name = nameAbsX()
}

/** STA $1234,Y */
class StaAbsoluteY(c: Computer): StaBase(c, STA_ABS_Y, 3, 5) {
    override var value by ValAbsoluteY()
    override val name = nameAbsY()
}

/** STA ($12,X) */
class StaIndX(c: Computer): StaBase(c, STA_IND_X, 2, 6) {
    override var value by ValIndirectX()
    override val name = nameIndirectX()
}

/** STA ($12),Y */
class StaIndY(c: Computer): StaBase(c, STA_IND_Y, 2, 6) {
    override var value by ValIndirectY()
    override val name = nameIndirectY()
}

abstract class StxBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : StBase(c, opCode, size, timing)
{
    override val registerName = "X"
    override var register = cpu.X
}

/** STX $12 */
class StxZp(c: Computer): StxBase(c, STX_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** STX $12,Y */
class StxZpY(c: Computer): StxBase(c, STX_ZP_Y, 2, 4) {
    override var value by ValZpY()
    override val name = nameZpY()
}

/** STX $1234 */
class StxAbsolute(c: Computer): StxBase(c, STX_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

abstract class StyBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : StBase(c, opCode, size, timing)
{
    override val registerName = "Y"
    override var register = cpu.Y
}

/** STY $12 */
class StyZp(c: Computer): StyBase(c, STY_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** STY $12,X */
class StyZpX(c: Computer): StyBase(c, STY_ZP_X, 2, 4) {
    override var value by ValZpX()
    override val name = nameZpX()
}

/** STY $1234 */
class StyAbsolute(c: Computer): StyBase(c, STY_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}
