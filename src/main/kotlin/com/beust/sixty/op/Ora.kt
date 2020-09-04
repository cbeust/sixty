package com.beust.sixty.op

import com.beust.sixty.*

abstract class OraBase(override val opCode: Int, override val size: Int, override val timing: Int,
        val a: Addressing)
    : InstructionBase("ORA", opCode, size, timing, a) {
    override fun run(c: Computer, op: Operand) = with(c) {
        cpu.A = cpu.A.or(op.get())
        cpu.P.setNZFlags(cpu.A)
    }
}

/** 0x09, ORA #$12 */
class OraImmediate: OraBase(ORA_IMM, 2, 2, Addressing.IMMEDIATE)

/** 0x05, ORA $12 */
class OraZp: OraBase(ORA_ZP, 2, 3, Addressing.ZP)

/** 0x15, ORA $12,X */
class OraZpX: OraBase(ORA_ZP_X, 2, 4, Addressing.ZP_X)

/** 0x0d, ORA $1234 */
class OraAbsolute: OraBase(ORA_ABS, 3, 4, Addressing.ABSOLUTE)

/** 0x1d, ORA $1234,X */
class OraAbsoluteX: OraBase(ORA_ABS_X, 3, 4, Addressing.ABSOLUTE_X) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** 0x19, ORA $1234,Y */
class OraAbsoluteY: OraBase(ORA_ABS_Y, 3, 4, Addressing.ABSOLUTE_Y) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** 0x01, ORA ($12,X) */
class OraIndX: OraBase(ORA_IND_X, 2, 6, Addressing.INDIRECT_X)

/** 0x11, ORA ($12),Y */
class OraIndY: OraBase(ORA_IND_Y, 2, 5, Addressing.INDIRECT_Y) {
    override var timing = 5
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

