package com.beust.sixty

enum class AddressingType {
    IMMEDIATE, ZP, ZP_X, ZP_Y, ABSOLUTE, ABSOLUTE_X, ABSOLUTE_Y, INDIRECT_X, INDIRECT_Y, REGISTER_A, INDIRECT,
        RELATIVE, ZPI, AIX, NONE;

    fun toString(pc: Int, byte: Int, word: Int): String {
        return when(this) {
            IMMEDIATE -> " #$${byte.h()}"
            ZP -> " $${byte.h()}"
            ZP_X -> " $${byte.h()},X"
            ZP_Y -> " $${byte.h()},Y"
            ABSOLUTE -> " $${word.hh()}"
            ABSOLUTE_X -> " $${word.hh()},X"
            ABSOLUTE_Y -> " $${word.hh()},Y"
            INDIRECT_X -> " ($${byte.h()},X)"
            INDIRECT_Y -> " ($${byte.h()}),Y"
            REGISTER_A -> ""
            INDIRECT -> " ($${word.hh()})"
            RELATIVE -> " $${(pc + byte.toByte() + 2).hh()}"
            ZPI, AIX, NONE -> ""
        }
    }

    fun deref(memory: IMemory, pc: Int, cpu: Cpu): Pair<Int, () -> Int> {
        fun byte() = memory[pc + 1]
        fun word() = memory[pc + 1].or(memory[pc + 2].shl(8))

        val result = when(this) {
            ABSOLUTE -> word().let { word -> word to { -> memory[word] } }
            ZP -> byte().let { byte -> byte to { -> memory[byte] } }
            ZP_X -> byte().let { byte -> (byte + cpu.X).and(0xff).let { it to { -> memory[it] } } }
            ZP_Y -> byte().let { byte -> (byte + cpu.Y).and(0xff).let { it to { -> memory[it] } } }
            ABSOLUTE -> word().let { word -> word to { -> memory.word(word) } }
            ABSOLUTE_X -> word().let { word -> (word + cpu.X).let { it to { -> memory[it] } } }
            ABSOLUTE_Y -> word().let { word -> (word + cpu.Y).let { it to { -> memory[it] } } }
            INDIRECT -> word().let { word -> word to { -> memory.word(word) } }
            INDIRECT_X -> byte().let { byte -> memory.word((byte + cpu.X).and(0xff)).let { it to { -> memory[it] } } }
            INDIRECT_Y -> byte().let { byte -> memory[byte].or(memory[(byte + 1).and(0xff)].shl(8))
                    .let { (it + cpu.Y) to { -> memory[it + cpu.Y] } } }
            else -> 0 to { -> 0 }
        }

        return result
    }
}