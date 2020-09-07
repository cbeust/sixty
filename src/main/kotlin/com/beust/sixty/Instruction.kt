package com.beust.sixty

interface Instruction {
    /**
     * Opcode of this instruction
     */
    val opCode: Int

    /**
     * Number of bytes occupied by this op (1, 2, or 3).
     */
    val size: Int

    /**
     * This should be a property and not a constant since the value of the timing can change for certain ops when
     * a page boundary is crossed.
     */
    val timing: Int

    val addressing: AddressingType

    val name: String

    fun run(c: Computer) {
        val (byte, word) = c.byteWord()
        run(c, byte, word)
    }

    fun run(c: Computer, byte: Int, word: Int)

    /**
     * Dynamic toString(): uses the memory and register to display itselt.
     */
    fun toString(c: Computer, byte: Int, word: Int): String

    /**
     * Static toString(): disassemble just based on the opcodes.
     */
    fun toString(pc: Int, byte: Int, word: Int): String

    /**
     * @return 1 if a page bounday was crossed, 0 otherwise
     */
    fun pageCrossed(old: Int, new: Int): Int {
        return if (old.and(0x80).xor(new.and(0x80)) != 0) 1 else 0
    }
}