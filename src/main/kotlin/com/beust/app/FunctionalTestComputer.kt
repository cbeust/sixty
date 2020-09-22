package com.beust.app

import com.beust.sixty.*

class SimpleComputer(override val memory: IMemory, override val cpu: Cpu): IComputer, IPulse {
    val pcListener: PcListener = object: PcListener {
        override fun onPcChanged(c: Computer) {
            val newValue = c.cpu.PC
            if (newValue == 0x334e) {
                print("\r  Arithmetic test (hex): " + memory[0xe])
            }
            if (newValue == 0x3401) {
                print("\r  Arithmetic test (bcd): " + memory[0xe])
            }
            if (newValue == 0x346c || newValue == 0x3469) {
                println("\nAll tests passed")
                this@SimpleComputer.stop()
            }
        }
    }
    private val computer = Computer(memory, cpu, pcListener)
    override fun stop() = computer.stop()
    override fun onPulse() = computer.onPulse()
}

fun functionalTestComputer(debugMemory: Boolean): IComputer {
    val functionalTestMemory = SimpleMemory(65536).apply {
        loadResource("src/test/resources/6502_functional_test.bin", 0x400)
    }
    val cpu = Cpu(memory = functionalTestMemory)
    val result = SimpleComputer(functionalTestMemory, cpu)
    cpu.PC = 0x400
    return result
}



