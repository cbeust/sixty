package com.beust.sixty

import com.beust.app.StackPointer

/**
 * Specs used:
 * https://en.wikipedia.org/wiki/MOS_Technology_6502
 * http://www.6502.org/tutorials/6502opcodes.html
 */

data class Cpu2(val memory: Memory,
        var A: Int = 0, var X: Int = 0, var Y: Int = 0, var PC: Int = 0,
        val P: StatusFlags = StatusFlags()) {
    val SP: StackPointer = StackPointer(memory)

    fun run(debugMemory: Boolean = false, debugAsm: Boolean = false): Computer.RunResult {
        var done = false
        while (!done) {
            var opCode = memory[PC]
            var byte = memory[PC + 1]
            var word = byte.or(memory[PC + 2].shl(8))
            var timing = 1 // TODO
            // Opcode bits:  aaabbbcc where bbb determines the addressing
            val bbb = opCode.and(0x14).shr(2)
            val cc = opCode.and(0x3)
            val aaa = opCode.and(0xe0).shr(5)
            val effectiveAddress =
                if (cc == 0) {
                    when(bbb) {
                        1 -> byte // zp
                        3 -> word // absolute
                        5 -> byte + X // zp,X
                        7 -> word + X // abs,X
                        else -> -1
                    }
                } else if (cc == 1) {
                    when(bbb) {
                        0 -> byte + X // zp,x
                        1 -> byte // zp
                        3 -> word // absolute
                        4 -> memory[byte] + Y// (zp),y
                        5 -> byte + X// zp,X
                        6 -> word + Y // abs,Y
                        7 -> word + X // abs,X
                        else -> -1
                    }
                } else if (cc == 2) {
                    when(bbb) {
                        1 -> byte // zp
                        2 -> A // accumulator
                        3 -> word // absolute
                        5 -> byte + X // zp,X
                        7 -> word + X // abs,X
                        else -> -1
                    }
                } else {
                    -1
                }

            val mea = memory[effectiveAddress]

            when(opCode) {
                ADC_IMM -> {
                    adc(byte)
                }
                ADC_ZP, ADC_ZP_X, ADC_ABS, ADC_ABS_X, ADC_ABS_Y, ADC_IND_X, ADC_IND_Y -> {
                    adc(mea)
                    when(opCode) {
                        ADC_ABS_X, ADC_ABS_Y, ADC_IND_Y -> {
                            timing += pageCrossed(PC, effectiveAddress)
                        }
                    }
                }
                AND_IMM -> {
                    A = A.and(byte)
                    P.setNZFlags(A)
                }
                AND_ZP, AND_ZP_X, AND_ABS, AND_ABS_X, AND_ABS_Y, AND_IND_X, AND_IND_Y -> {
                    A = A.and(mea)
                    P.setNZFlags(A)
                    when(opCode) {
                        AND_ABS_X, AND_ABS_Y, AND_IND_Y -> {
                            timing += pageCrossed(PC, effectiveAddress)
                        }
                    }

                }
                ASL -> {
                    A = asl(A)
                }
                ASL_ZP, ASL_ZP_X, ASL_ZP_X, ASL_ABS, ASL_ABS_X -> {
                    memory[effectiveAddress] = asl(mea)
                }
                BIT_ZP, BIT_ABS -> mea.let { v ->
                    P.Z = (v and A) == 0
                    P.N = (v and 0x80) != 0
                    P.V = (v and 0x40) != 0
                }
                BPL -> timing += branch(byte) { ! P.N }
                BMI -> timing += branch(byte) { P.N }
                BNE -> timing += branch(byte) { ! P.Z }
                BEQ -> timing += branch(byte) { P.Z }
                BCC -> timing += branch(byte) { ! P.C }
                BCC -> timing += branch(byte) { P.C }
                BVC -> timing += branch(byte) { ! P.V }
                BVS -> timing += branch(byte) { P.V }
                BRK -> {
                    handleInterrupt(true, Cpu.IRQ_VECTOR_H, Cpu.IRQ_VECTOR_L)
                }
                CMP_IMM -> cmp(A, byte)
                CMP_ZP, CMP_ZP_X, CMP_ABS, CMP_ABS_X, CMP_ABS_Y, CMP_IND_X, CMP_IND_Y -> {
                    cmp(A, mea)
                }
                CPX_IMM -> cmp(X, byte)
                CPX_ZP, CPX_ABS -> cmp(X, mea)
                CPY_IMM -> cmp(Y, byte)
                CPY_ZP, CPY_ABS -> cmp(Y, mea)
                DEC_ZP, DEC_ZP_X, DEC_ABS, DEC_ABS_X -> {
                    memory[effectiveAddress] = mea - 1
                    P.setNZFlags(mea)
                }
                EOR_IMM -> {
                    A = A.xor(byte)
                    P.setNZFlags(A)
                }
                EOR_ZP, EOR_ZP_X, EOR_ABS, EOR_ABS_X, EOR_ABS_Y, EOR_IND_Y, EOR_IND_X -> {
                    A = A.xor(mea)
                    P.setNZFlags(A)
                }
                CLC -> P.C = false
                SEC -> P.C = true
                CLI -> P.I = false
                SEI -> P.I = true
                CLD -> P.D = false
                SED -> P.D = true
                CLV -> P.V = false
                INC_ZP, INC_ZP_X, INC_ABS, INC_ABS_X -> {
                    memory[effectiveAddress] = mea + 1
                    P.setNZFlags(mea)
                }
                JMP -> PC = word
                JSR -> {
                    SP.pushWord(PC - 1)
                    PC = word
                }
                LDA_IMM -> {
                    A = byte
                    P.setNZFlags(A)
                }
                LDA_ZP, LDA_ZP_X, LDA_ABS, LDA_ABS_X, LDA_ABS_Y, LDA_IND_X, LDA_IND_Y -> {
                    A = mea
                    P.setNZFlags(A)
                }
                LDX_IMM -> {
                    X = byte
                    P.setNZFlags(X)
                }
                LDX_ZP, LDX_ZP_Y, LDX_ABS, LDX_ABS_Y -> {
                    X = mea
                    P.setNZFlags(A)
                }
                LDY_IMM -> {
                    Y = byte
                    P.setNZFlags(X)
                }
                LDY_ZP, LDY_ZP_X, LDY_ABS, LDY_ABS_X -> {
                    Y = mea
                    P.setNZFlags(A)
                }
            }
        }
    }

    private fun cmp(register: Int, v: Int) {
        val tmp: Int = (register - v) and 0xff
        P.C = register >= v
        P.Z = tmp == 0
        P.N = (tmp and 0x80) != 0
    }

    private fun handleInterrupt(brk: Boolean, vectorHigh: Int, vectorLow: Int) {
            P.B = brk
            SP.pushWord(PC + 1)
            SP.pushByte(P.toByte())
            P.I = true
            PC = memory[vectorHigh].shl(8).or(memory[vectorLow])
    }

    /**
     * @return the value to add to the timing
     */
    private fun branch(byte: Int, condition: () -> Boolean): Int {
        var result = 0
        if (condition()) {
            val old = PC
            PC += byte.toByte()
            result++
            result += pageCrossed(old, PC)
        }
        return result
    }
    
    private fun asl(v: Int): Int {
        P.C = if (v.and(0x80) != 0) true else false
        val result = v.shl(1).and(0xff)
        P.setNZFlags(result)
        return result
    }

    private fun adc(v: Int) {
        if (P.D) {
            var l = (A and 0x0f) + (v and 0x0f) + P.C.int()
            if (l and 0xff > 9) l += 6
            var h = (A shr 4) + (v shr 4) + if (l > 15) 1 else 0
            if (h and 0xff > 9) h += 6
            var result = l and 0x0f or (h shl 4)
            result = result and 0xff
            P.C = h > 15
            P.Z = result == 0
            P.V = false // BCD never sets overflow flag
            P.N = result and 0x80 != 0 // N Flag is valid on CMOS 6502/65816

            A = result
        } else {
            var result: Int = A + v + P.C.int()
            val carry6: Int = A.and(0x7f) + v.and(0x7f) + P.C.int()
            P.C = result.and(0x100) != 0
            P.V = P.C.xor((carry6.and(0x80) != 0))
            result = result and 0xff
            P.setNZFlags(result)
            A = result
        }
    }

    /**
     * @return 1 if a page bounday was crossed, 0 otherwise
     */
    fun pageCrossed(old: Int, new: Int): Int {
        return if (old.and(0x80).xor(new.and(0x80)) != 0) 1 else 0
    }

}
