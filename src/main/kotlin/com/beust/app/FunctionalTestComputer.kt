package com.beust.app

import com.beust.sixty.*

fun functionalTestComputer(): Computer {
    val functionalTestMemory = Memory(65536).apply {
        load("bin_files/6502_functional_test.bin", 0)
        this[0x37c9] = BEQ
        this[0x37ce] = BEQ
    }
    val functionalTestCpu = Cpu(memory = functionalTestMemory)
    val result = Computer(memory = functionalTestMemory, cpu = functionalTestCpu,
            memoryListener = DebugMemoryListener).apply {
        pcListener = object: PcListener {
            override fun onPcChanged(newValue: Int) {
                if (newValue == 0x346c) stop()
            }

        }
        cpu.PC = 0x400
    }
    return result
}


