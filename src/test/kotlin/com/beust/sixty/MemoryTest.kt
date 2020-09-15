package com.beust.sixty

import org.assertj.core.api.Assertions.assertThat
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File

@Test
class MemoryTest {

    fun languageCard() {
        val start = 0x300
        val memory = Memory().apply {
            val ins2 = File("asm/ram").inputStream()
            load(ins2, start)
        }
        var success = true
        val c = Computer(cpu = Cpu(memory = memory),
                memoryListener = DebugMemoryListener()).apply {
            cpu.PC = start
            pcListener = object: PcListener {
                override fun onPcChanged(c: Computer) {
                    if (memory[c.cpu.PC] == 0) {
                        success = false
                        stop()
                    }
                }
            }
        }
        c.run()

        assertThat(success).isTrue()
    }
}
