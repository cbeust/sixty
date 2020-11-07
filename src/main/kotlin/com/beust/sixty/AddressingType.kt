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
            INDIRECT -> " ($${word.hh()})"
            INDIRECT_X -> " ($${byte.h()},X)"
            INDIRECT_Y -> " ($${byte.h()}),Y"
            REGISTER_A -> ""
            RELATIVE -> " $${(pc + byte.toByte() + 2).hh()}"
            ZPI, AIX, NONE -> ""
        }
    }

    /**
     * @return the correct address based on the addressing mode and the registers.
     */
    fun address(memory: IMemory, pc: Int, cpu: Cpu): Int {
        fun byte() = memory[pc + 1]
        fun word() = memory.word(pc + 1)

        return when(this) {
            ZP -> byte().and(0xff)
            ZP_X -> (byte() + cpu.X).and(0xff)
            ZP_Y -> (byte() + cpu.Y).and(0xff)
            ABSOLUTE -> word()
            ABSOLUTE_X -> word() + cpu.X
            ABSOLUTE_Y -> word() + cpu.Y
            INDIRECT -> word()
            INDIRECT_X -> memory.word((byte() + cpu.X).and(0xff))
            INDIRECT_Y -> memory.word(byte()) + cpu.Y
            else -> ERROR("Unexpected addressing type: $this")
        }
    }
}