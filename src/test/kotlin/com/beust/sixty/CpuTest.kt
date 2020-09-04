package com.beust.sixty

import org.assertj.core.api.Assertions

class CpuTest: BaseTest() {
    override fun createComputer(vararg bytes: Int) = Computer(Cpu(Memory(bytes = *bytes)))

//    fun tsx() {
//        with(computer(LDX_IMM, 0x42, TXS, LDX_IMM, 0, 0xba)) {
//            Assertions.assertThat(cpu.SP.isEmpty())
//            run()
//            assertRegister(cpu.X, 0x42)
//        }
//    }
}