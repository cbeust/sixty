package com.beust.sixty.op

import com.beust.sixty.*

abstract class StackInstruction(override val name: String, override val opCode: Int,
        override val timing: Int): InstructionBase(name, opCode, 1, timing)

/** 0x20, JSR $1234 */
class Jsr: InstructionBase("JSR", JSR, 3, 6, Addressing.ABSOLUTE) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.SP.pushWord(pc - 1)
            cpu.PC = op.word
        }
    }
}

/** 0x8, PHP */
class Php: StackInstruction("PHP", PHP, 3) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.P.B = true
            cpu.P.reserved = true
            cpu.SP.pushByte(cpu.P.toByte())
        }
    }
}

/** 0x28, PLP */
class Plp: StackInstruction("PLP", PLP, 4) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.P.fromByte(cpu.SP.popByte())
        }
    }
}

/** 0x68, PLA */
class Pla: StackInstruction("PLA", PLA, 4) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.A = cpu.SP.popByte().toInt().and(0xff)
            cpu.P.setNZFlags(cpu.A)
        }
    }
}

/** 0x48, PHA */
class Pha: StackInstruction("PHA", PHA, 3) {
    override fun run(c: Computer, op: Operand) = c.cpu.SP.pushByte(c.cpu.A.toByte())
}

/** 0x9a, TXS */
class Txs: StackInstruction("TXS", TXS, 2) {
    override fun run(c: Computer, op: Operand) {
        c.cpu.SP.S = c.cpu.X
    }
}

/** 0xba, TSX */
class Tsx: StackInstruction("TSX", TSX, 2) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.X = cpu.SP.S.and(0xff)
            cpu.P.setNZFlags(cpu.X)
        }
    }
}

