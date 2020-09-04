package com.beust.sixty.op

import com.beust.sixty.*

abstract class SbcBase(override val opCode: Int, override val size: Int, override val timing: Int,
        val a: Addressing)
    : AdcSbcBase("SBC", opCode, size, timing, a)
{
    override fun run(c: Computer, op: Operand) = with(c) {
        if (cpu.P.D) {
            var l = (cpu.A and 0x0f) - (op.get() and 0x0f) - if (cpu.P.C) 0 else 1
            if (l and 0x10 != 0) l -= 6
            var h = (cpu.A shr 4) - (op.get() shr 4) - if (l and 0x10 != 0) 1 else 0
            if (h and 0x10 != 0) h -= 6
            val result = l and 0x0f or (h shl 4) and 0xff

            cpu.P.C = h and 0xff < 15
            cpu.P.Z = result == 0
            cpu.P.V = false // BCD never sets overflow flag
            cpu.P.N = result and 0x80 != 0 // N Flag is valid on CMOS 6502/65816

            cpu.A = result and 0xff
        } else {
            // Call ADC with the one complement of the operand
            add(c, (op.get().inv().and(0xff)))
        }
    }
}

/** SBC #$12 */
class SbcImmediate: SbcBase(SBC_IMM, 2, 2, Addressing.IMMEDIATE)

/** SBC $12 */
class SbcZp: SbcBase(SBC_ZP, 2, 3, Addressing.ZP)

/** SBC $12,X */
class SbcZpX: SbcBase(SBC_ZP_X, 2, 4, Addressing.ZP_X)

/** SBC $1234 */
class SbcAbsolute: SbcBase(SBC_ABS, 3, 4, Addressing.ABSOLUTE)

/** SBC $1234,X */
class SbcAbsoluteX: SbcBase(SBC_ABS_X, 3, 4, Addressing.ABSOLUTE_X) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** SBC $1234,Y */
class SbcAbsoluteY: SbcBase(SBC_ABS_Y, 3, 4, Addressing.ABSOLUTE_Y) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** 0x21, SBC ($12,X) */
class SbcIndX: SbcBase(SBC_IND_X, 2, 6, Addressing.INDIRECT_X)

/** 0x31, SBC ($12),Y */
class SbcIndY: SbcBase(SBC_IND_Y, 2, 5, Addressing.INDIRECT_Y) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

