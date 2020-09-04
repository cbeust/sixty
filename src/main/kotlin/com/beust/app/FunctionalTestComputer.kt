package com.beust.app

import com.beust.sixty.*

fun functionalTestComputer(): Computer {
    val functionalTestMemory = Memory(65536).apply {
        load("bin_files/6502_functional_test.bin", 0)
    }
    val functionalTestCpu = Cpu(memory = functionalTestMemory)
    val result = Computer(memory = functionalTestMemory, cpu = functionalTestCpu,
            memoryListener = DebugMemoryListener).apply {
        pcListener = object: PcListener {
            override fun onPcChanged(newValue: Int) {
//                if (newValue == 0x334e) {
//                    print("\r  Arithmetic test (hex): " + memory[0xe])
//                }
//                if (newValue == 0x3401) {
//                    print("\r  Arithmetic test (bcd): " + memory[0xe])
//                }
                if (newValue == 0x346c || newValue == 0x3469) {
                    println("\nAll tests passed")
                    stop()
                }
            }

        }
        cpu.PC = 0x400
    }
    return result
}


