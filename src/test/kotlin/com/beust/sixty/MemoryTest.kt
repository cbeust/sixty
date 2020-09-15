package com.beust.sixty

import org.testng.annotations.Test
import java.io.File

@Test
class MemoryTest {

    fun languageCard() {
        val start = 0x300
        val memory = Memory().apply {
            val ins = this::class.java.classLoader.getResource("6502_functional_test.bin")?.openStream()
            val ins2 = File("asm/ram").inputStream()
            load(ins2, start)
        }
        val c = Computer(cpu = Cpu(memory = memory),
                memoryListener = DebugMemoryListener()).apply {
            cpu.PC = start
            pcListener = object: PcListener {
                override fun onPcChanged(c: Computer) {
                    if (memory[c.cpu.PC] == 0) {
                        println("BRK ENCOUNTERED, TEST FAILED")
                        stop()
                    }
                }
            }
        }
        c.run()
    }
}
