package com.beust.sixty

import com.beust.app.StackPointer

/**
 * Specs used:
 * https://en.wikipedia.org/wiki/MOS_Technology_6502
 * http://www.6502.org/tutorials/6502opcodes.html
 */

data class Cpu(val memory: Memory,
        var A: Int = 0, var X: Int = 0, var Y: Int = 0,
        val P: StatusFlags = StatusFlags()) {
    val SP: StackPointer = StackPointer(memory)
    var PC: Int = 0
        set(n) {
//            if (n < 0x100) {
//                TODO("SHOULD NOT HAPPEN")
//            }
            field = n
        }
    fun toWord(address: Int) = memory[address].or(memory[address + 1].shl(8))

    fun nextInstruction(pc: Int = PC, debugMemory: Boolean = false, debugAsm: Boolean = false) {
        var opCode = memory[pc]
        var byte = memory[pc + 1]
        var word = byte.or(memory[pc + 2].shl(8))
        var timing = TIMINGS[opCode]
        val (effectiveAddress, mea) = when(ADDRESSING_TYPES[opCode]) {
            AddressingType.ABSOLUTE -> word to { -> memory[word] }
            AddressingType.ZP -> byte to { -> memory[byte] }
            AddressingType.ZP_X -> (byte + X).and(0xff).let { it to { -> memory[it] } }
            AddressingType.ZP_Y -> (byte + Y).and(0xff).let { it to { -> memory[it] } }
            AddressingType.ABSOLUTE -> word to { -> toWord(word) }
            AddressingType.ABSOLUTE_X -> (word + X).let { it to { -> memory[it] } }
            AddressingType.ABSOLUTE_Y -> (word + Y).let { it to { -> memory[it] } }
            AddressingType.INDIRECT -> word to { -> toWord(word) }
            AddressingType.INDIRECT_X -> toWord((byte + X).and(0xff)).let { it to { -> memory[it] } }
            AddressingType.INDIRECT_Y -> memory[byte].or(memory[(byte + 1).and(0xff)].shl(8))
                    .let { (it + Y) to { -> memory[it + Y] } }
            else -> 0 to { -> 0 }
        }
//        if (opCode == 0x91) {
//            println("BREAKPOINT")
//        }

        when(opCode) {
            ADC_IMM -> {
                adc(byte)
            }
            ADC_ZP, ADC_ZP_X, ADC_ABS, ADC_ABS_X, ADC_ABS_Y, ADC_IND_X, ADC_IND_Y -> {
                adc(mea())
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
                A = A.and(mea())
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
            ASL_ZP, ASL_ZP_X, ASL_ABS, ASL_ABS_X -> {
                memory[effectiveAddress] = asl(mea())
            }
            BIT_ZP, BIT_ABS -> mea().let { v ->
                P.Z = (v and A) == 0
                P.N = (v and 0x80) != 0
                P.V = (v and 0x40) != 0
            }
            BPL -> timing += branch(byte) { ! P.N }
            BMI -> timing += branch(byte) { P.N }
            BNE -> timing += branch(byte) { ! P.Z }
            BEQ -> timing += branch(byte) { P.Z }
            BCC -> timing += branch(byte) { ! P.C }
            BCS -> timing += branch(byte) { P.C }
            BVC -> timing += branch(byte) { ! P.V }
            BVS -> timing += branch(byte) { P.V }
            BRK -> {
                handleInterrupt(true, IRQ_VECTOR_H, IRQ_VECTOR_L)
            }
            CMP_IMM -> cmp(A, byte)
            CMP_ZP, CMP_ZP_X, CMP_ABS, CMP_ABS_X, CMP_ABS_Y, CMP_IND_X, CMP_IND_Y -> {
                cmp(A, mea())
                when(opCode) {
                    CMP_ABS_X, CMP_ABS_Y, CMP_IND_Y -> {
                        timing += pageCrossed(PC, effectiveAddress)
                    }
                }

            }
            CPX_IMM -> cmp(X, byte)
            CPX_ZP, CPX_ABS -> cmp(X, mea())
            CPY_IMM -> cmp(Y, byte)
            CPY_ZP, CPY_ABS -> cmp(Y, mea())
            DEC_ZP, DEC_ZP_X, DEC_ABS, DEC_ABS_X -> {
                (mea() - 1).and(0xff).let {
                    memory[effectiveAddress] = it
                    P.setNZFlags(it)
                }
            }
            EOR_IMM -> {
                A = A.xor(byte)
                P.setNZFlags(A)
            }
            EOR_ZP, EOR_ZP_X, EOR_ABS, EOR_ABS_X, EOR_ABS_Y, EOR_IND_Y, EOR_IND_X -> {
                A = A.xor(mea())
                P.setNZFlags(A)
                when(opCode) {
                    EOR_ABS_X, EOR_ABS_Y, EOR_IND_Y -> {
                        timing += pageCrossed(PC, effectiveAddress)
                    }
                }

            }
            CLC -> P.C = false
            SEC -> P.C = true
            CLI -> P.I = false
            SEI -> P.I = true
            CLD -> P.D = false
            SED -> P.D = true
            CLV -> P.V = false
            INC_ZP, INC_ZP_X, INC_ABS, INC_ABS_X -> {
                (mea() + 1).and(0xff).let {
                    memory[effectiveAddress] = it
                    P.setNZFlags(it)
                }
            }
            JMP -> PC = word
            JMP_IND -> PC = mea()
            JSR -> {
                SP.pushWord(PC - 1)
                PC = word
            }
            LDA_IMM -> {
                A = byte
                P.setNZFlags(A)
            }
            LDA_ZP, LDA_ZP_X, LDA_ABS, LDA_ABS_X, LDA_ABS_Y, LDA_IND_X, LDA_IND_Y -> {
                A = mea()
                P.setNZFlags(A)
                when(opCode) {
                    LDA_ABS_X, LDA_ABS_Y, LDA_IND_Y -> {
                        timing += pageCrossed(PC, effectiveAddress)
                    }

                }
            }
            LDX_IMM -> {
                X = byte
                P.setNZFlags(X)
            }
            LDX_ZP, LDX_ZP_Y, LDX_ABS, LDX_ABS_Y -> {
                X = mea()
                P.setNZFlags(X)
                when(opCode) {
                    LDX_ABS_Y -> {
                        timing += pageCrossed(PC, effectiveAddress)
                    }
                }
            }
            LDY_IMM -> {
                Y = byte
                P.setNZFlags(Y)
            }
            LDY_ZP, LDY_ZP_X, LDY_ABS, LDY_ABS_X -> {
                Y = mea()
                P.setNZFlags(Y)
                when(opCode) {
                    LDY_ABS_X -> {
                        timing += pageCrossed(PC, effectiveAddress)
                    }
                }
            }
            LSR -> A = lsr(A)
            LSR_ZP, LSR_ZP_X, LSR_ABS, LSR_ABS_X -> memory[effectiveAddress] = lsr(mea())
            NOP -> {}
            ORA_IMM -> {
                A = A.or(byte)
                P.setNZFlags(A)
                when(opCode) {
                    ORA_ABS_X, ORA_ABS_Y, ORA_IND_Y -> {
                        timing += pageCrossed(PC, effectiveAddress)
                    }
                }
            }
            ORA_ZP, ORA_ZP_X, ORA_ABS, ORA_ABS_X, ORA_ABS_Y, ORA_IND_X, ORA_IND_Y -> {
                A.or(mea()).let {
                    A = it
                    P.setNZFlags(it)
                }
            }
            TAX -> {
                X = A
                P.setNZFlags(X)
            }
            TXA -> {
                A = X
                P.setNZFlags(A)
            }
            DEX -> {
                X = (X - 1).and(0xff)
                P.setNZFlags(X)
            }
            INX -> {
                X = (X + 1).and(0xff)
                P.setNZFlags(X)
            }
            TAY -> {
                Y = A
                P.setNZFlags(Y)
            }
            TYA -> {
                A = Y
                P.setNZFlags(A)
            }
            DEY -> {
                Y = (Y - 1).and(0xff)
                P.setNZFlags(Y)
            }
            INY -> {
                Y = (Y + 1).and(0xff)
                P.setNZFlags(Y)
            }
            ROL -> A = rol(A)
            ROL_ZP, ROL_ZP_X, ROL_ABS, ROL_ABS_X -> {
                memory[effectiveAddress] = rol(mea())
            }
            ROR -> A = ror(A)
            ROR_ZP, ROR_ZP_X, ROR_ABS, ROR_ABS_X -> {
                memory[effectiveAddress] = ror(mea())
            }
            RTI -> {
                P.fromByte(SP.popByte())
                PC = SP.popWord()
            }
            RTS -> {
                PC = SP.popWord() + 1
            }
            SBC_IMM -> {
                sbc(byte)
            }
            SBC_ZP, SBC_ZP_X, SBC_ABS, SBC_ABS_X, SBC_ABS_Y, SBC_IND_X, SBC_IND_Y -> {
                sbc(mea())
                when(opCode) {
                    SBC_ABS_X, SBC_ABS_Y, SBC_IND_Y -> {
                        timing += pageCrossed(PC, effectiveAddress)
                    }
                }
            }
            STA_ZP, STA_ZP_X, STA_ABS, STA_ABS_X, STA_ABS_Y, STA_IND_X, STA_IND_Y -> {
                memory[effectiveAddress] = A
            }
            TXS -> SP.S = X
            TSX -> {
                X = SP.S.and(0xff)
                P.setNZFlags(X)
            }
            PHA -> SP.pushByte(A.toByte())
            PLA -> {
                A = SP.popByte().toInt().and(0xff)
                P.setNZFlags(A)
            }
            PHP -> {
                P.B = true
                P.reserved = true
                SP.pushByte(P.toByte())
            }
            PLP -> P.fromByte(SP.popByte())
            STX_ZP, STX_ZP_Y, STX_ABS -> {
                memory[effectiveAddress] = X
            }
            STY_ZP, STY_ZP_X, STY_ABS -> {
                memory[effectiveAddress] = Y
            }
            else -> {
                TODO("Unknown opcode: ${opCode.h()}")
            }
        }
    }

    private fun rol(v: Int): Int {
        val result = (v.shl(1).or(P.C.int())) and 0xff
        P.C = v.and(0x80) != 0
        P.setNZFlags(result)
        return result
    }

    private fun ror(v: Int): Int {
        val bit0 = v.and(1)
        val result = v.shr(1).or(P.C.int().shl(7))
        P.setNZFlags(result)
        P.C = bit0.toBoolean()
        return result
    }

    private fun lsr(v: Int): Int {
        val bit0 = v.and(1)
        P.C = bit0 != 0
        val result = v.shr(1)
        P.setNZFlags(result)
        return result
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

    private fun sbc(v: Int) {
        if (P.D) {
            var l = (A and 0x0f) - (v and 0x0f) - if (P.C) 0 else 1
            if (l and 0x10 != 0) l -= 6
            var h = (A shr 4) - (v shr 4) - if (l and 0x10 != 0) 1 else 0
            if (h and 0x10 != 0) h -= 6
            val result = l and 0x0f or (h shl 4) and 0xff

            P.C = h and 0xff < 15
            P.Z = result == 0
            P.V = false // BCD never sets overflow flag
            P.N = result and 0x80 != 0 // N Flag is valid on CMOS 6502/65816

            A = result and 0xff
        } else {
            // Call ADC with the one complement of the operand
            add((v.inv().and(0xff)))
        }
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
            add(v)
        }
    }

    private fun add(v: Int) {
        var result: Int = A + v + P.C.int()
        val carry6: Int = A.and(0x7f) + v.and(0x7f) + P.C.int()
        P.C = result.and(0x100) != 0
        P.V = P.C.xor((carry6.and(0x80) != 0))
        result = result and 0xff
        P.setNZFlags(result)
        A = result
    }

    /**
     * @return 1 if a page bounday was crossed, 0 otherwise
     */
    fun pageCrossed(old: Int, new: Int): Int {
        return if (old.and(0x80).xor(new.and(0x80)) != 0) 1 else 0
    }

    override fun toString(): String {
        return "A=${A.h()} X=${X.h()} Y=${Y.h()} S=${SP.S.h()} P=${P.toByte().h()} PC=\$${PC.h()} P=${P} SP=$SP"
    }
}
