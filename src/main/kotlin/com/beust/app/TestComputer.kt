package com.beust.app

import com.beust.sixty.*

/**
 * Test computer where every character stored in address $300 is printed on stdout.
 */
object TestComputer {
    fun createComputer(): IComputer {
        return Computer.create {
            memory = SimpleMemory(size = 0x400).apply {
                init(0,
                        LDX_IMM, 0,
                        LDA_ZP_X, 0x10,
                        BEQ, 0x7,
                        STA_ABS, 0, 3,
                        INX,
                        JMP, 2, 0,
                        RTS,
                        NOP, NOP,
                        'H'.toInt(), 'E'.toInt(), 'L'.toInt(), 'L'.toInt(), 'O'.toInt(), '\n'.toInt(),
                        RTS
                )
            pcListener = object: PcListener {
                override fun onPcChanged(c: Computer) = with(c) {
                    if (memory[cpu.PC] == RTS) {
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