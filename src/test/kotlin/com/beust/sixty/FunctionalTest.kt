package com.beust.sixty

import org.testng.annotations.Test

@Test
class FunctionalTest {

    fun functionalTest() {
        val memory = Memory(65536).apply {
            val ins = this::class.java.classLoader.getResource("6502_functional_test.bin")?.openStream()
            load(ins!!, 0)
        }
        Computer(cpu = Cpu2(memory = memory),
                memoryListener = DebugMemoryListener()).apply {
            pcListener = object : PcListener {
                override fun onPcChanged(newValue: Int) {
                    if (newValue == 0x346c || newValue == 0x3469) {
                        println("\nAll 6502 functional tests passed")
                        stop()
                    }
                }
            }
            cpu.PC = 0x400
        }.run()
    }
}
