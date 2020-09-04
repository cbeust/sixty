package com.beust.sixty.op

import com.beust.sixty.*

abstract class AdcSbcBase(override val name: String, override val opCode: Int, override val size: Int,
        override val timing: Int, override val addressing: Addressing)
    : InstructionBase(name, opCode, size, timing, addressing)
{
    fun add(c: Computer, v: Int) = with(c) {
        var result: Int = cpu.A + v + cpu.P.C.int()
        val carry6: Int = cpu.A.and(0x7f) + v.and(0x7f) + cpu.P.C.int()
        cpu.P.C = result.and(0x100) != 0
        cpu.P.V = cpu.P.C.xor((carry6.and(0x80) != 0))
        result = result and 0xff
        cpu.P.setNZFlags(result)
        cpu.A = result
    }
}