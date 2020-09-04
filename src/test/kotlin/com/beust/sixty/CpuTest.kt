package com.beust.sixty

import org.assertj.core.api.Assertions

class CpuTest: BaseTest() {
    override fun createComputer(vararg bytes: Int) = Computer(Cpu(Memory(bytes = *bytes))).apply {
        pcListener = object: PcListener {
            override fun onPcChanged(newValue: Int) {
                if (memory[newValue] == BRK) stop()
            }

        }
    }

//    fun tsx() {
//        with(computer(LDX_IMM, 0x42, TXS, LDX_IMM, 0, 0xba)) {
//            Assertions.assertThat(cpu.SP.isEmpty())
//            run()
//            assertRegister(cpu.X, 0x42)
//        }
//    }
}