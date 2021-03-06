package com.beust.sixty

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test
import java.io.File

@Test
class MemoryTest {

    fun memorySize() {
        runTest("memory-size.bin")
    }

    @Test(enabled = false)
    fun auxmem() {
        runTest("auxmem.bin")
    }

    fun bankMemory() {
        runTest("bank-memory.bin")
    }

    fun cxxx() {
        runTest("cxxx.bin")
    }

    private fun runTest(fileName: String) {
        val start = 0x6000
        var success = true
        var message: String? = null

//        DEBUG = true
        val mem = Apple2Memory().apply {
            val ins2 = File("asm/$fileName").inputStream()
            load(ins2.readAllBytes(), start)
            listeners.add(DebugMemoryListener(this))
        }
        val c = Computer.create {
            memory = mem
            pcListener = object : PcListener {
                override fun onPcChanged(c: Computer) {
                    if (memory[c.cpu.PC] == 0) {
                        success = false
                        val word = memory.word(address = 0x3d + c.cpu.Y)
                        val expected = memory[word]
                        message = "Failed at test #" + (memory[0x3d] + 1) + " at comparison #" + c.cpu.Y +
                                " got " + c.cpu.A
                        ", PC=" + c.cpu.PC.hh()
                        c.stop()
                    }
                }
            }
        }.build()
        c.cpu.PC = start

        assertThat(success)
                .withFailMessage(message ?: "")
                .isTrue()
        println(mem[0x3d].toString() + " memory tests passed")
//        assertThat(c.cpu.P.C)
//                .withFailMessage("Failed after " + memory[0x3d] + " tests")
//                .isFalse()
    }
}
