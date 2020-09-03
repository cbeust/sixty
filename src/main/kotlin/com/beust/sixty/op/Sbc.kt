package com.beust.sixty.op

import com.beust.sixty.*

abstract class SbcBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int,
        val op: Operand)
    : AdcSbcBase(c, opCode, size, timing)
{
    override fun run() {
        if (cpu.P.D) {
            var l: Int
            var h: Int
            val result: Int
            l = (cpu.A and 0x0f) - (operand and 0x0f) - cpu.P.C.int()
            if (l and 0x10 != 0) l -= 6
            h = (cpu.A shr 4) - (operand shr 4) - if (l and 0x10 != 0) 1 else 0
            if (h and 0x10 != 0) h -= 6
            result = l and 0x0f or (h shl 4) and 0xff
            cpu.P.C = h and 0xff < 15
            cpu.P.Z = result == 0
            cpu.P.V = false // BCD never sets overflow flag
            cpu.P.N = result and 0x80 != 0
            cpu.A = result and 0xff
        } else {
            // Call ADC with the one complement of the operand
            add((op.get().inv().and(0xff)))
        }
    }
    override fun toString(): String = "SBC${op.name}"
}

/** SBC #$12 */
class SbcImmediate(c: Computer): SbcBase(c, SBC_IMM, 2, 2, OperandImmediate(c))

/** SBC $12 */
class SbcZp(c: Computer): SbcBase(c, SBC_ZP, 2, 3, OperandZp(c))

/** SBC $12,X */
class SbcZpX(c: Computer): SbcBase(c, SBC_ZP_X, 2, 4, OperandZpX(c))

/** SBC $1234 */
class SbcAbsolute(c: Computer): SbcBase(c, SBC_ABS, 3, 4, OperandAbsolute(c))

/** SBC $1234,X */
class SbcAbsoluteX(c: Computer): SbcBase(c, SBC_ABS_X, 3, 4, OperandAbsoluteX(c)) {
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.X)
    }
}

/** SBC $1234,Y */
class SbcAbsoluteY(c: Computer): SbcBase(c, SBC_ABS_Y, 3, 4, OperandAbsoluteY(c)) {
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
}

/** 0x21, SBC ($12,X) */
class SbcIndX(c: Computer): SbcBase(c, SBC_IND_X, 2, 6, OperandIndirectX(c))

/** 0x31, SBC ($12),Y */
class SbcIndY(c: Computer): SbcBase(c, SBC_IND_Y, 2, 5, OperandIndirectY(c)) {
    override var timing = 4
    override fun run() {
        super.run()
        timing += pageCrossed(cpu.PC, memory[word] + cpu.Y)
    }
}

