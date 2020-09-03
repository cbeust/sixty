package com.beust.sixty.op

import com.beust.sixty.*

abstract class StBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        val op: Operand)
    : InstructionBase(c)
{
    abstract val register: Int
    abstract val registerName: String

    override fun run() {
        op.set(register)
    }

    override fun toString(): String = "ST${registerName}${op.name}"
}

abstract class StaBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        op: Operand)
    : StBase(c, opCode, size, timing, op)
{
    override val registerName = "A"
    override var register = cpu.A
}

/** STA $12 */
class StaZp(c: Computer): StaBase(c, STA_ZP, 2, 3, OperandZp(c))

/** STA $12,X */
class StaZpX(c: Computer): StaBase(c, STA_ZP_X, 2, 4, OperandZpX(c))

/** STA $1234 */
class StaAbsolute(c: Computer): StaBase(c, STA_ABS, 3, 4, OperandAbsolute(c))

/** STA $1234,X */
class StaAbsoluteX(c: Computer): StaBase(c, STA_ABS_X, 3, 5, OperandAbsoluteX(c))

/** STA $1234,Y */
class StaAbsoluteY(c: Computer): StaBase(c, STA_ABS_Y, 3, 5, OperandAbsoluteY(c))

/** STA ($12,X) */
class StaIndX(c: Computer): StaBase(c, STA_IND_X, 2, 6, OperandIndirectX(c))

/** STA ($12),Y */
class StaIndY(c: Computer): StaBase(c, STA_IND_Y, 2, 6, OperandIndirectY(c))

abstract class StxBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        op: Operand)
    : StBase(c, opCode, size, timing, op)
{
    override val registerName = "X"
    override var register = cpu.X
}

/** STX $12 */
class StxZp(c: Computer): StxBase(c, STX_ZP, 2, 3, OperandZp(c))

/** STX $12,Y */
class StxZpY(c: Computer): StxBase(c, STX_ZP_Y, 2, 4, OperandZpY(c))

/** STX $1234 */
class StxAbsolute(c: Computer): StxBase(c, STX_ABS, 3, 4, OperandAbsolute(c))

abstract class StyBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        op: Operand)
    : StBase(c, opCode, size, timing, op)
{
    override val registerName = "Y"
    override var register = cpu.Y
}

/** STY $12 */
class StyZp(c: Computer): StyBase(c, STY_ZP, 2, 3, OperandZp(c))

/** STY $12,X */
class StyZpX(c: Computer): StyBase(c, STY_ZP_X, 2, 4, OperandZpX(c))

/** STY $1234 */
class StyAbsolute(c: Computer): StyBase(c, STY_ABS, 3, 4, OperandAbsolute(c))
