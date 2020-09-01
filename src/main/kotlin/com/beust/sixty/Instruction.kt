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

    fun run()

    /**
     * @return 1 if a page bounday was crossed, 0 otherwise
     */
    fun pageCrossed(old: Int, new: Int): Int {
        return if (old.and(0x80).xor(new.and(0x80)) != 0) 1 else 0
    }
}