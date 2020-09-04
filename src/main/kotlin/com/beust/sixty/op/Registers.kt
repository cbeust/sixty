package com.beust.sixty.op

import com.beust.sixty.*

abstract class RegisterInstruction(override val name: String, override val opCode: Int)
    : InstructionBase(name, opCode, 1, 2)

/** 0x88, INX */
class Dey: RegisterInstruction("DEY", DEY) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.Y = (--cpu.Y).and(0xff)
            cpu.P.setNZFlags(cpu.Y)
        }
    }
}

/** 0x8a, TXA */
class Txa: RegisterInstruction("TXA", TXA) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.A = cpu.X
            cpu.P.setNZFlags(cpu.A)
        }
    }
}

/** 0x98, TYA */
class Tya: RegisterInstruction("TYA", TYA) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.A = cpu.Y
            cpu.P.setNZFlags(cpu.A)
        }
    }
}

/** 0xa8, TAY */
class Tay: RegisterInstruction("TAY", TAY) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.Y = cpu.A
            cpu.P.setNZFlags(cpu.Y)
        }
    }
}




/** 0xaa, TAX */
class Tax: RegisterInstruction("TAX", TAX) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.X = cpu.A
            cpu.P.setNZFlags(cpu.X)
        }
    }
}

/** 0xc8, INY */
class Iny: RegisterInstruction("INY", INY) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.Y = (cpu.Y + 1).and(0xff)
            cpu.P.setNZFlags(cpu.Y)
        }
    }
}

/** 0xca, DEX */
class Dex: RegisterInstruction("DEX", DEX) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.X = (--cpu.X).and(0xff)
            cpu.P.setNZFlags(cpu.X)
        }
    }
}

/** 0xe8, INX */
class Inx: RegisterInstruction("INX", INX) {
    override fun run(c: Computer, op: Operand) {
        with(c) {
            cpu.X = (cpu.X + 1).and(0xff)
            cpu.P.setNZFlags(cpu.X)
        }
    }
}
