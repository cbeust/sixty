package com.beust.app

import com.beust.sixty.*
import com.beust.sixty.Op.*

/**
 * Test computer where every character stored in address $300 is printed on stdout.
 */
object TestComputer {
    fun createComputer(): IComputer {
        return Computer.create {
            memory = SimpleMemory(size = 0x400).apply {
                init(0,
                        LDX_IMM.opcode, 0,
                        LDA_ZP_X.opcode, 0x10,
                        BEQ.opcode, 0x7,
                        STA_ABS.opcode, 0, 3,
                        INX.opcode,
                        JMP.opcode, 2, 0,
                        RTS.opcode,
                        NOP.opcode, NOP.opcode,
                        'H'.toInt(), 'E'.toInt(), 'L'.toInt(), 'L'.toInt(), 'O'.toInt(), '\n'.toInt(),
                        RTS.opcode
                )
            pcListener = object: PcListener {
                override fun onPcChanged(c: Computer) = with(c) {
                    if (memory[cpu.PC] == RTS.opcode) {
                        stop()
                    }
                }
            }
            memoryListeners.add(object : MemoryListener() {
                override fun isInRange(address: Int) = true
                override fun onWrite(location: Int, value: Int) {
                    if (location == 0x300) println(value.toChar())
                }
            })}
        }.build()
    }
}