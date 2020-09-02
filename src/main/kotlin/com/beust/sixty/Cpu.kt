//@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.beust.sixty

import com.beust.app.StackPointer
import com.beust.sixty.op.*
import kotlin.reflect.KProperty

/**
 * Specs used:
 * https://en.wikipedia.org/wiki/MOS_Technology_6502
 * http://www.6502.org/tutorials/6502opcodes.html
 */

data class Cpu(var A: Int = 0, var X: Int = 0, var Y: Int = 0, var PC: Int = 0xffff,
        val memory: Memory, val P: StatusFlags = StatusFlags())
{
    val SP: StackPointer

    init {
        SP = StackPointer(memory)
    }

    fun clone() = Cpu(A, X, Y, PC, memory.clone(), P)

    fun nextInstruction(computer: Computer, noThrows: Boolean = false): InstructionBase? {
        val op = computer.memory[PC] and 0xff
        val result = when(op) {
            BRK -> Brk(computer)
            PHP -> Php(computer)
            CLC -> Clc(computer)
            JSR -> Jsr(computer)
            ROL_ZP -> RolZp(computer)
            PLP -> Plp(computer)
            ROL -> Rol(computer)
            ROL_ABS -> RolAbsolute(computer)
            SEC -> Sec(computer)
            ROL_ABS_X -> RolAbsoluteX(computer)
            ROL_ZP_X -> RolZpX(computer)
            RTI -> Rti(computer)
            LSR_ZP -> LsrZp(computer)
            LSR -> Lsr(computer)
            JMP -> Jmp(computer)
            PHA -> Pha(computer)
            LSR_ABS -> LsrAbsolute(computer)
            LSR_ABS_X -> LsrAbsoluteX(computer)
            LSR_ZP_X -> LsrZpX(computer)
            CLI -> Cli(computer)
            RTS -> Rts(computer)
            ROR_ZP -> RorZp(computer)
            PLA -> Pla(computer)
            JMP_IND -> JmpIndirect(computer)
            ROR -> Ror(computer)
            ROR_ABS -> RorAbsolute(computer)
            ROR_ZP_X -> RorZpX(computer)
            SEI -> Sei(computer)
            ROR_ABS_X -> RorAbsoluteX(computer)
            STA_IND_X -> StaIndirectX(computer)
            LDA_IND_Y -> LdaIndirectY(computer)
            STY_ZP -> StyZp(computer)
            STA_ZP -> StaZp(computer)
            STX_ZP -> StxZp(computer)
            DEY -> Dey(computer)
            STY_ABS -> StyAbsolute(computer)
            STA_ABS -> StaAbsolute(computer)
            STX_ABS -> StxAbsolute(computer)
            TXA -> Txa(computer)
            STA_IND_Y -> StaIndirectY(computer)
            STY_ZP_X-> StyZpX(computer)
            STA_ZP_X -> StaZpX(computer)
            STX_ZP_Y -> StxZpY(computer)
            TYA -> Tya(computer)
            STA_ABS_Y -> StaAbsoluteY(computer)
            TXS -> Txs(computer)
            STA_ABS_X -> StaAbsoluteX(computer)
            LDY_IMM -> LdyImm(computer)
            LDA_IND_X -> LdaIndirectX(computer)
            LDX_IMM -> LdxImm(computer)
            LDY_ZP -> LdyZp(computer)
            LDA_ZP -> LdaZp(computer)
            LDX_ZP -> LdxZp(computer)
            TAY -> Tay(computer)
            LDA_IMM -> LdaImm(computer)
            TAX -> Tax(computer)
            LDY_ABS -> LdyAbsolute(computer)
            LDA_ABS -> LdaAbsolute(computer)
            LDX_ABS -> LdXAbsolute(computer)
            LDY_ZP_X -> LdyZpX(computer)
            LDA_ZP_X -> LdaZpX(computer)
            LDX_ZP_Y -> LdxZpY(computer)
            LDA_ABS_Y -> LdaAbsoluteY(computer)
            CLV -> Clv(computer)
            TSX -> Tsx(computer)
            LDY_ABS_X -> LdyAbsoluteX(computer)
            LDA_ABS_X -> LdaAbsoluteX(computer)
            LDX_ABS_Y -> LdxAbsoluteY(computer)
            INY -> Iny(computer)
            DEX -> Dex(computer)
            CLD -> Cld(computer)
            INX -> Inx(computer)
            NOP -> Nop(computer)
            SED -> Sed(computer)

            ASL -> Asl(computer)
            ASL_ZP -> AslZp(computer)
            ASL_ZP_X -> AslZpX(computer)
            ASL_ABS -> AslAbsolute(computer)
            ASL_ABS_X -> AslAbsoluteX(computer)

            BPL -> Bpl(computer)
            BMI -> Bmi(computer)
            BVC -> Bvc(computer)
            BVS -> Bvs(computer)
            BCC -> Bcc(computer)
            BCS -> Bcs(computer)
            BNE -> Bne(computer)
            BEQ -> Beq(computer)

            CMP_IMM -> CmpImmediate(computer)
            CMP_ZP -> CmpZp(computer)
            CMP_ZP_X-> CmpZpX(computer)
            CMP_ABS -> CmpAbsolute(computer)
            CMP_ABS_X -> CmpAbsoluteX(computer)
            CMP_ABS_Y -> CmpAbsoluteY(computer)
            CMP_IND_X -> CmpIndX(computer)
            CMP_IND_Y -> CmpIndY(computer)

            CPX_IMM -> CpxImm(computer)
            CPX_ZP -> CpxZp(computer)
            CPX_ABS -> CpxAbsolute(computer)

            CPY_IMM -> CpyImm(computer)
            CPY_ZP -> CpyZp(computer)
            CPY_ABS -> CpyAbsolute(computer)

            DEC_ZP -> DecZp(computer)
            DEC_ZP_X -> DecZpX(computer)
            DEC_ABS -> DecAbsolute(computer)
            DEC_ABS_X -> DecAbsoluteX(computer)

            INC_ZP -> IncZp(computer)
            INC_ZP_X -> IncZpX(computer)
            INC_ABS -> IncAbsolute(computer)
            INC_ABS_X -> IncAbsoluteX(computer)

            AND_IMM -> AndImmediate(computer)
            AND_ZP -> AndZp(computer)
            AND_ZP_X -> AndZpX(computer)
            AND_ABS -> AndAbsolute(computer)
            AND_ABS_X -> AndAbsoluteX(computer)
            AND_ABS_Y -> AndAbsoluteY(computer)
            AND_IND_X -> AndIndX(computer)
            AND_IND_Y -> AndIndY(computer)

            ADC_IMM -> AdcImm(computer)
            ADC_ZP -> AdcZp(computer)
            ADC_ZP_X -> AdcZpX(computer)
            ADC_ABS -> AdcAbsolute(computer)
            ADC_ABS_X -> AdcAbsoluteX(computer)
            ADC_ABS_Y-> AdcAbsoluteY(computer)
            ADC_IND_X -> AdcIndX(computer)
            ADC_IND_Y -> AdcIndY(computer)

            EOR_IMM -> EorImmediate(computer)
            EOR_ZP -> EorZp(computer)
            EOR_ZP_X -> EorZpX(computer)
            EOR_ABS -> EorAbsolute(computer)
            EOR_IND_X -> EorIndX(computer)
            EOR_IND_Y -> EorIndY(computer)
            EOR_ABS_X -> EorAbsoluteX(computer)
            EOR_ABS_Y -> EorAbsoluteY(computer)

            ORA_IMM -> OraImmediate(computer)
            ORA_ZP -> OraZp(computer)
            ORA_ZP_X -> OraZpX(computer)
            ORA_ABS -> OraAbsolute(computer)
            ORA_IND_X -> OraIndX(computer)
            ORA_IND_Y -> OraIndY(computer)
            ORA_ABS_X-> OraAbsoluteX(computer)
            ORA_ABS_Y -> OraAbsoluteY(computer)

            SBC_IMM -> SbcImmediate(computer)
            SBC_ZP -> SbcZp(computer)
            SBC_ZP_X -> SbcZpX(computer)
            SBC_ABS -> SbcAbsolute(computer)
            SBC_ABS_X -> SbcAbsoluteX(computer)
            SBC_ABS_Y -> SbcAbsoluteY(computer)
            SBC_IND_X -> SbcIndX(computer)
            SBC_IND_Y -> SbcIndY(computer)

            BIT_ZP -> BitZp(computer)
            BIT_ABS -> BitAbsolute(computer)

            else -> {
                if (noThrows) {
                    Unknown(computer, op)
                } else {
                    null
                }
            }
        }

        return result
    }

    override fun toString(): String {
        return "A=${A.h()} X=${X.h()} Y=${Y.h()} S=${SP.S.h()} P=${P.toByte().h()} PC=\$${PC.h()} P=${P} SP=$SP"
    }

    companion object {
        // NMI vector
//        const val NMI_VECTOR_L = 0xfffa
//        const val NMI_VECTOR_H = 0xfffb
//
//        // Reset vector
//        const val RST_VECTOR_L = 0xfffc
//        const val RST_VECTOR_H = 0xfffd

        // IRQ vector
        const val IRQ_VECTOR_L = 0xfffe
        const val IRQ_VECTOR_H = 0xffff
    }
}

