@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.beust.sixty

import java.util.*

private fun Byte.toHex(): String = String.format("%02x", this.toInt())
private fun UByte.toHex(): String = String.format("%02x", this.toInt())
private fun Int.toHex(): String = String.format("%02x", this)

/**
 * Specs used:
 * https://en.wikipedia.org/wiki/MOS_Technology_6502
 * http://www.6502.org/tutorials/6502opcodes.html
 */
interface ICpu {
    fun nextInstruction(computer: Computer): Instruction
}

interface Instruction {
    /**
     * Number of bytes occupied by this op (1, 2, or 3).
     */
    val size: Int

    /**
     * This should be a property and not a constant since the value of the timing can change for certain ops when
     * a page boundary is crossed.
     */
    val timing: Int

    fun runDebug() {
        println(toString())
        run()
    }

    fun run()
}

class Memory(vararg bytes: Int) {
    private val content: UByteArray = UByteArray(4096)

    init {
        bytes.map { it.toUByte() }.toUByteArray().copyInto(content)
    }

    fun byte(i: UByte) = content[i.toInt()]
    fun byte(i: Int) = content[i]
    fun setByte(i: Int, b1: UByte) {
        println("mem[${i.toHex()}]=${b1.toHex()}")
        if (i == 0x2ff) {
            println("problem")
        }
        content[i] = b1
    }

    override fun toString(): String {
        return content.slice(0..16).map { it.toInt().and(0xff).toHex()}.joinToString(" ")
    }

}

class Computer(val cpu: Cpu = Cpu(), val memory: Memory) {
    fun run() {
        var n = 0
        var done = false
        while (! done) {
            if ((memory.byte(cpu.PC) == 0x60.toUByte() && cpu.SP.isEmpty()) ||
                    memory.byte(cpu.PC) == 0.toUByte()) {
                done = true
            } else {
                val inst = cpu.nextInstruction(this)
                print(cpu.PC.toHex() + ": ")
                // @@
                inst.runDebug()
                cpu.PC += inst.size
                n++
            }
        }
    }

