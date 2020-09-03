package com.beust.sixty

import org.assertj.core.api.Assertions

class CpuTest: BaseTest() {
    override fun createComputer(vararg bytes: Int) = Computer(memory = Memory(bytes = *bytes))

    fun tsx() {
        with(computer(0xa2, 0x42, 0x9a, 0xa2, 0, 0xba)) {
            Assertions.assertThat(cpu.SP.isEmpty())
            disassemble()
            run()
            assertRegister(cpu.X, 0x42)
        }
    }
}