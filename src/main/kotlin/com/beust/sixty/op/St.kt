package com.beust.sixty.op

import com.beust.sixty.*

abstract class StBase(override val name: String, override val opCode: Int, override val size: Int,
        override val timing: Int, val a: Addressing)
    : InstructionBase(name, opCode, size, timing, a)
{
    abstract fun register(c: Computer): Int
    abstract val registerName: String

    override fun run(c: Computer, op: Operand) {
        op.set(register(c))
    }
}

abstract class StaBase(override val opCode: Int, override val size: Int, override val timing: Int,
        a: Addressing)
    : StBase("STA", opCode, size, timing, a)
{
    override val registerName = "A"
    override fun register(c: Computer) = c.cpu.A
}

/** STA $12 */
class StaZp: StaBase(STA_ZP, 2, 3, Addressing.ZP)

/** STA $12,X */
class StaZpX: StaBase(STA_ZP_X, 2, 4, Addressing.ZP_X)

/** STA $1234 */
class StaAbsolute: StaBase(STA_ABS, 3, 4, Addressing.ABSOLUTE)

/** STA $1234,X */
class StaAbsoluteX: StaBase(STA_ABS_X, 3, 5, Addressing.ABSOLUTE_X)

/** STA $1234,Y */
class StaAbsoluteY: StaBase(STA_ABS_Y, 3, 5, Addressing.ABSOLUTE_Y)

/** STA ($12,X) */
class StaIndX: StaBase(STA_IND_X, 2, 6, Addressing.INDIRECT_X)

/** STA ($12),Y */
class StaIndY: StaBase(STA_IND_Y, 2, 6, Addressing.INDIRECT_Y)

abstract class StxBase(override val opCode: Int, override val size: Int, override val timing: Int,
        a: Addressing)
    : StBase("STX", opCode, size, timing, a)
{
    override val registerName = "X"
    override fun register(c: Computer) = c.cpu.X
}

/** STX $12 */
class StxZp: StxBase(STX_ZP, 2, 3, Addressing.ZP)

/** STX $12,Y */
class StxZpY: StxBase(STX_ZP_Y, 2, 4, Addressing.ZP_Y)

/** STX $1234 */
class StxAbsolute: StxBase(STX_ABS, 3, 4, Addressing.ABSOLUTE)

abstract class StyBase(override val opCode: Int, override val size: Int, override val timing: Int,
        a: Addressing)
    : StBase("STY", opCode, size, timing, a)
{
    override val registerName = "Y"
    override fun register(c: Computer) = c.cpu.Y
}

/** STY $12 */
class StyZp: StyBase(STY_ZP, 2, 3, Addressing.ZP)

/** STY $12,X */
class StyZpX: StyBase(STY_ZP_X, 2, 4, Addressing.ZP_X)

/** STY $1234 */
class StyAbsolute: StyBase(STY_ABS, 3, 4, Addressing.ABSOLUTE)