    fun disassemble(pc: Int = cpu.PC) {
        var done = false
        while (! done) {
            val p = memory.byte(cpu.PC)
            if ((p == 0x60.toUByte() && cpu.SP.isEmpty()) ||
                    p == 0.toUByte()) {
                done = true
            }
            val inst = cpu.nextInstruction(this)
            println(cpu.PC.toHex() + ": " + inst.toString())
            cpu.PC += inst.size
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

class StatusFlags {
    private val bits = BitSet(8)

    private fun bit(n: Int) = if (bits.get(n)) 1u else 0u
    private fun bit(n: Int, value: UInt) = bits.set(n, value != 0u)

    var N: UInt // Negative
        get() = bit(7)
        set(v) = bit(7, v)

    var V: UInt // Overflow
        get() = bit(6)
        set(v) = bit(6, v)

    var D: UInt // Decimal
        get() = bit(3)
        set(v) = bit(3, v)

    var I: UInt // Interrupt disable
        get() = bit(2)
        set(v) = bit(2, v)

    var Z: UInt // Zero
        get() = bit(1)
        set(v) = bit(1, v)

    var C: UInt // Carry
        get() = bit(0)
        set(v) = bit(0, v)

    override fun toString() = "{N=$N V=$V D=$D I=$I Z=$Z C=$C}"
}

data class Cpu(var A: UByte = 0u, var X: UByte = 0u, var Y: UByte = 0u, var PC: Int = 0,
        val SP: StackPointer = StackPointer(), val P: StatusFlags = StatusFlags()) : ICpu {
    override fun nextInstruction(computer: Computer): Instruction {
        val op = computer.memory.byte(PC).toInt() and 0xff
        val result = when(op) {
            0x00 -> Brk(computer)
            0x20 -> Jsr(computer)
            0x4c -> Jmp(computer)
            0x60 -> Rts(computer)
            0x85 -> StaZp(computer)
            0x90 -> Bcc(computer)
            0x91 -> StaIndY(computer)
            0xa0 -> LdyImm(computer)
            0xa5 -> LdaZp(computer)
            0xa9 -> LdaImm(computer)
            0xc0 -> CpyImm(computer)
            0xc8 -> Iny(computer)
            0xc9 -> CmpImm(computer)
            0xd0 -> Bne(computer)
            0xe6 -> IncZp(computer)
            0xe8 -> Inx(computer)
            0xea -> Nop(computer)
            else -> TODO("NOT IMPLEMENTED: ${op.toHex()}")
        }

        return result
    }

    override fun toString(): String {
        return "{Cpu A=${A.toHex()} X=${X.toHex()} Y=${Y.toHex()} PC=${PC.toHex()} P=$P SP=$SP}"
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

/** 0x00, BRK */
class Brk(c: Computer): InstructionBase(c) {
    override val size = 1
    override val timing = 7
    override fun run() {}
    override fun toString(): String = "BRK"
}

/** 0x4c, JMP $1234 */
class Jmp(c: Computer): InstructionBase(c) {
    override val size = 3
    override val timing = 3
    override fun run() {
        cpu.PC = word - size
    }

    override fun toString(): String = "JMP $${word.toHex()}"
}

/** 0x20, JSR $1234 */
class Jsr(c: Computer): InstructionBase(c) {
    override val size = 3
    override val timing = 6
    override fun run() {
        cpu.SP.pushWord(pc + size - 1)
        cpu.PC = word - size
    }

    override fun toString(): String = "JSR $${word.toHex()}"
}

fun Byte.unsigned() = java.lang.Byte.toUnsignedInt(this)

abstract class CmpImmBase(c: Computer, val name: String): InstructionBase(c) {
    override val size = 2
    override val timing = 2

    abstract val value: UByte

    override fun run() {
        if (value != b1) {
            cpu.P.Z = 0u
        } else {
            cpu.P.Z = 1u
        }
        if (value < b1) {
            cpu.P.C = 0u
        } else {
            cpu.P.C = 1u
        }
        val sub = value - b1
        cpu.P.N = sub.and(0x80u).shr(7)
    }

    override fun toString(): String = "$name #$${b1.toHex()}"
}

/** 0xc9, CMP $#12 */
class CmpImm(c: Computer): CmpImmBase(c, "CMP") {
    override val value get() = computer.cpu.A
}

/** 0xc0, CPY $#12 */
class CpyImm(c: Computer): CmpImmBase(c, "CPY") {
    override val value get() = computer.cpu.Y
}

/** 0x60, RTS */
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
    override fun toString(): String = "STA $" + memory.byte(cpu.PC + 1).toHex()
}

open class BranchBase(c: Computer, val name: String, val condition: () -> Boolean): InstructionBase(c) {
    override val size = 2
    /** TODO(Varied timing if the branch is taken/not taken and if it crosses a page) */
    override val timing = 2
    override fun run() {
        if (condition()) cpu.PC += b1.toInt()
    }
    override fun toString(): String
            = "$name ${(cpu.PC + size + b1.toInt()).toHex()}"
}

/** 0x90, BCC */
class Bcc(computer: Computer): BranchBase(computer, "BCC", { computer.cpu.P.C == 0u })

/** 0x91, STA ($12),Y */
class StaIndY(c: Computer): InstructionBase(c) {
    override val size = 2
    override val timing = 6
    override fun run() {
        val target = memory.byte(b1.toInt() + 1).toInt().shl(8).or(memory.byte(b1.toInt()).toInt())
        memory.setByte((target.toUInt() + cpu.Y.toUInt()).toInt(), cpu.A)
    }
    override fun toString(): String = "STA ($${b1.toHex()}),Y"
}

/** 0xa5, LDA $10 */
class LdaZp(c: Computer): InstructionBase(c) {
    override val size = 2
    override val timing = 3
    override fun run() {
        cpu.A = memory.byte(b1)
    }
    override fun toString(): String = "LDA $" + b1.toHex()
}

abstract class LdImmBase(c: Computer, val name: String): InstructionBase(c) {
    override val size = 2
    override val timing = 2
    override fun toString(): String = "$name #$" + b1.toHex()
}

/** 0xa0, LDY #$10 */
class LdyImm(c: Computer): LdImmBase(c, "LDY") {
    override fun run() { cpu.Y = b1 }
}

/** 0xa9, LDA #$10 */
class LdaImm(c: Computer): LdImmBase(c, "LDA") {
    override fun run() { cpu.A = b1 }
}

/** 0xc8, INY */
class Iny(c: Computer): InstructionBase(c) {
    override val size = 1
    override val timing = 2
    override fun run() { cpu.Y++ }
    override fun toString(): String = "INY"
}

/** 0xd0, BNE */
class Bne(computer: Computer): BranchBase(computer, "BNE", { computer.cpu.P.Z == 0u })

/** 0xe6, INC $10 */
class IncZp(c: Computer): InstructionBase(c) {
    override val size = 2
    override val timing = 4
    override fun run() { memory.setByte(b1.toInt(), (memory.byte(b1) + 1u).toUByte()) }
    override fun toString(): String = "INC $${b1.toHex()}"
}

/** 0xe8, INX */
class Inx(c: Computer): InstructionBase(c) {
    override val size = 1
    override val timing = 2
    override fun run() { cpu.X++ }
    override fun toString(): String = "INX"
}

/** 0xea, NOP */
class Nop(c: Computer): InstructionBase(c) {
    override val size = 1
    override val timing = 2
    override fun run() { }
    override fun toString(): String = "NOP"
}

fun main() {
    val memory = Memory(0xa9, 0x23)
    val computer = Computer(memory = memory)
    computer.cpu.nextInstruction(computer).run()
    println(computer.cpu)
}
