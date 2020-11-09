package com.beust.sixty

import com.beust.sixty.Op.*

class CpuTest: BaseTest() {
    override fun createComputer(vararg bytes: Int): IComputer {
        return Computer.create {
            memory = SimpleMemory(0x10000).apply {
                init(0, *bytes)
            }
            pcListener = object : PcListener {
                override fun onPcChanged(c: Computer) {
                    val opCode = memory[c.cpu.PC]
                    if (opCode == BRK.opcode || (opCode == RTS.opcode && c.cpu.SP.isEmpty())) {
                        c.stop()
                    }
                }
            }
        }.build()
    }

//    fun tsx() {
//        with(computer(LDX_IMM, 0x42, TXS, LDX_IMM, 0, 0xba)) {
//            Assertions.assertThat(cpu.SP.isEmpty())
//            run()
//            assertRegister(cpu.X, 0x42)
//        }
//    }
}