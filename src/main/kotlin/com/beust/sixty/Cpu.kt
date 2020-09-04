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
            BRK -> Brk()
            PHP -> Php()
            CLC -> Clc()
            JSR -> Jsr()
            ROL_ZP -> RolZp()
            PLP -> Plp()
            ROL -> Rol()
            ROL_ABS -> RolAbsolute()
            SEC -> Sec()
            ROL_ABS_X -> RolAbsoluteX()
            ROL_ZP_X -> RolZpX()
            RTI -> Rti()
            LSR_ZP -> LsrZp()
            LSR -> Lsr()
            JMP -> Jmp()
            PHA -> Pha()
            LSR_ABS -> LsrAbsolute()
            LSR_ABS_X -> LsrAbsoluteX()
            LSR_ZP_X -> LsrZpX()
            CLI -> Cli()
            RTS -> Rts()
            ROR_ZP -> RorZp()
            PLA -> Pla()
            JMP_IND -> JmpIndirect()
            ROR -> Ror()
            ROR_ABS -> RorAbsolute()
            ROR_ZP_X -> RorZpX()
            SEI -> Sei()
            ROR_ABS_X -> RorAbsoluteX()
            DEY -> Dey()
            TXA -> Txa()
            TYA -> Tya()
            TXS -> Txs()
            TAY -> Tay()
            TAX -> Tax()
            CLV -> Clv()
            TSX -> Tsx()
            INY -> Iny()
            DEX -> Dex()
            CLD -> Cld()
            INX -> Inx()
            NOP -> Nop()
            SED -> Sed()

            ASL -> Asl()
            ASL_ZP -> AslZp()
            ASL_ZP_X -> AslZpX()
            ASL_ABS -> AslAbsolute()
            ASL_ABS_X -> AslAbsoluteX()

            BPL -> Bpl()
            BMI -> Bmi()
            BVC -> Bvc()
            BVS -> Bvs()
            BCC -> Bcc()
            BCS -> Bcs()
            BNE -> Bne()
            BEQ -> Beq()

            CMP_IMM -> CmpImmediate()
            CMP_ZP -> CmpZp()
            CMP_ZP_X-> CmpZpX()
            CMP_ABS -> CmpAbsolute()
            CMP_ABS_X -> CmpAbsoluteX()
            CMP_ABS_Y -> CmpAbsoluteY()
            CMP_IND_X -> CmpIndX()
            CMP_IND_Y -> CmpIndY()

            CPX_IMM -> CpxImm()
            CPX_ZP -> CpxZp()
            CPX_ABS -> CpxAbsolute()

            CPY_IMM -> CpyImm()
            CPY_ZP -> CpyZp()
            CPY_ABS -> CpyAbsolute()

            DEC_ZP -> DecZp()
            DEC_ZP_X -> DecZpX()
            DEC_ABS -> DecAbsolute()
            DEC_ABS_X -> DecAbsoluteX()

            INC_ZP -> IncZp()
            INC_ZP_X -> IncZpX()
            INC_ABS -> IncAbsolute()
            INC_ABS_X -> IncAbsoluteX()

            AND_IMM -> AndImmediate()
            AND_ZP -> AndZp()
            AND_ZP_X -> AndZpX()
            AND_ABS -> AndAbsolute()
            AND_ABS_X -> AndAbsoluteX()
            AND_ABS_Y -> AndAbsoluteY()
            AND_IND_X -> AndIndX()
            AND_IND_Y -> AndIndY()

            ADC_IMM -> AdcImmediate()
            ADC_ZP -> AdcZp()
            ADC_ZP_X -> AdcZpX()
            ADC_ABS -> AdcAbsolute()
            ADC_ABS_X -> AdcAbsoluteX()
            ADC_ABS_Y-> AdcAbsoluteY()
            ADC_IND_X -> AdcIndX()
            ADC_IND_Y -> AdcIndY()

            EOR_IMM -> EorImmediate()
            EOR_ZP -> EorZp()
            EOR_ZP_X -> EorZpX()
            EOR_ABS -> EorAbsolute()
            EOR_IND_X -> EorIndX()
            EOR_IND_Y -> EorIndY()
            EOR_ABS_X -> EorAbsoluteX()
            EOR_ABS_Y -> EorAbsoluteY()

            LDA_IMM -> LdaImmediate()
            LDA_ZP -> LdaZp()
            LDA_ZP_X -> LdaZpX()
            LDA_ABS -> LdaAbsolute()
            LDA_ABS_X -> LdaAbsoluteX()
            LDA_ABS_Y -> LdaAbsoluteY()
            LDA_IND_X -> LdaIndX()
            LDA_IND_Y -> LdaIndY()

            LDX_IMM -> LdxImm()
            LDX_ZP -> LdxZp()
            LDX_ZP_Y -> LdxZpY()
            LDX_ABS -> LdxAbsolute()
            LDX_ABS_Y -> LdxAbsoluteY()

            LDY_IMM -> LdyImm()
            LDY_ZP -> LdyZp()
            LDY_ZP_X -> LdyZpX()
            LDY_ABS -> LdyAbsolute()
            LDY_ABS_X -> LdyAbsoluteX()

            ORA_IMM -> OraImmediate()
            ORA_ZP -> OraZp()
            ORA_ZP_X -> OraZpX()
            ORA_ABS -> OraAbsolute()
            ORA_IND_X -> OraIndX()
            ORA_IND_Y -> OraIndY()
            ORA_ABS_X-> OraAbsoluteX()
            ORA_ABS_Y -> OraAbsoluteY()

            SBC_IMM -> SbcImmediate()
            SBC_ZP -> SbcZp()
            SBC_ZP_X -> SbcZpX()
            SBC_ABS -> SbcAbsolute()
            SBC_ABS_X -> SbcAbsoluteX()
            SBC_ABS_Y -> SbcAbsoluteY()
            SBC_IND_X -> SbcIndX()
            SBC_IND_Y -> SbcIndY()

            STA_ZP -> StaZp()
            STA_ZP_X -> StaZpX()
            STA_ABS -> StaAbsolute()
            STA_ABS_X -> StaAbsoluteX()
            STA_ABS_Y -> StaAbsoluteY()
            STA_IND_X -> StaIndX()
            STA_IND_Y -> StaIndY()

            STX_ZP -> StxZp()
            STX_ZP_Y -> StxZpY()
            STX_ABS -> StxAbsolute()

            STY_ZP -> StyZp()
            STY_ZP_X-> StyZpX()
            STY_ABS -> StyAbsolute()

            BIT_ZP -> BitZp()
            BIT_ABS -> BitAbsolute()

            else -> {
                if (noThrows) {
                    Unknown(op)
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

interface Operand {
    fun get(): Int
    fun set(v: Int)
    val name: String
}

abstract class OperandBase(computer: Computer): Operand {
    val cpu by lazy { computer.cpu }
    val memory by lazy { computer.memory }
    val pc by lazy { cpu.PC}
    val operand by lazy { memory[pc + 1] }
    val word by lazy { memory[pc + 2].shl(8).or(memory[pc + 1]) }

    var op: Operand? = null
}

enum class Addressing {
    IMMEDIATE, ZP, ZP_X, ZP_Y, ABSOLUTE, ABSOLUTE_X, ABSOLUTE_Y, INDIRECT_X, INDIRECT_Y, REGISTER_A, NONE;

    fun toOperand(c: Computer): Operand {
        return when(this) {
            IMMEDIATE -> OperandImmediate(c)
            ZP -> OperandZp(c)
            ZP_X -> OperandZpX(c)
            ZP_Y -> OperandZpY(c)
            ABSOLUTE -> OperandAbsolute(c)
            ABSOLUTE_X -> OperandAbsoluteX(c)
            ABSOLUTE_Y -> OperandAbsoluteY(c)
            INDIRECT_X -> OperandIndirectX(c)
            INDIRECT_Y -> OperandIndirectY(c)
            REGISTER_A -> OperandRegisterA(c)
            NONE -> TODO("NONE ADDRESSING")
        }
    }
}

class OperandImmediate(c: Computer): OperandBase(c) {
    override fun get() = operand
    override fun set(v: Int) = TODO("Should never happen")
    override val name get() = " #$${operand.h()}"
}

class OperandAbsolute(c: Computer): OperandBase(c) {
    override fun get() = memory[word]
    override fun set(v: Int) { memory[word] = v }
    override val name get() = " $${word.h()}"
}

class OperandAbsoluteX(c: Computer): OperandBase(c) {
    override fun get() = memory[word + cpu.X]
    override fun set(v: Int) { memory[word + cpu.X] = v }
    override val name get() = " $${word.h()},X"
}

class OperandAbsoluteY(c: Computer): OperandBase(c) {
    override fun get() = memory[word + cpu.Y]
    override fun set(v: Int) { memory[word + cpu.Y] = v }
    override val name get() = " $${word.h()},Y"
}

class OperandIndirectX(c: Computer): OperandBase(c) {
    private val address = memory[(operand + cpu.X) and 0xff].or(memory[(operand + cpu.X + 1) and 0xff].shl(8))

    override fun get() = memory[address]
    override fun set(v: Int) { memory[address] = v }
    override val name get() = " ($${operand.h()},X)"
}

class OperandIndirectY(c: Computer): OperandBase(c) {
    private val address = memory[operand].or(memory[(operand + 1) and 0xff].shl(8))

    override fun get() = memory[address + cpu.Y]
    override fun set(v: Int) { memory[address + cpu.Y] = v }
    override val name get() = " ($${operand.h()}),Y"
}

class OperandZp(c: Computer): OperandBase(c) {
    override fun get() = memory[operand]
    override fun set(v: Int) { memory[operand] = v }
    override val name get() = " $${operand.h()}"
}

class OperandZpX(c: Computer): OperandBase(c) {
    override fun get() = memory[(operand + cpu.X) and 0xff]
    override fun set(v: Int) { memory[(operand + cpu.X) and 0xff] = v }
    override val name get() = " $${operand.h()},X"
}

class OperandZpY(c: Computer): OperandBase(c) {
    override fun get() = memory[(operand + cpu.Y) and 0xff]
    override fun set(v: Int) { memory[(operand + cpu.Y) and 0xff] = v }
    override val name get() = " $${operand.h()},Y"
}

class OperandRegisterA(c: Computer): OperandBase(c) {
    override fun get() = cpu.A
    override fun set(v: Int) { cpu.A = v }
    override val name get() = ""
}

abstract class InstructionBase(override val name: String, override val opCode: Int, override val size: Int,
        override val timing: Int, override val addressing: Addressing = Addressing.NONE) : Instruction
{
    var changedPc: Boolean = false

    override fun run(c: Computer) = run(c, addressing.toOperand(c))
    override fun toString(c: Computer) = toString() + addressing.toOperand(c).name
    override fun toString() = name

    abstract fun run(c: Computer, op: Operand)
}

/** 0x00, BRK */
class Brk: InstructionBase("BRK", BRK, 1, 7) {
    private fun handleInterrupt(c: Computer, brk: Boolean, vectorHigh: Int, vectorLow: Int) {
        with(c) {
            cpu.SP.pushWord(cpu.PC + 2)
            cpu.SP.pushByte(cpu.P.toByte())
            cpu.P.I = true
            cpu.PC = memory[vectorHigh].shl(8).or(memory[vectorLow])
        }
    }

    override fun run(c: Computer, op: Operand) {
        with(c) {
            changedPc = true
            handleInterrupt(c, true, Cpu.IRQ_VECTOR_H, Cpu.IRQ_VECTOR_L)
            cpu.P.B = true
            //        cpu.P.I = false
            cpu.P.reserved = true
        }
    }
}

/** 0x40, RTI */
class Rti: InstructionBase("RTI", RTI, 1, 6) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            changedPc = true
            cpu.P.fromByte(cpu.SP.popByte())
            cpu.PC = cpu.SP.popWord()
        }
    }
    override fun toString(): String = "RTI"
}

/** 0x4c, JMP $1234 */
class Jmp: InstructionBase("JMP", JMP, 3, 3) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            changedPc = true
            cpu.PC = word
        }
    }
}

/** 0x60, RTS */
class Rts: InstructionBase("RTS", RTS, 1, 6) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            changedPc = true
            cpu.PC = cpu.SP.popWord() + 1
        }
    }
}

/** 0x6c, JMP ($0036) */
class JmpIndirect: InstructionBase("JMP", JMP_IND, 3, 5) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            changedPc = true
            cpu.PC = memory.wordAt(word)
        }
    }
}

/** 0xea, NOP */
class Nop: InstructionBase("NOP", NOP, 1, 2) {
    override fun run(c: Computer, op: Operand) { }
}

/** Unknown */
class Unknown(opCode: Int): InstructionBase("???", opCode, 1, 1) {
    override fun run(c: Computer, op: Operand) { }
}
