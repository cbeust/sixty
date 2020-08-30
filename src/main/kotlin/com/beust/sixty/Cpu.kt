//@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.beust.sixty

import java.util.*

fun logMem(i: Int, value: Int, extra: String = "") {
    println("mem[${i.hh()}] = ${(value.and(0xff)).h()} $extra")
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

interface IStackPointer {
    val S: Int // The S register. Actually a byte
    fun pushByte(a: Byte)
    fun popByte(): Byte
    fun pushWord(a: Int)
    fun popWord(): Int
    fun isEmpty(): Boolean

    fun peekByte(): Byte {
        val result = popByte()
        pushByte(result)
        return result
    }

    fun peekWord(): Int {
        val result = popWord()
        pushWord(result)
        return result
    }
}

class InMemoryStackPointer : IStackPointer {
    private val stack = Stack<Byte>()

    override val S = stack.size
    override fun pushByte(a: Byte) { stack.push(a) }
    override fun popByte() = stack.pop()
    override fun pushWord(a: Int) {
        pushByte(a.toByte())
        pushByte(a.shr(8).toByte())
    }
    override fun popWord(): Int = popByte().toInt().shl(8).or(popByte().toInt())
    override fun isEmpty() = stack.isEmpty()
    override fun toString(): String {
        return stack.map { it.h()}.joinToString(" ")
    }
}

data class Cpu(var A: Int = 0, var X: Int = 0, var Y: Int = 0, var PC: Int = 0,
        val SP: IStackPointer = InMemoryStackPointer(), val P: StatusFlags = StatusFlags()) : ICpu {
    override fun clone() = Cpu(A, X, Y, PC, SP, P)
    override fun nextInstruction(computer: Computer, noThrows: Boolean): Instruction {
        val op = computer.memory[PC] and 0xff
        val result = when(op) {
            0x00 -> Brk(computer)
//            0x01 -> OraIndirectX(computer)
//            0x05 -> OraZp(computer)
//            0x06 -> AslZp(computer)
//            0x08 -> Php(computer)
//            0x09 -> OraImm(computer)
            0x0a -> Asl(computer)
//            0x0d -> OraAbsolute(computer)
//            0x0e -> AslAbsolute(computer)
            0x10 -> Bpl(computer)
//            0x11 -> OraIndirectY(computer)
//            0x15 -> OraZpX(computer)
//            0x1e -> AslAbsoluteX(computer)
//            0x16 -> AslZpX(computer)
//            0x18 -> Clc(computer)
//            0x19 -> OraAbsoluteY(computer)
//            0x1d -> OraAbsoluteX(computer)
            0x20 -> Jsr(computer)
//            0x21 -> AndIndirectX(computer)
//            0x24 -> BitZp(computer)
//            0x25 -> AndZp(computer)
//            0x26 -> RolZp(computer)
//            0x28 -> Plp(computer)
//            0x29 -> And(computer)
//            0x2a -> Rol(computer)
//            0x2c -> BitAbsolute(computer)
//            0x2d -> AndAbsolute(computer)
//            0x2e -> RolAbsolute(computer)
            0x30 -> Bmi(computer)
//            0x31 -> AndIndirectY(computer)
//            0x3d -> AndAbsX(computer)
//            0x35 -> AndZpX(computer)
//            0x36 -> RolZpX(computer)
//            0x38 -> Clc(computer)
//            0x39 -> AndAboluteY(computer)
//            0x34 -> RolAbsoluteX(computer)
//            0x40 -> Rti(computer)
//            0x41 -> EorIndirectX(computer)
//            0x45 -> EorZp(computer)
//            0x46 -> LsrZp(computer)
//            0x49 -> EorImm(computer)
//            0x4a -> Lsr(computer)
            0x4c -> Jmp(computer)
            0x48 -> Pha(computer)
//            0x4d -> EorAbsolute(computer)
//            0x4e -> LsrAbsolute(computer)
//            0x50 -> Bvc(computer)
//            0x51 -> EorIndirectY(computer)
//            0x55 -> EorZpX(computer)
//            0x65 -> LsrZpX(computer)
//            0x58 -> Cli(computer)
//            0x59 -> EorAbsY(computer)
//            0x5d -> EorAbsX(computer)
//            0x5e -> LsrAbsoluteX(computer)
            0x60 -> Rts(computer)
//            0x61 -> AdcIndirectX(computer)
//            0x65 -> AdcZp(computer)
            0x68 -> Pla(computer)
            0x69 -> AdcImm(computer)
            0x6c -> JmpIndirect(computer)
//            0x65 -> AdcAbsolute(computer)
//            0x66 -> RorZp(computer)
//            0x6a -> Ror(computer)
//            0x6e -> RorAbsolute(computer)
//            0x70 -> Bvs(computer)
//            0x71 -> AdcIndirectY(computer)
//            0x??75 -> AdcZpX(computer)
//            0x??75 -> AdcAbsoluteX(computer)
//            0x76 -> RorZpX(computer)
//            0x78 -> Sei(computer)
//            0x79 -> AdcAbsoluteY(computer)
//            0x74 -> RorAbsoluteX(computer)
//            0x81 -> StaIndirectX(computer)
//            0x84 -> StyZp(computer)
            0x85 -> StaZp(computer)
//            0x86 -> StxZp(computer)
            0x88 -> Dey(computer)
            0x8c -> StyAbsolute(computer)
            0x8d -> StaAbsolute(computer)
            0x8e -> StxAbsolute(computer)
            0x8a -> Txa(computer)
            0x90 -> Bcc(computer)
            0x91 -> StaIndirectY(computer)
//            0x94 -> StyZpX(computer)
            0x95 -> StaZpX(computer)
//            0x96 -> StxZpY(computer)
            0x98 -> Tya(computer)
//            0x99 -> StaAbsoluteY(computer)
//            0x9a -> Txs(computer)
//            0x9d -> StaAbsoluteX(computer)
            0xa0 -> LdyImm(computer)
//            0xa1 -> LdaIndirectX(computer)
            0xa2 -> LdxImm(computer)
//            0xa4 -> LdyZp(computer)
            0xa5 -> LdaZp(computer)
//            0xa6 -> LdxZp(computer)
            0xa8 -> Tay(computer)
            0xa9 -> LdaImm(computer)
            0xaa -> Tax(computer)
//            0xac -> LdyAbsolute(computer)
            0xad -> LdaAbsolute(computer)
//            0xae -> LdxAbsolute(computer)
//            0xb0 -> Bcs(computer)
//            0xb1 -> LdaIndirectY(computer)
//            0xb4 -> LdyZpX(computer)
//            0xb5 -> LdaZpX(computer)
//            0xb6 -> LdxZpY(computer)
//            0xb8 -> Clv(computer)
//            0xb9 -> LdaAbsoluteY(computer)
            0xba -> Tsx(computer)
//            0xbc -> LdyAbsoluteX(computer)
            0xbd -> LdaAbsoluteX(computer)
//            0xbe -> LdxAbsoluteY(computer)
            0xc0 -> CpyImm(computer)
//            0xc1 -> CmpIndirectX(computer)
//            0xc4 -> CpyZp(computer)
//            0xc5 -> CmpZp(computer)
//            0xc6 -> DecZp(computer)
            0xc8 -> Iny(computer)
            0xc9 -> CmpImm(computer)
//            0xcd -> CmpAbsolute(computer)
            0xca -> Dex(computer)
//            0xcc -> CpyAbsolute(computer)
//            0xce -> DecAbsolute(computer)
            0xd0 -> Bne(computer)
//            0xd1 -> CmpIndirectY(computer)
//            0xd5 -> CmpZpX(computer)
//            0xd6 -> DecZpX(computer)
//            0xd8 -> Cld(computer)
//            0xd9 -> CmpAbsY(computer)
//            0xdd -> CmpAbsX(computer)
//            0xde -> DecAbsX(computer)
//            0xe0 -> CpxImm(computer)
//            0xe1 -> SbcIndirectX(computer)
//            0xe4 -> CpxZp(computer)
//            0xe5 -> SbcZp(computer)
            0xe6 -> IncZp(computer)
            0xe8 -> Inx(computer)
//            0xe9 -> SbcImmediate(computer)
            0xea -> Nop(computer)
//            0xec -> CpxAbsolute(computer)
//            0xed -> SbcAbsolute(computer)
//            0xee -> IncAbsolute(computer)
//            0xf0 -> Beq(computer)
//            0xf1 -> SbcIndirectY(computer)
//            0xf5 -> SbcZpX(computer)
//            0xf7 -> IncZpX(computer)
//            0xf8 -> Sed(computer)
//            0xf9 -> SbcAbsoluteY(computer)
//            0xfd -> SbcAbsoluteX(computer)
//            0xfe -> IncAbsoluteX(computer)
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

/** 0x0a, ASL */
class Asl(c: Computer): InstructionBase(c) {
    override val opCode = 0xa
    override val size = 1
    override val timing = 2
    override fun run() {
        cpu.P.C = if (cpu.A.and(0x80) != 0) true else false
        val newValue = cpu.A.shl(1).and(0xff)
        cpu.P.setArithmeticFlags(newValue)
        cpu.A = newValue
    }
    override fun toString(): String = "ASL"
}

/** 0x10, BPL */
class Bpl(computer: Computer): BranchBase(computer, 0x10, "BPL", { ! computer.cpu.P.N })

/** 0x4c, JMP $1234 */
class Jmp(c: Computer): InstructionBase(c) {
    override val opCode = 0x4c
    override val size = 3
    override val timing = 3
    override fun run() {
        cpu.PC = word - size
    }

    override fun toString(): String = "JMP $${word.hh()}"
}

abstract class StackInstruction(c: Computer, override val opCode: Int, val name: String): InstructionBase(c) {
    override val size = 1
    override fun toString(): String = name
}

/** 0x48, PHA */
class Pha(c: Computer): StackInstruction(c, 0x48, "PHA") {
    override val timing = 3
    override fun run() {
        cpu.SP.pushByte(cpu.A.toByte())
    }
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

    override fun toString(): String = "JSR $${word.hh()}"
}

/** 0x30, BMI */
class Bmi(computer: Computer): BranchBase(computer, 0x30, "BMI", { computer.cpu.P.N })

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
    override fun run() {
        computer.cpu.PC = cpu.SP.popWord()
    }
    override fun toString(): String = "RTS"
}

/** 0x68, PLA */
class Pla(c: Computer): StackInstruction(c, 0x68, "PLA") {
    override val timing = 4
    override fun run() {
        cpu.A = cpu.SP.popByte().toInt()
    }
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
    override fun toString(): String = "JMP ($${word.hh()})"
}

/** 0x85, STA ($10) */
class StaZp(c: Computer): InstructionBase(c) {
    override val opCode = 0x85
    override val size = 2
    override val timing = 3
    override fun run() { memory[operand] = cpu.A }
    override fun toString(): String = "STA $" + memory[cpu.PC + 1].h()
}

/** 0x88, INX */
class Dey(c: Computer): RegisterInstruction(c, 0x88, "DEY") {
    override fun run() {
        cpu.Y--
        cpu.P.setArithmeticFlags(cpu.Y)
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
        cpu.P.setArithmeticFlags(cpu.A)
    }
}

open class BranchBase(c: Computer, override val opCode: Int, val name: String, val condition: () -> Boolean)
    : InstructionBase(c)
{
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
class StaIndirectY(c: Computer): InstructionBase(c) {
    override val opCode = 0x91
    override val size = 2
    override val timing = 6
    override fun run() {
        val target = memory[operand + 1].shl(8).or(memory[operand])
        memory[target + cpu.Y] = cpu.A
    }
    override fun toString(): String = "STA ($${operand.toByte().h()}),Y"
}

/** 0x95, STA $12,X */
class StaZpX(c: Computer): InstructionBase(c) {
    override val opCode = 0x95
    override val size = 2
    override val timing = 4
    override fun run() {
        memory[operand + cpu.X] = cpu.A
    }
    override fun toString(): String = "STA $${operand.h()},X"
}

/** 0x98, TYA */
class Tya(c: Computer): RegisterInstruction(c, 0x98, "TYA") {
    override fun run() {
        cpu.A = cpu.Y
        cpu.P.setArithmeticFlags(cpu.A)
    }
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

/** 0xa8, TAY */
class Tay(c: Computer): RegisterInstruction(c, 0xa8, "TAY") {
    override fun run() {
        cpu.Y = cpu.A
        cpu.P.setArithmeticFlags(cpu.Y)
    }
}

abstract class LdImmBase(c: Computer, override val opCode: Int, val name: String): InstructionBase(c) {
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

/** 0xaa, TAX */
class Tax(c: Computer): RegisterInstruction(c, 0xaa, "TAX") {
    override fun run() {
        cpu.X = cpu.A
        cpu.P.setArithmeticFlags(cpu.X)
    }
}

/** 0xad, LDA $1234 */
class LdaAbsolute(c: Computer): InstructionBase(c) {
    override val opCode = 0xad
    override val size = 3
    override val timing = 4
    override fun run() {
        cpu.A = memory[word]
    }
    override fun toString(): String = "LDA $${word.hh()}"
}


/** 0xba, TSX */
class Tsx(c: Computer): InstructionBase(c) {
    override val opCode = 0xba
    override val size = 1
    override val timing = 2
    override fun run() { cpu.X = cpu.SP.S }
    override fun toString(): String = "TSX"
}

/** 0xbd, LDA $1234,X */
class LdaAbsoluteX(c: Computer): InstructionBase(c) {
    override val opCode = 0xbd
    override val size = 3
    override var timing = 4 // variable
    override fun run() {
        cpu.A = memory[word + cpu.X]
        timing += pageCrossed(word, word + cpu.X)
    }
    override fun toString(): String = "LDA $${word.hh()},X"
}

abstract class IncBase(c: Computer, override val opCode: Int): InstructionBase(c) {
    protected fun calculate(oldValue: Int): Int {
        val result = (oldValue + 1).and(0xff)
        cpu.P.setArithmeticFlags(result)
        return result
    }
}

/** 0xc8, INY */
class Iny(c: Computer): RegisterInstruction(c, 0xc8, "INY") {
    override fun run() {
        cpu.Y = (cpu.Y + 1).and(0xff)
        cpu.P.setArithmeticFlags(cpu.Y)
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
        cpu.X--
        cpu.P.setArithmeticFlags(cpu.X)
    }
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
class Inx(c: Computer): RegisterInstruction(c, 0xe8, "INX") {
    override fun run() {
        cpu.X = (cpu.X + 1).and(0xff)
        cpu.P.setArithmeticFlags(cpu.X)
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
