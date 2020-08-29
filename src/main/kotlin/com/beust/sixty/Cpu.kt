//@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.beust.sixty

import java.io.File
import java.util.*

fun logMem(i: Int, value: Int, extra: String = "") {
    println("mem[${i.h()}] = ${(value.and(0xff)).h()} $extra")
}

/**
 * Specs used:
 * https://en.wikipedia.org/wiki/MOS_Technology_6502
 * http://www.6502.org/tutorials/6502opcodes.html
 */
interface ICpu {
    fun nextInstruction(computer: Computer, noThrows: Boolean = false): Instruction
    fun clone(): Cpu
}

interface Instruction {
    /**
     * Opcode of this instruction
     */
    val opCode: Int

    /**
     * Number of bytes occupied by this op (1, 2, or 3).
     */
    val size: Int

    /**
     * This should be a property and not a constant since the value of the timing can change for certain ops when
     * a page boundary is crossed.
     */
    val timing: Int

    fun run()

    /**
     * @return 1 if a page bounday was crossed, 0 otherwise
     */
    fun pageCrossed(old: Int, new: Int): Int {
        return if (old.and(0x80).xor(new.and(0x80)) != 0) 1 else 0
    }
}

class Memory(val size: Int = 0x10000, vararg bytes: Int) {
    var interceptor: MemoryInterceptor? = null
    var listener: MemoryListener? = null
    val content: IntArray = IntArray(size)

    init {
        bytes.copyInto(content)
    }

    operator fun get(i: Int): Int {
        val result = if (interceptor != null) {
            val response = interceptor!!.onRead(i)
            if (response.allow) response.value
            else content[i]
        } else {
            content[i]
        }

        listener?.onRead(i, result)
        return result.and(0xff)
    }

    operator fun set(i: Int, value: Int) {
        if (interceptor != null) {
            val response = interceptor!!.onWrite(i, value)
            if (response.allow) {
                if (DEBUG_MEMORY) logMem(i, value, "(allowed)")
                content[i] = value
            } else {
                if (DEBUG_MEMORY) logMem(i, value, "(denied)")
            }
        } else {
            if (DEBUG_MEMORY) logMem(i, value)
            content[i] = value
        }
        listener?.onWrite(i, value)
    }

    override fun toString(): String {
        return content.slice(0..16).map { it.and(0xff).h()}.joinToString(" ")
    }

    fun init(i: Int, vararg bytes: Int) {
        var ii = i
        bytes.forEach { b ->
            set(i + ii, b)
            ii++
        }
    }

    fun load(file: String, address: Int) {
//        File(file).readBytes().map { it.toInt() }.toIntArray().copyInto(content, address)
        File(file).readBytes().forEachIndexed { index, v ->
            if (index + address < 0xffff) {
                content[index + address] = v.toInt()
            }
        }
    }

    fun wordAt(word: Int): Int {
        return get(word + 1).shl(8).or(get(word))
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
        return stack.map { it.h()}.joinToString(" ")
    }
}

fun Boolean.int(): Int = if (this) 1 else 0

class StatusFlags {
    private val bits = BitSet(8)

    private fun bit(n: Int) = bits.get(n)
    private fun bit(n: Int, value: Boolean) = bits.set(n, value)

    var N: Boolean // Negative
        get() = bit(7)
        set(v) = bit(7, v)

    var V: Boolean // Overflow
        get() = bit(6)
        set(v) = bit(6, v)

    var D: Boolean // Decimal
        get() = bit(3)
        set(v) = bit(3, v)

    var I: Boolean // Interrupt disable
        get() = bit(2)
        set(v) = bit(2, v)

    var Z: Boolean // Zero
        get() = bit(1)
        set(v) = bit(1, v)

    var C: Boolean // Carry
        get() = bit(0)
        set(v) = bit(0, v)

    override fun toString() = "{N=${N.int()} V=${V.int()} D=${D.int()} I=${I.int()} Z=${Z.int()} C=${C.int()}"

    fun setArithmeticFlags(reg: Int) {
        Z = reg == 0
        N = reg.and(0x80) != 0
    }

}