abstract class InstructionBase(val computer: Computer): Instruction {
    val cpu by lazy { computer.cpu }
    val memory by lazy { computer.memory }
    val pc by lazy { cpu.PC}
    val operand by lazy { memory[pc + 1] }
    val word by lazy { memory[pc + 2].shl(8).or(memory[pc + 1]) }

    var changedPc: Boolean = false

    inner class ValImmediate {
        operator fun getValue(thisRef: Any?, property: KProperty<*>) = operand
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) { TODO("Should never happen") }
    }

    inner class ValAbsolute {
        operator fun getValue(thisRef: Any?, property: KProperty<*>) = memory[word]
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) { memory[word] = value }
    }

    inner class ValAbsoluteX {
        operator fun getValue(thisRef: Any?, property: KProperty<*>) = memory[word + cpu.X]
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) { memory[word + cpu.X] = value }
    }

    inner class ValAbsoluteY {
        operator fun getValue(thisRef: Any?, property: KProperty<*>) = memory[word + cpu.Y]
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) { memory[word + cpu.Y] = value }
    }

    inner class ValIndirectX {
        operator fun getValue(thisRef: Any?, property: KProperty<*>) = memory[memory[operand + cpu.X]]
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            memory[memory[operand + cpu.X]] = value
        }
    }

    inner class ValIndirectY{
        operator fun getValue(thisRef: Any?, property: KProperty<*>) = memory[memory[operand] + cpu.Y]
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            memory[memory[operand] + cpu.Y] = value
        }
    }

    inner class ValZp {
        operator fun getValue(thisRef: Any?, property: KProperty<*>) = memory[operand]
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) { memory[operand] = value }
    }

    inner class ValZpX {
        operator fun getValue(thisRef: Any?, property: KProperty<*>) = memory[operand + cpu.X]
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) { memory[operand + cpu.X] = value }
    }

    inner class ValRegisterA {
        operator fun getValue(thisRef: Any?, property: KProperty<*>) = cpu.A
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) { cpu.A = value }
    }

    protected fun nameImmediate() = " #$${operand.h()}"
    protected fun nameZp() = " $${operand.h()}"
    protected fun nameZpX() = nameZp() + ",X"
    protected fun nameAbs() = " $${word.hh()}"
    protected fun nameAbsX() = nameAbs() + ",X"
    protected fun nameAbsY() = nameAbs() + ",Y"
    protected fun nameA() = ""
    protected fun nameIndirectX() = "($${operand.h()},X)"
    protected fun nameIndirectY() = "($${operand.h()}),Y"

    protected fun indirectX(address: Int): Int = memory[address + cpu.X]
    protected fun indirectY(address: Int): Int = memory[address] + cpu.Y
}

