package com.beust.sixty.op

import com.beust.sixty.*

abstract class OraBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : InstructionBase(c)
{
    abstract var value: Int
    abstract val name: String
    override fun run() {
        cpu.A = cpu.A.or(value)
        cpu.P.setNZFlags(value)
    }
    override fun toString(): String = "ORA${name}"
}

/** 0x09, ORA #$12 */
class OraImmediate(c: Computer): OraBase(c, ORA_IMM, 2, 2) {
    override var value by ValImmediate()
    override val name = nameImmediate()
}

/** 0x05, ORA $12 */
class OraZp(c: Computer): OraBase(c, ORA_ZP, 2, 3) {
    override var value by ValZp()
    override val name = nameZp()
}

/** 0x15, ORA $12,X */
class OraZpX(c: Computer): OraBase(c, ORA_ZP_X, 2, 4) {
    override var value by ValZpX()
    override val name = nameZpX()
}

/** 0x0d, ORA $1234 */
class OraAbsolute(c: Computer): OraBase(c, ORA_ABS, 3, 4) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** 0x1d, ORA $1234,X */
class OraAbsoluteX(c: Computer): OraBase(c, ORA_ABS_X, 3, 4) {
    override var value by ValAbsoluteX()
    override val name = nameAbsX()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** 0x19, ORA $1234,Y */
class OraAbsoluteY(c: Computer): OraBase(c, ORA_ABS_Y, 3, 4) {
    override var value by ValAbsoluteY()
    override val name = nameAbsY()
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** 0x01, ORA ($12,X) */
class OraIndX(c: Computer): OraBase(c, ORA_IND_X, 2, 6) {
    override var value by ValIndirectX()
    override val name = nameIndirectX()
}

/** 0x11, ORA ($12),Y */
class OraIndY(c: Computer): OraBase(c, ORA_IND_Y, 2, 5) {
    override var value by ValIndirectY()
    override val name = nameIndirectY()
    override var timing = 5
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

