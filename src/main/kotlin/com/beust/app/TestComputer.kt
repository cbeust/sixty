package com.beust.app

import com.beust.sixty.*

/**
 * Test computer where every character stored in address $300 is printed on stdout.
 */
object TestComputer {
    fun createComputer(): Computer {
        val memory = Memory(size = 0x400).apply {
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
                    0
            )
        }

        val result = Computer(Cpu2(memory),
                memoryListener = object: MemoryListener() {
                    override fun onWrite(location: Int, value: Int) {
                        if (location == 0x300) {
                            print(value.toChar())
                        }
                    }
                })

        return result
    }
}