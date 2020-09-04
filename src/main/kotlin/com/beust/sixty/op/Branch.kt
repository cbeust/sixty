package com.beust.sixty.op

import com.beust.sixty.*

abstract class BranchBase(override val name: String, override val opCode: Int)
    : InstructionBase(name, opCode, 2, 2)
{
    abstract fun condition(c: Computer): Boolean

    /** TODO(Varied timing if the branch is taken/not taken and if it crosses a page) */
    override var timing = 2

    override fun run(c: Computer, op: Operand) = with(c) {
        if (condition(c)) {
            changedPc = true
            val old = cpu.PC
            cpu.PC += operand.toByte() + size
            timing++
            timing += pageCrossed(old, cpu.PC)
        }  // needs to be signed here
    }

    override fun toString(c: Computer): String = with(c) {
        "$name $${(cpu.PC).h()}"
    }
}

/** 0xd0, BNE, zero clear */
class Bne: BranchBase("BNE", BNE) {
    override fun condition(c: Computer) = !c.cpu.P.Z
}

/** 0xf0, BEQ, zero set */
class Beq: BranchBase("BEQ", BEQ) {
    override fun condition(c: Computer) = c.cpu.P.Z
}

/** 0x90, BCC */
class Bcc: BranchBase("BCC", BCC) {
    override fun condition(c: Computer) = !c.cpu.P.C
}

/** 0xb0, BCS */
class Bcs: BranchBase("BCS", BCS) {
    override fun condition(c: Computer) = c.cpu.P.C
}

/** 0x10, BPL */
class Bpl: BranchBase("BPL", BPL) {
    override fun condition(c: Computer) = !c.cpu.P.N
}

/** 0x30, BMI */
class Bmi: BranchBase("BMI", BMI) {
    override fun condition(c: Computer) = c.cpu.P.N
}

/** 0x50, BVC */
class Bvc: BranchBase("BVC", BVC) {
    override fun condition(c: Computer) = !c.cpu.P.V
}

/** 0x70, BVS */
class Bvs: BranchBase("BVS", BCS) {
    override fun condition(c: Computer) = c.cpu.P.V
}
