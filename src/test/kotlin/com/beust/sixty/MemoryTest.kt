package com.beust.sixty

import com.beust.app.DEBUG
import org.assertj.core.api.Assertions.assertThat
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File

@Test
class MemoryTest {

    fun languageCard() {
        val start = 0x300
        val memory = Memory().apply {
            loadResource("Apple2e.rom", 0xc000)
            val ins2 = File("asm/ram2").inputStream()
            load(ins2, start)
        }
        var success = true
        var message: String? = null

        DEBUG = true
        val c = Computer(cpu = Cpu(memory = memory)).apply {
            cpu.PC = start
            pcListener = object: PcListener {
                override fun onPcChanged(c: Computer) {
                    if (memory[c.cpu.PC] == 0) {
                        success = false
                        message = "Failed after " + memory[0x3d] + " tests, at PC " + c.cpu.PC.hh()
                        stop()
                    }
                }
            }
        }
        c.run()

        assertThat(success)
                .withFailMessage(message ?: "")
                .isTrue()
//        assertThat(c.cpu.P.C)
//                .withFailMessage("Failed after " + memory[0x3d] + " tests")
//                .isFalse()
    }
}
