package com.beust.app

import com.beust.sixty.*

/**
 * Test computer where every character stored in address $300 is printed on stdout.
 */
object TestComputer {
    fun createComputer(): IComputer {
        val memory = SimpleMemory(size = 0x400).apply {
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

        val result = SimpleComputer(memory, Cpu(memory))

        return result
    }
}