/** 0x00, BRK */
class Brk(c: Computer): InstructionBase(c) {
    private fun handleInterrupt(brk: Boolean, vectorHigh: Int, vectorLow: Int) {
        cpu.SP.pushWord(cpu.PC + 2)
        cpu.SP.pushByte(cpu.P.toByte())
        cpu.P.I = true
        cpu.PC = memory[vectorHigh].shl(8).or(memory[vectorLow])
    }

    override val opCode = 0
    override val size = 1
    override val timing = 7
    override fun run() {
        changedPc = true
        handleInterrupt(true, Cpu.IRQ_VECTOR_H, Cpu.IRQ_VECTOR_L)
        cpu.P.B = true
//        cpu.P.I = false
        cpu.P.reserved = true
    }
    override fun toString(): String = "BRK"
}

/** 0x8, PHP */
class Php(c: Computer): StackInstruction(c, PHP, "PHP") {
    override val timing = 3
    override fun run() {
        cpu.P.B = true
        cpu.P.reserved = true
        cpu.SP.pushByte(cpu.P.toByte())
    }
}

abstract class FlagInstruction(c: Computer, override val opCode: Int, val name: String): InstructionBase(c) {
    override val size = 1
    override val timing = 2
    override fun toString(): String = name
}

/** 0x18, CLC */
class Clc(c: Computer): FlagInstruction(c, 0x18, "CLC") {
    override fun run() { cpu.P.C = false }
}

