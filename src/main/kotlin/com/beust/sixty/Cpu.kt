package com.beust.sixty

import com.beust.app.StackPointer
import com.beust.app.UiState
import java.lang.IllegalArgumentException

/**
 * Specs used:
 * https://en.wikipedia.org/wiki/MOS_Technology_6502
 * http://www.6502.org/tutorials/6502opcodes.html
 */

data class Cpu(val memory: IMemory,
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

    fun nextInstruction(pc: Int = PC, debugMemory: Boolean = false, debugAsm: Boolean = false): Int {
        val opCode = memory[pc]
//        var op = Op.find(opCode)
        val op = OPCODES[opCode]
        var timing = op.timing
        val addressingType = op.addressingType

        fun runInst(effectiveAddress: Int, indY: Int, absX: Int, absY: Int, closure: () -> Unit): Int {
            closure()
            P.setNZFlags(A)
            val result =
                if (opCode == indY) {
                    pageCrossed(memory.word(memory[PC - 1]), effectiveAddress)
                } else if (opCode == absX || opCode == absY) {
                    pageCrossed(memory.word(PC - 2), effectiveAddress)
                } else {
                    0
                }
            return result
        }

        when(opCode) {
            ADC_IMM -> adc(memory[pc + 1])
            ADC_ZP, ADC_ZP_X, ADC_ABS, ADC_ABS_X, ADC_ABS_Y, ADC_IND_X, ADC_IND_Y -> {
                addressingType.address(memory, pc, this).let { address ->
                    timing += runInst(address, ADC_IND_Y, ADC_ABS_X, ADC_ABS_Y) {
                        adc(memory[address])
                    }
                }
            }
            AND_IMM -> {
                A = A.and(memory[pc + 1])
                P.setNZFlags(A)
            }
            AND_ZP, AND_ZP_X, AND_ABS, AND_ABS_X, AND_ABS_Y, AND_IND_X, AND_IND_Y -> {
                addressingType.address(memory, pc, this).let { address ->
                    timing += runInst(address, AND_IND_Y, AND_ABS_X, AND_ABS_Y) {
                        A = A.and(memory[address])
                    }
                }
            }
            ASL -> A = asl(A)
            ASL_ZP, ASL_ZP_X, ASL_ABS, ASL_ABS_X -> {
                addressingType.address(memory, pc, this).let { address ->
                    memory[address] = asl(memory[address])
                }
            }
            BIT_ZP, BIT_ABS -> {
                addressingType.address(memory, pc, this).let { address ->
                    memory[address].let { v ->
                        P.Z = (v and A) == 0
                        P.N = (v and 0x80) != 0
                        P.V = (v and 0x40) != 0
                    }
                }
            }
            BPL -> timing += branch(memory[pc + 1], !P.N)
            BMI -> timing += branch(memory[pc + 1], P.N )
            BNE -> timing += branch(memory[pc + 1], ! P.Z)
            BEQ -> timing += branch(memory[pc + 1], P.Z)
            BCC -> timing += branch(memory[pc + 1], ! P.C)
            BCS -> timing += branch(memory[pc + 1], P.C)
            BVC -> timing += branch(memory[pc + 1], ! P.V)
            BVS -> timing += branch(memory[pc + 1], P.V)
            BRK -> {
//                throw IllegalArgumentException("BRK")
                handleInterrupt(true, IRQ_VECTOR_H, IRQ_VECTOR_L)
            }
            CMP_IMM -> cmp(A, memory[pc + 1])
            CMP_ZP, CMP_ZP_X, CMP_ABS, CMP_ABS_X, CMP_ABS_Y, CMP_IND_X, CMP_IND_Y -> {
                addressingType.address(memory, pc, this).let { address ->
                    cmp(A, memory[address])
                }
            }
            CPX_IMM -> cmp(X, memory[pc + 1])
            CPX_ZP, CPX_ABS -> {
                addressingType.address(memory, pc, this).let { address ->
                    cmp(X, memory[address])
                }
            }
            CPY_IMM -> cmp(Y, memory[pc + 1])
            CPY_ZP, CPY_ABS -> {
                addressingType.address(memory, pc, this).let { address ->
                    cmp(Y, memory[address])
                }
            }
            DEC_ZP, DEC_ZP_X, DEC_ABS, DEC_ABS_X -> {
                addressingType.address(memory, pc, this).let { address ->
                    (memory[address] - 1).and(0xff).let {
                        memory[address] = it
                        P.setNZFlags(it)
                    }
                }
            }
            EOR_IMM -> {
                A = A.xor(memory[pc + 1])
                P.setNZFlags(A)
            }
            EOR_ZP, EOR_ZP_X, EOR_ABS, EOR_ABS_X, EOR_ABS_Y, EOR_IND_Y, EOR_IND_X -> {
                addressingType.address(memory, pc, this).let { address ->
                    A = A.xor(memory[address])
                    P.setNZFlags(A)
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
                addressingType.address(memory, pc, this).let { address ->
                    val content = memory[address]
                    val word = memory.word(pc + 1)
                    if (opCode == INC_ABS_X && word == 0xc083) {
                        // Special case to support "false reads": https://github.com/AppleWin/AppleWin/issues/404
                        // inc $c083,x will actually cause an additional read on $c083
                        content
                    }
                    (content+ 1).and(0xff).let {
                        memory[address] = it
                        P.setNZFlags(it)
                    }
                }
            }
            JMP -> PC = memory.word(pc + 1)
            JMP_IND -> PC = memory.word(addressingType.address(memory, pc, this))
            JSR -> {
                SP.pushWord(PC - 1)
                PC = memory.word(pc + 1)
            }
            LDA_IMM -> {
                A = memory[pc + 1]
                P.setNZFlags(A)
            }
            LDA_ZP, LDA_ZP_X, LDA_ABS, LDA_ABS_X, LDA_ABS_Y, LDA_IND_X, LDA_IND_Y -> {
                addressingType.address(memory, pc, this).let { address ->
                    A = memory[address]
                    P.setNZFlags(A)
                }
            }
            LDX_IMM -> {
                X = memory[pc + 1]
                P.setNZFlags(X)
            }
            LDX_ZP, LDX_ZP_Y, LDX_ABS, LDX_ABS_Y -> {
                addressingType.address(memory, pc, this).let { address ->
                    X = memory[address]
                    P.setNZFlags(X)
                }
            }
            LDY_IMM -> {
                Y = memory[pc + 1]
                P.setNZFlags(Y)
            }
            LDY_ZP, LDY_ZP_X, LDY_ABS, LDY_ABS_X -> {
                addressingType.address(memory, pc, this).let { address ->
                    Y = memory[address]
                    P.setNZFlags(Y)
                }
            }
            LSR -> A = lsr(A)
            LSR_ZP, LSR_ZP_X, LSR_ABS, LSR_ABS_X -> {
                addressingType.address(memory, pc, this).let { address ->
                    memory[address] = lsr(memory[address])
                }
            }
            NOP -> {}
            ORA_IMM -> {
                A = A.or(memory[pc + 1])
                P.setNZFlags(A)
            }
            ORA_ZP, ORA_ZP_X, ORA_ABS, ORA_ABS_X, ORA_ABS_Y, ORA_IND_X, ORA_IND_Y -> {
                addressingType.address(memory, pc, this).let { address ->
                    A.or(memory[address]).let {
                        A = it
                        P.setNZFlags(it)
                    }
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
                addressingType.address(memory, pc, this).let { address ->
                    memory[address] = rol(memory[address])
                }
            }
            ROR -> A = ror(A)
            ROR_ZP, ROR_ZP_X, ROR_ABS, ROR_ABS_X -> {
                addressingType.address(memory, pc, this).let { address ->
                    memory[address] = ror(memory[address])
                }
            }
            RTI -> {
                P.fromByte(SP.popByte())
                PC = SP.popWord()
            }
            RTS -> {
                PC = SP.popWord() + 1
            }
            SBC_IMM -> {
                sbc(memory[pc + 1])
            }
            SBC_ZP, SBC_ZP_X, SBC_ABS, SBC_ABS_X, SBC_ABS_Y, SBC_IND_X, SBC_IND_Y -> {
                addressingType.address(memory, pc, this).let { address ->
                    sbc(memory[address])
                }
            }
            STA_ZP, STA_ZP_X, STA_ABS, STA_ABS_X, STA_ABS_Y, STA_IND_X, STA_IND_Y -> {
                addressingType.address(memory, pc, this).let { address ->
                    memory[address] = A
                }
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
                addressingType.address(memory, pc, this).let { address ->
                    memory[address] = X
                }
            }
            STY_ZP, STY_ZP_X, STY_ABS -> {
                addressingType.address(memory, pc, this).let { address ->
                    memory[address] = Y
                }
            }
            else -> {
                val message = "Unknown opcode: ${opCode.h()}"
                UiState.error.value = message
//                TODO("")
            }
        }
        return timing
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
    private fun branch(byte: Int, condition: Boolean): Int {
        var result = 0
        if (condition) {
            val old = PC
            PC += byte.toByte()
            result++
            result += pageCrossed(old, PC)
        }
        return result
    }
    
    private fun asl(v: Int): Int {
        P.C = v.and(0x80) != 0
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
        return if (old.xor(new).and(0xff00) > 0) 1 else 0
    }

    override fun toString(): String {
        return "A=${A.h()} X=${X.h()} Y=${Y.h()} S=${SP.S.h()} P=${P.toByte().h()} PC=\$${PC.h()} P=${P} SP=$SP"
    }
}
