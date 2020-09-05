//@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.beust.sixty

import com.beust.app.StackPointer

/**
 * Specs used:
 * https://en.wikipedia.org/wiki/MOS_Technology_6502
 * http://www.6502.org/tutorials/6502opcodes.html
 */

data class Cpu(val memory: Memory,
        var A: Int = 0, var X: Int = 0, var Y: Int = 0, var PC: Int = 0,
        val P: StatusFlags = StatusFlags())
{
    val SP: StackPointer = StackPointer(memory)

    fun nextInstruction(mem: Memory = memory, address: Int = PC, noThrows: Boolean = false): Instruction
        = nextInstruction(mem[address] and 0xff, noThrows)

    override fun toString(): String {
        return "A=${A.h()} X=${X.h()} Y=${Y.h()} S=${SP.S.h()} P=${P.toByte().h()} PC=\$${PC.h()} P=${P} SP=$SP"
    }

    companion object {
        fun nextInstruction(op: Int, noThrows: Boolean = false): Instruction =
                OP_ARRAY[op]
                        ?: Unknown(op)
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
    val byte: Int
    val word: Int
}

abstract class OperandBase(computer: Computer, override val byte: Int, override val word: Int): Operand {
    val cpu by lazy { computer.cpu }
    val memory by lazy { computer.memory }
//    val pc by lazy { cpu.PC}
//    val operand by lazy { memory[pc + 1] }
//    val word by lazy { memory[pc + 2].shl(8).or(memory[pc + 1]) }
//
//    var op: Operand? = null
}

enum class Addressing {
    IMMEDIATE, ZP, ZP_X, ZP_Y, ABSOLUTE, ABSOLUTE_X, ABSOLUTE_Y, INDIRECT_X, INDIRECT_Y, REGISTER_A, INDIRECT,
        RELATIVE, NONE;

    fun toOperand(c: Computer, byte: Int, word: Int): Operand {
        return when(this) {
            IMMEDIATE -> OperandImmediate(c, byte, word)
            ZP -> OperandZp(c, byte, word)
            ZP_X -> OperandZpX(c, byte, word)
            ZP_Y -> OperandZpY(c, byte, word)
            ABSOLUTE -> OperandAbsolute(c, byte, word)
            ABSOLUTE_X -> OperandAbsoluteX(c, byte, word)
            ABSOLUTE_Y -> OperandAbsoluteY(c, byte, word)
            INDIRECT_X -> OperandIndirectX(c, byte, word)
            INDIRECT_Y -> OperandIndirectY(c, byte, word)
            REGISTER_A -> OperandRegisterA(c, byte, word)
            INDIRECT -> OperandIndirect(c, byte, word)
            RELATIVE -> OperandRelative(c, byte, word)
            NONE -> OperandNone(c, byte, word)
        }
    }

    fun toString(pc: Int, byte: Int, word: Int): String {
        return when(this) {
            IMMEDIATE -> " #$${byte.h()}"
            ZP -> " $${byte.h()}"
            ZP_X -> " $${byte.h()},X"
            ZP_Y -> " $${byte.h()},Y"
            ABSOLUTE -> " $${word.hh()}"
            ABSOLUTE_X -> " $${word.hh()},X"
            ABSOLUTE_Y -> " $${word.hh()},Y"
            INDIRECT_X -> " ($${byte.h()},X)"
            INDIRECT_Y -> " ($${byte.h()},Y)"
            REGISTER_A -> ""
            INDIRECT -> " ($${word.hh()})"
            RELATIVE -> " $${(pc + byte.toByte() + 2).hh()}"
            NONE -> ""
        }
    }

}

class OperandNone(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = byte
    override fun set(v: Int) = TODO("Should never happen")
}

class OperandImmediate(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = byte
    override fun set(v: Int) = TODO("Should never happen")
}

class OperandAbsolute(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = memory[word]
    override fun set(v: Int) { memory[word] = v }
}

class OperandAbsoluteX(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = memory[word + cpu.X]
    override fun set(v: Int) { memory[word + cpu.X] = v }
}

class OperandAbsoluteY(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = memory[word + cpu.Y]
    override fun set(v: Int) { memory[word + cpu.Y] = v }
}

class OperandIndirectX(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    private val address = memory[(this.byte + cpu.X) and 0xff].or(memory[(this.byte + cpu.X + 1) and 0xff].shl(8))

    override fun get() = memory[address]
    override fun set(v: Int) { memory[address] = v }
}

class OperandIndirectY(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    private val address = memory[this.byte].or(memory[(this.byte + 1) and 0xff].shl(8))

    override fun get() = memory[address + cpu.Y]
    override fun set(v: Int) { memory[address + cpu.Y] = v }
}

class OperandZp(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = memory[byte]
    override fun set(v: Int) { memory[byte] = v }
}

class OperandZpX(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = memory[(byte + cpu.X) and 0xff]
    override fun set(v: Int) { memory[(byte + cpu.X) and 0xff] = v }
}

class OperandZpY(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = memory[(byte + cpu.Y) and 0xff]
    override fun set(v: Int) { memory[(byte + cpu.Y) and 0xff] = v }
}

class OperandRegisterA(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = cpu.A
    override fun set(v: Int) { cpu.A = v }
}

class OperandIndirect(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    private val address = memory[(this.byte + cpu.X) and 0xff].or(memory[(this.byte + cpu.X + 1) and 0xff].shl(8))

    override fun get() = memory[address]
    override fun set(v: Int) { memory[address] = v }
}

class OperandRelative(val c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = TODO("Should never happen")
    override fun set(v: Int) { TODO("Should never happen") }
}

abstract class InstructionBase(override val name: String, override val opCode: Int, override val size: Int,
        override val timing: Int, override val addressing: Addressing = Addressing.NONE) : Instruction
{
    override fun run(c: Computer, byte: Int, word: Int) = run(c, addressing.toOperand(c, byte, word))
    override fun toString(c: Computer, byte: Int, word: Int) = toString(c.cpu.PC, byte, word)
    override fun toString(pc: Int, byte: Int, word: Int) = toString() + addressing.toString(pc, byte, word)
    override fun toString() = name

    abstract fun run(c: Computer, op: Operand)
}

/** 0x00, BRK */
class Brk: InstructionBase("BRK", BRK, 1, 7) {
    private fun handleInterrupt(c: Computer, brk: Boolean, vectorHigh: Int, vectorLow: Int) {
        with(c) {
            cpu.P.B = brk
            cpu.SP.pushWord(cpu.PC + 1)
            cpu.SP.pushByte(cpu.P.toByte())
            cpu.P.I = true
            cpu.PC = memory[vectorHigh].shl(8).or(memory[vectorLow])
        }
    }

    override fun run(c: Computer, op: Operand) {
        with(c) {
            handleInterrupt(c, true, Cpu.IRQ_VECTOR_H, Cpu.IRQ_VECTOR_L)
//            cpu.P.I = false
        }
    }
}

/** 0x40, RTI */
class Rti: InstructionBase("RTI", RTI, 1, 6) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.P.fromByte(cpu.SP.popByte())
            cpu.PC = cpu.SP.popWord()
        }
    }
    override fun toString(): String = "RTI"
}

/** 0x4c, JMP $1234 */
class Jmp: InstructionBase("JMP", JMP, 3, 3, Addressing.ABSOLUTE) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.PC = op.word
        }
    }
}

/** 0x60, RTS */
class Rts: InstructionBase("RTS", RTS, 1, 6) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.PC = cpu.SP.popWord() + 1
        }
    }
}

/** 0x6c, JMP ($0036) */
class JmpIndirect: InstructionBase("JMP", JMP_IND, 3, 5, Addressing.INDIRECT) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.PC = memory.wordAt(op.word)
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