abstract class StackInstruction(c: Computer, override val opCode: Int, val name: String): InstructionBase(c) {
    override val size = 1
    override fun toString(): String = name
}

/** 0x20, JSR $1234 */
class Jsr(c: Computer): InstructionBase(c) {
    override val opCode = 0x20
    override val size = 3
    override val timing = 6
    override fun run() {
        changedPc = true
        cpu.SP.pushWord(pc + size - 1)
        cpu.PC = word
    }
    override fun toString(): String = "JSR $${word.hh()}"
}

/** 0x28, PLP */
class Plp(c: Computer): StackInstruction(c, PLP, "PLP") {
    override val timing = 4
    override fun run() {
        cpu.P.fromByte(cpu.SP.popByte())
    }
}

/** 0x38, SEC */
class Sec(c: Computer): FlagInstruction(c, 0x38, "SEC") {
    override fun run() { cpu.P.C = true }
}

/** 0x40, RTI */
class Rti(c: Computer): InstructionBase(c) {
    override val opCode = RTI
    override val size = 1
    override val timing = 6
    override fun run() {
        changedPc = true
        cpu.P.fromByte(cpu.SP.popByte())
        cpu.PC = cpu.SP.popWord()
    }
    override fun toString(): String = "RTI"
}

/** 0x48, PHA */
class Pha(c: Computer): StackInstruction(c, 0x48, "PHA") {
    override val timing = 3
    override fun run() = cpu.SP.pushByte(cpu.A.toByte())
}

/** 0x58, CLI */
class Cli(c: Computer): FlagInstruction(c, 0x58, "CLI") {
    override fun run() { cpu.P.I = false }
}

/** 0x4c, JMP $1234 */
class Jmp(c: Computer): InstructionBase(c) {
    override val opCode = 0x4c
    override val size = 3
    override val timing = 3
    override fun run() {
        changedPc = true
        cpu.PC = word
    }

    override fun toString(): String = "JMP $${word.hh()}"
}

