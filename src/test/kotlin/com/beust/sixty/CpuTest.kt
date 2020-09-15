package com.beust.sixty

class CpuTest: BaseTest() {
    override fun createComputer(vararg bytes: Int) = Computer(Cpu(Memory().apply {
        init(0, *bytes)
    })).apply {
        pcListener = object: PcListener {
            override fun onPcChanged(c: Computer) {
                if (memory[c.cpu.PC] == BRK) stop()
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