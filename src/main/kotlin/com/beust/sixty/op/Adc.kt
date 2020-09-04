package com.beust.sixty.op

import com.beust.sixty.*

abstract class AdcBase(override val opCode: Int, override val size: Int, override val timing: Int,
        override val addressing: Addressing)
    : AdcSbcBase("ADC", opCode, size, timing, addressing)
{
    override fun run(c: Computer, op: Operand) = with(c) {
        if (cpu.P.D) {
            var l: Int
            var h: Int
            var result: Int
            l = (cpu.A and 0x0f) + (op.get() and 0x0f) + cpu.P.C.int()
            if (l and 0xff > 9) l += 6
            h = (cpu.A shr 4) + (op.get() shr 4) + if (l > 15) 1 else 0
            if (h and 0xff > 9) h += 6
            result = l and 0x0f or (h shl 4)
            result = result and 0xff
            cpu.P.C = h > 15
            cpu.P.Z = result == 0
            cpu.P.V = false // BCD never sets overflow flag
            cpu.P.N = result and 0x80 != 0 // N Flag is valid on CMOS 6502/65816

            cpu.A = result
        } else {
            add(c, op.get())
        }
    }
}

/** ADC #$12 */
class AdcImmediate: AdcBase(ADC_IMM, 2, 2, Addressing.IMMEDIATE)

/** ADC $12 */
class AdcZp: AdcBase(ADC_ZP, 2, 3, Addressing.ZP)

/** ADC $12,X */
class AdcZpX: AdcBase(ADC_ZP_X, 2, 4, Addressing.ZP_X)

/** ADC $1234 */
class AdcAbsolute: AdcBase(ADC_ABS, 3, 4, Addressing.ABSOLUTE)

/** ADC $1234,X */
class AdcAbsoluteX: AdcBase(ADC_ABS_X, 3, 4, Addressing.ABSOLUTE_X) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** ADC $1234,Y */
class AdcAbsoluteY: AdcBase(ADC_ABS_Y, 3, 4, Addressing.ABSOLUTE_Y) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** 0x21, ADC ($12,X) */
class AdcIndX: AdcBase(ADC_IND_X, 2, 6, Addressing.INDIRECT_X)

/** 0x31, ADC ($12),Y */
class AdcIndY: AdcBase(ADC_IND_Y, 2, 5, Addressing.INDIRECT_Y) {
    override var timing = 4
    override fun run(c: Computer, op: Operand) = with(c) {
        super.run(c, op)
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