/** 0x60, RTS */
class Rts(c: Computer): InstructionBase(c) {
    override val opCode = 0x60
    override val size = 1
    override val timing = 6
    override fun run() {
        changedPc = true
        computer.cpu.PC = cpu.SP.popWord() + 1
    }
    override fun toString(): String = "RTS"
}

/** 0x68, PLA */
class Pla(c: Computer): StackInstruction(c, 0x68, "PLA") {
    override val timing = 4
    override fun run() {
        cpu.A = cpu.SP.popByte().toInt().and(0xff)
        cpu.P.setNZFlags(cpu.A)
    }
}

abstract class AddBase(c: Computer): InstructionBase(c) {
    fun adc(value: Int, operand: Int): Int {
        var result: Int = operand + value + cpu.P.C.int()
        val carry6: Int = operand.and(0x7f) + value.and(0x7f) + cpu.P.C.int()
        cpu.P.C = result.and(0x100) == 1
        cpu.P.V = cpu.P.C.xor((carry6.and(0x80) != 0))
        result = result and 0xff
        cpu.P.setNZFlags(result)
        return result
    }
}

/** 0x69, ADC #$12 */
class AdcImm(c: Computer): AddBase(c) {
    override val opCode = 0x69
    override val size = 2
    override val timing = 2
    override fun run() { cpu.A = adc(cpu.A, operand) }
    override fun toString(): String = "ADC #${operand.h()}"
}

/** 0x6c, JMP ($0036) */
class JmpIndirect(c: Computer): InstructionBase(c) {
    override val opCode = 0x6c
    override val size = 3
    override val timing = 5
    override fun run() {
        changedPc = true
        cpu.PC = memory.wordAt(word)
    }
    override fun toString(): String = "JMP ($${word.hh()})"
}

/** 0x78, SEI */
class Sei(c: Computer): FlagInstruction(c, 0x78, "SEI") {
    override fun run() { cpu.P.I = true }
}

/** 0x81, STA($82,X) */
class StaIndirectX(c: Computer): InstructionBase(c) {
    override val opCode = STA_IND_X
    override val size = 2
    override val timing = 6
    override fun run() {
        val targetAddress = indirectX(operand)
        memory[targetAddress] = cpu.A
    }
    override fun toString(): String = "STA ($${operand.h()},X)"
}

/** 0xb1, LDA $1234 */
class LdaIndirectY(c: Computer): InstructionBase(c) {
    override val opCode = LDA_IND_Y
    override val size = 2
    override var timing = 5  // variable timing
    override fun run() {
        val targetAddress = indirectY(operand)
        val new = memory[targetAddress]
        cpu.P.setNZFlags(new)
        timing += pageCrossed(cpu.PC, targetAddress)
        cpu.A = new
    }
    override fun toString(): String = "LDA $${word.hh()}"
}

/** 0x84, STY $10 */
class StyZp(c: Computer): ZpBase(c, 0x84, "STY") {
    override fun run() { memory[operand] = cpu.Y }
}

/** 0x85, STA ($10) */
class StaZp(c: Computer): InstructionBase(c) {
    override val opCode = 0x85
    override val size = 2
    override val timing = 3
    override fun run() { memory[operand] = cpu.A }
    override fun toString(): String = "STA $" + memory[cpu.PC + 1].h()
}

/** 0x86, STX $10 */
class StxZp(c: Computer): ZpBase(c, 0x86, "STX") {
    override fun run() { memory[operand] = cpu.X }
}

/** 0x88, INX */
class Dey(c: Computer): RegisterInstruction(c, 0x88, "DEY") {
    override fun run() {
        cpu.Y = (--cpu.Y).and(0xff)
        cpu.P.setNZFlags(cpu.Y)
    }
}

/** 0x8c, STY ($1234) */
class StyAbsolute(c: Computer): InstructionBase(c) {
    override val opCode = 0x8c
    override val size = 3
    override val timing = 4
    override fun run() { memory[word] = cpu.Y }
    override fun toString(): String = "STY $${word.hh()}"
}