data class Cpu(var A: Int = 0, var X: Int = 0, var Y: Int = 0, var PC: Int = 0,
        val SP: StackPointer = StackPointer(), val P: StatusFlags = StatusFlags()) : ICpu {
    override fun clone() = Cpu(A, X, Y, PC, SP, P)
    override fun nextInstruction(computer: Computer, noThrows: Boolean): Instruction {
        val op = computer.memory[PC] and 0xff
        val result = when(op) {
            0x00 -> Brk(computer)
            0x20 -> Jsr(computer)
            0x4c -> Jmp(computer)
            0x60 -> Rts(computer)
            0x6c -> JmpIndirect(computer)
            0x69 -> AdcImm(computer)
            0x85 -> StaZp(computer)
            0x8c -> StyAbsolute(computer)
            0x8d -> StaAbsolute(computer)
            0x8e -> StxAbsolute(computer)
            0x90 -> Bcc(computer)
            0x91 -> StaIndY(computer)
            0xa0 -> LdyImm(computer)
            0xa2 -> LdxImm(computer)
            0xa5 -> LdaZp(computer)
            0xa9 -> LdaImm(computer)
            0xc0 -> CpyImm(computer)
            0xc8 -> Iny(computer)
            0xc9 -> CmpImm(computer)
            0xd0 -> Bne(computer)
            0xe6 -> IncZp(computer)
            0xe8 -> Inx(computer)
            0xea -> Nop(computer)
            else -> if (noThrows) Unknown(computer, op) else TODO("NOT IMPLEMENTED: ${PC.h()}: ${op.h()}")
        }

        return result
    }

    override fun toString(): String {
        return "{Cpu A=${A.h()} X=${X.h()} Y=${Y.h()} PC=${PC.h()} P=$P SP=$SP}"
    }
}

abstract class InstructionBase(val computer: Computer): Instruction {
    val cpu by lazy { computer.cpu }
    val memory by lazy { computer.memory }
    val pc by lazy { cpu.PC}
    val operand by lazy { memory[pc + 1] }
    val word by lazy { memory[pc + 2].shl(8).or(memory[pc + 1]) }
}

/** 0x00, BRK */
class Brk(c: Computer): InstructionBase(c) {
    override val opCode = 0
    override val size = 1
    override val timing = 7
    override fun run() {}
    override fun toString(): String = "BRK"
}

/** 0x4c, JMP $1234 */
class Jmp(c: Computer): InstructionBase(c) {
    override val opCode = 0x4c
    override val size = 3
    override val timing = 3
    override fun run() {
        cpu.PC = word - size
    }

    override fun toString(): String = "JMP $${word.h()}"
}

/** 0x20, JSR $1234 */
class Jsr(c: Computer): InstructionBase(c) {
    override val opCode = 0x20
    override val size = 3
    override val timing = 6
    override fun run() {
        cpu.SP.pushWord(pc + size - 1)
        cpu.PC = word - size
    }

    override fun toString(): String = "JSR $${word.h()}"
}

abstract class CmpImmBase(c: Computer, val name: String): InstructionBase(c) {
    override val size = 2
    override val timing = 2

    abstract val register: Int

    override fun run() {
        val tmp: Int = (register - operand) and 0xff
        cpu.P.C = register >= operand
        cpu.P.Z = tmp == 0
        cpu.P.N = (tmp and 0x80) != 0
    }

    override fun toString(): String = "$name #$${operand.h()}"
}

/** 0xc9, CMP $#12 */
class CmpImm(c: Computer): CmpImmBase(c, "CMP") {
    override val opCode = 0xc9
    override val register get() = computer.cpu.A
}

/** 0xc0, CPY $#12 */
class CpyImm(c: Computer): CmpImmBase(c, "CPY") {
    override val opCode = 0xc0
    override val register get() = computer.cpu.Y
}

/** 0x60, RTS */
class Rts(c: Computer): InstructionBase(c) {
    override val opCode = 0x60
    override val size = 1
    override val timing = 6
    override fun run() { computer.cpu.PC = cpu.SP.popWord() }
    override fun toString(): String = "RTS"
}

/** 0x69, ADC #$12 */
class AdcImm(c: Computer): InstructionBase(c) {
    override val opCode = 0x69
    override val size = 2
    override val timing = 2
    override fun run() {
        val value = cpu.A
        var result: Int = operand + value + cpu.P.C.int()
        val carry6: Int = operand.and(0x7f) + value.and(0x7f) + cpu.P.C.int()
        cpu.P.C = result.and(0x100) == 1
        cpu.P.V = cpu.P.C.xor((carry6.and(0x80) != 0))
        result = result and 0xff
        cpu.P.setArithmeticFlags(result)
        cpu.A = result
    }
    override fun toString(): String = "ADC #${operand.h()}"
}

/** 0x6c, JMP ($0036) */
class JmpIndirect(c: Computer): InstructionBase(c) {
    override val opCode = 0x6c
    override val size = 3
    override val timing = 5
    override fun run() { cpu.PC = memory.wordAt(word) -  size }
    override fun toString(): String = "JMP ($${word.h()})"
}

/** 0x85, STA ($10) */
class StaZp(c: Computer): InstructionBase(c) {
    override val opCode = 0x85
    override val size = 2
    override val timing = 3
    override fun run() { memory[operand] = cpu.A }
    override fun toString(): String = "STA $" + memory[cpu.PC + 1].h()
}

