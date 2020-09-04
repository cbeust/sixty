package com.beust.sixty.op

import com.beust.sixty.*

abstract class FlagInstruction(override val name: String, override val opCode: Int)
        : InstructionBase(name, opCode, 1, 2)

/** 0x18, CLC */
class Clc: FlagInstruction("CLC", CLC) {
    override fun run(c: Computer, op: Operand) { c.cpu.P.C = false }
}

/** 0x38, SEC */
class Sec: FlagInstruction("SEC", SEC) {
    override fun run(c: Computer, op: Operand) { c.cpu.P.C = true }
}

/** 0x58, CLI */
class Cli: FlagInstruction("CLI", CLI) {
    override fun run(c: Computer, op: Operand) { c.cpu.P.I = false }
}

/** 0x78, SEI */
class Sei: FlagInstruction("SEI", SEI) {
    override fun run(c: Computer, op: Operand) { c.cpu.P.I = true }
}

/** 0xb8, CLV */
class Clv: FlagInstruction("CLV", CLV) {
    override fun run(c: Computer, op: Operand) { c.cpu.P.V = false }
}

/** 0xd8, CLD */
class Cld: FlagInstruction("CLD", CLD) {
    override fun run(c: Computer, op: Operand) { c.cpu.P.D = false }
}

/** 0xf8, SED */
class Sed: FlagInstruction("SED", SED) {
    override fun run(c: Computer, op: Operand) { c.cpu.P.D = true }
}