/** 0x8d, STA ($1234) */
class StaAbsolute(c: Computer): InstructionBase(c) {
    override val opCode = 0x8d
    override val size = 3
    override val timing = 4
    override fun run() { memory[word] = cpu.A }
    override fun toString(): String = "STA $${word.hh()}"
}

/** 0x8e, STX ($1234) */
class StxAbsolute(c: Computer): InstructionBase(c) {
    override val opCode = 0x8e
    override val size = 3
    override val timing = 4
    override fun run() { memory[word] = cpu.X }
    override fun toString(): String = "STX $${word.hh()}"
}

/** 0x8a, TXA */
class Txa(c: Computer): RegisterInstruction(c, 0x8a, "TXA") {
    override fun run() {
        cpu.A = cpu.X
        cpu.P.setNZFlags(cpu.A)
    }
}

/** 0x91, STA ($12),Y */
class StaIndirectY(c: Computer): InstructionBase(c) {
    override val opCode = 0x91
    override val size = 2
    override val timing = 6
    override fun run() {
        val targetAddress = indirectY(operand)
        memory[targetAddress] = cpu.A
    }
    override fun toString(): String = "STA ($${operand.toByte().h()}),Y"
}

abstract class StyZpInd(c: Computer, override val opCode: Int, private val name: String): InstructionBase(c) {
    override val size = 2
    override val timing = 4
    override fun toString(): String = "$name $${operand.h()},X"
}

/** 0x94, STY $12,X */
class StyZpX(c: Computer): StyZpInd(c, STY_ZP_X, "STY") {
    override fun run() { memory[operand + cpu.X] = cpu.Y }
}

/** 0x95, STA $12,X */
class StaZpX(c: Computer): StyZpInd(c, STA_ZP_X, "STA") {
    override fun run() { memory[operand + cpu.X] = cpu.A }
}

/** 0x96, STX $12,Y */
class StxZpY(c: Computer): InstructionBase(c) {
    override val opCode = STX_ZP_Y
    override val size = 2
    override val timing = 4
    override fun run() {
        memory[operand + cpu.Y] = cpu.X
    }
    override fun toString(): String = "STX $${operand.h()},Y"
}

/** 0x98, TYA */
class Tya(c: Computer): RegisterInstruction(c, 0x98, "TYA") {
    override fun run() {
        cpu.A = cpu.Y
        cpu.P.setNZFlags(cpu.A)
    }
}

/** 0x99, STA $1234,Y */
class StaAbsoluteY(c: Computer): InstructionBase(c) {
    override val opCode = STA_ABS_Y
    override val size = 3
    override val timing = 5
    override fun run() {
        memory[operand + cpu.Y] = cpu.A
    }
    override fun toString(): String = "STA $${operand.h()},X"
}

/** 0x9a, TXS */
class Txs(c: Computer): StackInstruction(c, 0x9a, "TXS") {
    override val timing = 2
    override fun run() { cpu.SP.S = cpu.X }
}

abstract class ZpBase(c: Computer, override val opCode: Int, private val name: String,
        private val suffix: String = "") : InstructionBase(c) {
    override val size = 2
    override val timing = 3
    override fun toString(): String = "$name $" + operand.h() + suffix
}

/** 0xa4, LDY $10 */
class LdyZp(c: Computer): ZpBase(c, 0xa5, "LDY") {
    override fun run() {
        cpu.Y = memory[operand]
        cpu.P.setNZFlags(cpu.Y)
    }
}

/** 0xa5, LDA $10 */
class LdaZp(c: Computer): ZpBase(c, 0xa5, "LDA") {
    override fun run() {
        cpu.A = memory[operand]
        cpu.P.setNZFlags(cpu.A)
    }
}

