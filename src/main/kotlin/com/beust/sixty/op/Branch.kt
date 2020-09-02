package com.beust.sixty.op

import com.beust.sixty.BVS
import com.beust.sixty.Computer
import com.beust.sixty.InstructionBase
import com.beust.sixty.h

open class BranchBase(c: Computer, override val opCode: Int, val name: String, val condition: () -> Boolean)
    : InstructionBase(c)
{
    override val size = 2
    /** TODO(Varied timing if the branch is taken/not taken and if it crosses a page) */
    override var timing = 2
    override fun run() {
        if (condition()) {
            changedPc = true
            val old = cpu.PC
            cpu.PC += operand.toByte() + size
            timing++
            timing += pageCrossed(old, cpu.PC)
        }  // needs to be signed here
    }
    override fun toString(): String
            = "$name $${(cpu.PC + size + operand.toByte()).h()}"
}

/** 0xd0, BNE, zero clear */
class Bne(computer: Computer): BranchBase(computer, 0xd0, "BNE", { !computer.cpu.P.Z })

/** 0xf0, BEQ, zero set */
class Beq(computer: Computer): BranchBase(computer, 0xf0, "BEQ", { computer.cpu.P.Z })

/** 0x90, BCC */
class Bcc(computer: Computer): BranchBase(computer, 0x90, "BCC", { !computer.cpu.P.C })

/** 0xb0, BCS */
class Bcs(computer: Computer): BranchBase(computer, 0xb0, "BCS", { computer.cpu.P.C })

/** 0x10, BPL */
class Bpl(computer: Computer): BranchBase(computer, 0x10, "BPL", { !computer.cpu.P.N })

/** 0x30, BMI */
class Bmi(computer: Computer): BranchBase(computer, 0x30, "BMI", { computer.cpu.P.N })

/** 0x50, BVC */
class Bvc(computer: Computer): BranchBase(computer, 0x50, "BVC", { !computer.cpu.P.V })

/** 0x70, BVS */
class Bvs(computer: Computer): BranchBase(computer, BVS, "BVS", { computer.cpu.P.V })
