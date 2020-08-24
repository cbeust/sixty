package com.beust.sixty

import java.util.*

private fun Byte.toHex(): String = String.format("%02x", this.toInt())
private fun Int.toHex(): String = String.format("%02x", this)

/**
 * Specs used:
 * https://en.wikipedia.org/wiki/MOS_Technology_6502
 */
interface ICpu {
    fun nextInstruction(computer: Computer): Instruction
}

interface Instruction {
    val size: Int
    val timing: Int
    fun runBase() {
        println(toString())
        run()
    }
    fun run()
}

class Memory(vararg bytes: Int) {
    private val content: ByteArray = ByteArray(4096)

    init {
        bytes.map { it.toByte() }.toByteArray().copyInto(content)
    }

    fun byte(i: Int) = content[i]
    fun setByte(i: Int, b1: Byte) { content[i] = b1 }

    override fun toString(): String {
        return content.slice(0..16).map { it.toInt().and(0xff).toHex()}.joinToString(" ")
    }

}

class Computer(val cpu: Cpu = Cpu(), val memory: Memory) {
    fun run() {
        var done = false
        while (! done) {
            if ((memory.byte(cpu.PC) == 0x60.toByte() && cpu.SP.isEmpty()) ||
                    memory.byte(cpu.PC) == 0.toByte()) {
                done = true
            } else {
                val inst = cpu.nextInstruction(this)
                print(cpu.PC.toHex() + ": ")
                inst.runBase()
                cpu.PC += inst.size
            }
        }
    }

    override fun toString(): String {
        return "{Computer cpu:$cpu}\n$memory"
    }

}

class StackPointer {
    private val stack = Stack<Byte>()
    fun pushByte(a: Byte) = stack.push(a)
    fun popByte() = stack.pop()
    fun pushWord(a: Int) {
        pushByte(a.toByte())
        pushByte(a.shr(8).toByte())
    }
    fun popWord(): Int = popByte().toInt().shl(8).or(popByte().toInt())
//    fun peek(): Byte = stack.peek()
    fun isEmpty() = stack.isEmpty()
    override fun toString(): String {
        return stack.map { it.toHex()}.joinToString(" ")
    }
}

data class Cpu(var A: Byte = 0, var X: Byte = 0, var Y: Byte = 0, var PC: Int = 0,
        val SP: StackPointer = StackPointer()) : ICpu {
    override fun nextInstruction(computer: Computer): Instruction {
        val op = computer.memory.byte(PC).toInt() and 0xff
        val result = when(op) {
            0x00 -> Brk(computer)
            0x20 -> Jsr(computer)
            0xff -> LdyImm(computer)
            0x60 -> Rts(computer)
            0x85 -> StaZp(computer)
            0x91 -> StaIndY(computer)
            0xa9 -> LdaImm(computer)
            0xe8 -> Inx(computer)
            else -> TODO("NOT IMPLEMENTED: ${op.toHex()}")
        }

        return result
    }

    override fun toString(): String {
        return "{Cpu A=${A.toHex()} X=${X.toHex()} Y=${Y.toHex()} PC=${PC.toHex()} SP=$SP}"
    }
}

abstract class InstructionBase(val computer: Computer): Instruction {
    val cpu by lazy { computer.cpu }
    val memory by lazy { computer.memory }
    val pc  by lazy { cpu.PC}
//    val pc1 by lazy { cpu.PC + 1}
//    val pc2 by lazy { cpu.PC + 2}
    val b1 by lazy { memory.byte(cpu.PC + 1) }
//    val b2 by lazy { memory.byte(cpu.PC + 2) }
    val word by lazy { memory.byte(cpu.PC + 2).toInt().shl(8).or(memory.byte(cpu.PC + 1).toInt()) }
}

/** 0x00 */
class Brk(c: Computer): InstructionBase(c) {
    override val size = 1
    override val timing = 7
    override fun run() {}
    override fun toString(): String = "BRK"
}

/** 0x20 */
class Jsr(c: Computer): InstructionBase(c) {
    override val size = 3
    override val timing = 6
    override fun run() {
        cpu.SP.pushWord(pc + size - 1)
        cpu.PC = word - size
    }

    override fun toString(): String = "JSR $${word.toHex()}"
}

/** 0x44 */
class LdyImm(c: Computer): InstructionBase(c) {
    override val size = 2
    override val timing = 2
    override fun run() { cpu.Y = memory.byte(cpu.PC + 1) }
    override fun toString(): String = "LDY #$" + memory.byte(cpu.PC + 1).toHex()
}

/** 0x60 */
class Rts(c: Computer): InstructionBase(c) {
    override val size = 1
    override val timing = 6
    override fun run() { computer.cpu.PC = cpu.SP.popWord() }
    override fun toString(): String = "RTS"
}

/** 0x85, STA ($10) */
class StaZp(c: Computer): InstructionBase(c) {
    override val size = 2
    override val timing = 3
    override fun run() { memory.setByte(b1.toInt(), cpu.A) }
    override fun toString(): String = "LDA #$" + memory.byte(cpu.PC + 1).toHex()
}

/** 0x91, STA ($12),Y */
class StaIndY(c: Computer): InstructionBase(c) {
    override val size = 2
    override val timing = 6
    override fun run() {
        val target = memory.byte(word + 1).toInt().shl(8).or(memory.byte(word).toInt())
        memory.setByte(target + cpu.Y, cpu.A)
    }
    override fun toString(): String = "STA ($${word.toHex()}), Y"
}

/** 0xa9, LDA #$10 */
class LdaImm(c: Computer): InstructionBase(c) {
    override val size = 2
    override val timing = 2
    override fun run() { cpu.A = memory.byte(cpu.PC + 1) }
    override fun toString(): String = "LDA #$" + computer.memory.byte(computer.cpu.PC + 1).toHex()
}

/** 0xe8, INX */
class Inx(c: Computer): InstructionBase(c) {
    override val size = 1
    override val timing = 2
    override fun run() { cpu.X++ }
    override fun toString(): String = "INX"
}

fun main() {
    val memory = Memory(0xa9, 0x23)
    val computer = Computer(memory = memory)
    computer.cpu.nextInstruction(computer).run()
    println(computer.cpu)
}