/** 0xa6, LDX $10 */
class LdxZp(c: Computer): ZpBase(c, 0xa6, "LDX") {
    override fun run() {
        cpu.X = memory[operand]
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xa8, TAY */
class Tay(c: Computer): RegisterInstruction(c, 0xa8, "TAY") {
    override fun run() {
        cpu.Y = cpu.A
        cpu.P.setNZFlags(cpu.Y)
    }
}

abstract class LdImmBase(c: Computer, override val opCode: Int, val name: String): InstructionBase(c) {
    override val size = 2
    override val timing = 2
    override fun toString(): String = "$name #$" + operand.h()
}

/** 0x9d, STA $1234,X */
class StaAbsoluteX(c: Computer): InstructionBase(c) {
    override val opCode = STA_ABS_X
    override val size = 3
    override val timing = 5
    override fun run() { memory[word + cpu.X] = cpu.A }
    override fun toString(): String = "STA $${word.hh()},X"
}

/** 0xa0, LDY #$10 */
class LdyImm(c: Computer): LdImmBase(c, 0xa0, "LDY") {
    override fun run() {
        cpu.Y = operand
        cpu.P.setNZFlags(cpu.Y)
    }
}

/** 0xa1, LDA ($12,X) */
class LdaIndirectX(c: Computer): InstructionBase(c) {
    override val opCode = LDA_IND_X
    override val size = 2
    override var timing = 6
    override fun run() {
        val targetAddress = indirectX(operand)
        val new = memory[targetAddress]
        cpu.P.setNZFlags(new)
        cpu.A = new
    }
    override fun toString(): String = "LDA ($${operand.h()},X}"
}

/** 0xa2, LDX #$10 */
class LdxImm(c: Computer): LdImmBase(c, 0xa2, "LDX") {
    override fun run() {
        cpu.X = operand
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xa9, LDA #$10 */
class LdaImm(c: Computer): LdImmBase(c, 0xa9, "LDA") {
    override fun run() {
        cpu.A = operand
        cpu.P.setNZFlags(cpu.A)
    }
}

/** 0xaa, TAX */
class Tax(c: Computer): RegisterInstruction(c, 0xaa, "TAX") {
    override fun run() {
        cpu.X = cpu.A
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xad, LDA $1234 */
abstract class LdAbsoluteBase(c: Computer, override val opCode: Int, private val name: String): InstructionBase(c) {
    override val size = 3
    override val timing = 4
    override fun toString(): String = "$name $${word.hh()}"
}

/** 0xac, LDY $1234 */
class LdyAbsolute(c: Computer): LdAbsoluteBase(c, LDY_ABS, "LDY") {
    override fun run() {
        cpu.Y = memory[word]
        cpu.P.setNZFlags(cpu.Y)
    }
}

/** 0xad, LDA $1234 */
class LdaAbsolute(c: Computer): LdAbsoluteBase(c, LDA_ABS, "LDA") {
    override fun run() {
        cpu.A = memory[word]
        cpu.P.setNZFlags(cpu.A)
    }
}

/** 0xae, LDX $1234 */
class LdXAbsolute(c: Computer): LdAbsoluteBase(c, LDX_ABS, "LDX") {
    override fun run() {
        cpu.X = memory[word]
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xb4, LDY $12,X */
class LdyZpX(c: Computer): ZpBase(c, LDY_ZP_X, "LDY", ",X") {
    override fun run() {
        cpu.Y = memory[operand + cpu.X]
        cpu.P.setNZFlags(cpu.Y)
    }
}

/** 0xb5, LDA $12,X */
class LdaZpX(c: Computer): ZpBase(c, LDA_ZP_X, "LDA", ",X") {
    override fun run() {
        cpu.A = memory[operand + cpu.X]
        cpu.P.setNZFlags(cpu.A)
    }
}

/** 0xb6, LDA $12,Y */
class LdxZpY(c: Computer): ZpBase(c, LDX_ZP_Y, "LDX", ",Y") {
    override fun run() {
        cpu.X = memory[operand + cpu.Y]
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xb9, LDA $12,Y */
class LdaAbsoluteY(c: Computer): ZpBase(c, LDA_ABS_Y, "LDA", ",Y") {
    override val size = 3
    override var timing = 4 // variable
    override fun run() {
        cpu.A = memory[word + cpu.Y]
        timing += pageCrossed(word, word + cpu.Y)
        cpu.P.setNZFlags(cpu.A)
    }
}

/** 0xb8, CLV */
class Clv(c: Computer): FlagInstruction(c, CLV, "CLV") {
    override fun run() { cpu.P.V = false }
}

/** 0xba, TSX */
class Tsx(c: Computer): StackInstruction(c, 0xba, "TSX") {
    override val timing = 2
    override fun run() {
        cpu.X = cpu.SP.S.and(0xff)
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xbc, LDY $1234,X */
class LdyAbsoluteX(c: Computer): InstructionBase(c) {
    override val opCode = LDY_ABS_X
    override val size = 3
    override var timing = 4 // variable
    override fun run() {
        cpu.Y = memory[word + cpu.X]
        timing += pageCrossed(word, word + cpu.X)
        cpu.P.setNZFlags(cpu.A)
    }
    override fun toString(): String = "LDY $${word.hh()},X"
}

/** 0xbd, LDA $1234,X */
class LdaAbsoluteX(c: Computer): InstructionBase(c) {
    override val opCode = LDA_ABS_X
    override val size = 3
    override var timing = 4 // variable
    override fun run() {
        cpu.A = memory[word + cpu.X]
        timing += pageCrossed(word, word + cpu.X)
        cpu.P.setNZFlags(cpu.A)
    }
    override fun toString(): String = "LDA $${word.hh()},X"
}

/** 0xbe, LDX $1234,Y */
class LdxAbsoluteY(c: Computer): InstructionBase(c) {
    override val opCode = LDX_ABS_Y
    override val size = 3
    override var timing = 4 // variable
    override fun run() {
        cpu.X = memory[word + cpu.Y]
        timing += pageCrossed(word, word + cpu.Y)
        cpu.P.setNZFlags(cpu.X)
    }
    override fun toString(): String = "LDX $${word.hh()},Y"
}

/** 0xc8, INY */
class Iny(c: Computer): RegisterInstruction(c, 0xc8, "INY") {
    override fun run() {
        cpu.Y = (cpu.Y + 1).and(0xff)
        cpu.P.setNZFlags(cpu.Y)
    }
}

abstract class RegisterInstruction(c: Computer, override val opCode: Int, val name: String) : InstructionBase(c) {
    override val size = 1
    override val timing = 2
    override fun toString(): String = name
}

/** 0xca, DEX */
class Dex(c: Computer): RegisterInstruction(c, 0xca, "DEX") {
    override fun run() {
        cpu.X = (--cpu.X).and(0xff)
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xd8, CLD */
class Cld(c: Computer): FlagInstruction(c, 0xd8, "CLD") {
    override fun run() { cpu.P.D = false }
}

/** 0xe8, INX */
class Inx(c: Computer): RegisterInstruction(c, 0xe8, "INX") {
    override fun run() {
        cpu.X = (cpu.X + 1).and(0xff)
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xea, NOP */
class Nop(c: Computer): InstructionBase(c) {
    override val opCode = 0xea
    override val size = 1
    override val timing = 2
    override fun run() { }
    override fun toString(): String = "NOP"
}

/** 0xf8, SED */
class Sed(c: Computer): FlagInstruction(c, 0xf8, "SED") {
    override fun run() { cpu.P.D = true }
}

/** Unknown */
class Unknown(c: Computer, override val opCode: Int): InstructionBase(c) {
    override val size = 1
    override val timing = 1
    override fun run() { }
    override fun toString(): String = "???"
}

fun main() {
    val memory = Memory(0xa9, 0x23)
    val computer = Computer(memory = memory)
    computer.cpu.nextInstruction(computer)!!.run()
//    println(computer.cpu)
}
