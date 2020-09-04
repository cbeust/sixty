//@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.beust.sixty

import com.beust.app.StackPointer

/**
 * Specs used:
 * https://en.wikipedia.org/wiki/MOS_Technology_6502
 * http://www.6502.org/tutorials/6502opcodes.html
 */

data class Cpu(var A: Int = 0, var X: Int = 0, var Y: Int = 0, var PC: Int = 0xffff,
        val memory: Memory, val P: StatusFlags = StatusFlags())
{
    val SP: StackPointer = StackPointer(memory)
    val OP_ARRAY = Array<Instruction?>(0x100) { _ -> null }

    init {
        OPCODES.forEach {
            OP_ARRAY[it.opCode] = it
        }
    }

    fun clone() = Cpu(A, X, Y, PC, memory.clone(), P)

    fun nextInstruction(computer: Computer, noThrows: Boolean = false): Instruction {
        val op = computer.memory[PC] and 0xff
        return OP_ARRAY[op] ?: Unknown(op)
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
    IMMEDIATE, ZP, ZP_X, ZP_Y, ABSOLUTE, ABSOLUTE_X, ABSOLUTE_Y, INDIRECT_X, INDIRECT_Y, REGISTER_A, NONE;

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
            NONE -> OperandNone(c, byte, word)
        }
    }
}

class OperandNone(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = byte
    override fun set(v: Int) = TODO("Should never happen")
    override val name get() = ""
}

class OperandImmediate(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = byte
    override fun set(v: Int) = TODO("Should never happen")
    override val name get() = " #$${byte.h()}"
}

class OperandAbsolute(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = memory[word]
    override fun set(v: Int) { memory[word] = v }
    override val name get() = " $${word.h()}"
}

class OperandAbsoluteX(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = memory[word + cpu.X]
    override fun set(v: Int) { memory[word + cpu.X] = v }
    override val name get() = " $${word.h()},X"
}

class OperandAbsoluteY(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = memory[word + cpu.Y]
    override fun set(v: Int) { memory[word + cpu.Y] = v }
    override val name get() = " $${word.h()},Y"
}

class OperandIndirectX(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    private val address = memory[(this.byte + cpu.X) and 0xff].or(memory[(this.byte + cpu.X + 1) and 0xff].shl(8))

    override fun get() = memory[address]
    override fun set(v: Int) { memory[address] = v }
    override val name get() = " ($${byte.h()},X)"
}

class OperandIndirectY(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    private val address = memory[this.byte].or(memory[(this.byte + 1) and 0xff].shl(8))

    override fun get() = memory[address + cpu.Y]
    override fun set(v: Int) { memory[address + cpu.Y] = v }
    override val name get() = " ($${byte.h()}),Y"
}

class OperandZp(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = memory[byte]
    override fun set(v: Int) { memory[byte] = v }
    override val name get() = " $${byte.h()}"
}

class OperandZpX(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = memory[(byte + cpu.X) and 0xff]
    override fun set(v: Int) { memory[(byte + cpu.X) and 0xff] = v }
    override val name get() = " $${byte.h()},X"
}

class OperandZpY(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = memory[(byte + cpu.Y) and 0xff]
    override fun set(v: Int) { memory[(byte + cpu.Y) and 0xff] = v }
    override val name get() = " $${byte.h()},Y"
}

class OperandRegisterA(c: Computer, byte: Int, word: Int): OperandBase(c, byte, word) {
    override fun get() = cpu.A
    override fun set(v: Int) { cpu.A = v }
    override val name get() = ""
}

abstract class InstructionBase(override val name: String, override val opCode: Int, override val size: Int,
        override val timing: Int, override val addressing: Addressing = Addressing.NONE) : Instruction
{
    override fun run(c: Computer, byte: Int, word: Int) = run(c, addressing.toOperand(c, byte, word))
    override fun toString(c: Computer, byte: Int, word: Int)
            = toString() + addressing.toOperand(c, byte, word).name
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
class JmpIndirect: InstructionBase("JMP", JMP_IND, 3, 5) {
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
