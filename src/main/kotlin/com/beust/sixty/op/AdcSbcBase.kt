package com.beust.sixty.op

import com.beust.sixty.Computer
import com.beust.sixty.InstructionBase
import com.beust.sixty.int

abstract class AdcSbcBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : InstructionBase(c)
{
    fun add(v: Int) {
        var result: Int = cpu.A + v + cpu.P.C.int()
        val carry6: Int = cpu.A.and(0x7f) + v.and(0x7f) + cpu.P.C.int()
        cpu.P.C = result.and(0x100) == 1
        cpu.P.V = cpu.P.C.xor((carry6.and(0x80) != 0))
        result = result and 0xff
        cpu.P.setNZFlags(result)
        cpu.A = result
    }
}