/** 0x8c, STY ($1234) */
class StyAbsolute(c: Computer): InstructionBase(c) {
    override val opCode = 0x8c
    override val size = 3
    override val timing = 4
    override fun run() { memory[word] = cpu.Y }
    override fun toString(): String = "STY $${word.h()}"
}

/** 0x8d, STA ($1234) */
class StaAbsolute(c: Computer): InstructionBase(c) {
    override val opCode = 0x8d
    override val size = 3
    override val timing = 4
    override fun run() { memory[word] = cpu.A }
    override fun toString(): String = "STA $${word.h()}"
}

/** 0x8e, STX ($1234) */
class StxAbsolute(c: Computer): InstructionBase(c) {
    override val opCode = 0x8e
    override val size = 3
    override val timing = 4
    override fun run() { memory[word] = cpu.X }
    override fun toString(): String = "STX $${word.h()}"
}

open class BranchBase(c: Computer, opCode: Int, val name: String, val condition: () -> Boolean): InstructionBase(c) {
    override val opCode = opCode
    override val size = 2
    /** TODO(Varied timing if the branch is taken/not taken and if it crosses a page) */
    override var timing = 2
    override fun run() {
        if (condition()) {
            val old = cpu.PC
            cpu.PC += operand.toByte()
            timing++
            timing += pageCrossed(old, cpu.PC)
        }  // needs to be signed here
    }
    override fun toString(): String
            = "$name $${(cpu.PC + size + operand.toByte()).h()}"
}

/** 0x90, BCC */
class Bcc(computer: Computer): BranchBase(computer, 0x90, "BCC", { ! computer.cpu.P.C })

/** 0x91, STA ($12),Y */
class StaIndY(c: Computer): InstructionBase(c) {
    override val opCode = 0x91
    override val size = 2
    override val timing = 6
    override fun run() {
        val target = memory[operand + 1].toInt().shl(8).or(memory[operand].toInt())
        memory[(target.toUInt() + cpu.Y.toUInt()).toInt()] = cpu.A
    }
    override fun toString(): String = "STA ($${operand.toByte().h()}),Y"
}

/** 0xa5, LDA $10 */
class LdaZp(c: Computer): InstructionBase(c) {
    override val opCode = 0xa5
    override val size = 2
    override val timing = 3
    override fun run() {
        cpu.A = memory[operand]
    }
    override fun toString(): String = "LDA $" + operand.h()
}

abstract class LdImmBase(c: Computer, opCode: Int, val name: String): InstructionBase(c) {
    override val opCode = opCode
    override val size = 2
    override val timing = 2
    override fun toString(): String = "$name #$" + operand.h()
}

/** 0xa0, LDY #$10 */
class LdyImm(c: Computer): LdImmBase(c, 0xa0, "LDY") {
    override fun run() { cpu.Y = operand }
}

/** 0xa2, LDX #$10 */
class LdxImm(c: Computer): LdImmBase(c, 0xa2, "LDX") {
    override fun run() { cpu.X = operand }
}

/** 0xa9, LDA #$10 */
class LdaImm(c: Computer): LdImmBase(c, 0xa9, "LDA") {
    override fun run() { cpu.A = operand }
}

abstract class IncBase(c: Computer, opCode: Int): InstructionBase(c) {
    override val opCode = opCode
    protected fun calculate(oldValue: Int): Int {
        val result = (oldValue + 1).and(0xff)
        cpu.P.setArithmeticFlags(result)
        return result
    }
}

/** 0xc8, INY */
class Iny(c: Computer): IncBase(c, 0xc8) {
    override val size = 1
    override val timing = 2
    override fun run() {
        cpu.Y = (cpu.Y + 1).and(0xff)
        cpu.P.setArithmeticFlags(cpu.Y)
    }
    override fun toString(): String = "INY"
}

/** 0xd0, BNE */
class Bne(computer: Computer): BranchBase(computer, 0xd0, "BNE", { ! computer.cpu.P.Z })

/** 0xe6, INC $10 */
class IncZp(c: Computer): IncBase(c, 0xe6) {
    override val size = 2
    override val timing = 4
    override fun run() {
        memory[operand] = calculate(memory[operand])
    }
    override fun toString(): String = "INC $${operand.h()}"
}

/** 0xe8, INX */
class Inx(c: Computer): IncBase(c, 0xe8) {
    override val size = 1
    override val timing = 2
    override fun run() {
        val newValue = cpu.X + 1
        cpu.P.setArithmeticFlags(newValue)
        cpu.X = newValue
    }
    override fun toString(): String = "INX"
}

/** 0xea, NOP */
class Nop(c: Computer): InstructionBase(c) {
    override val opCode = 0xea
    override val size = 1
    override val timing = 2
    override fun run() { }
    override fun toString(): String = "NOP"
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
    computer.cpu.nextInstruction(computer).run()
//    println(computer.cpu)
}
