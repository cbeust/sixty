package com.beust.sixty

import org.testng.annotations.Test

@Test
class FunctionalTest {

    fun functionalTest() {
        val computer = Computer.create {
            memory = SimpleMemory(65536).apply {
                val ins = this::class.java.classLoader.getResource("6502_functional_test.bin")?.openStream()
                load(ins!!.readAllBytes())
            }
            pcListener = object : PcListener {
                private var previous = 0
                override fun onPcChanged(c: Computer) {
                    val newValue = c.cpu.PC
                    if (newValue == 0x346c || newValue == 0x3469) {
                        println("\nAll 6502 functional tests passed")
                        c.stop()
                    }
                    if (previous != 0 && previous == newValue) {
                        throw AssertionError("Infinite loop at PC:" + newValue.hh())
                    }
                    previous = newValue
                }
            }
        }.build()
        computer.cpu.PC = 0x400
        var runStatus = Computer.RunStatus.RUN
        while (runStatus == Computer.RunStatus.RUN) {
            runStatus = computer.step()
        }
    }
}
