package com.beust.sixty

import com.beust.app.StackPointer

class Apple2Test: BaseTest() {
    override fun createComputer(vararg bytes: Int): Computer {
        val memory = SimpleMemory(0x10000).apply {
            init(0, *bytes)
        }
        val stackPointer = StackPointer(memory = memory)

        return Computer(Cpu(memory)).apply {
            pcListener = object: PcListener {
                override fun onPcChanged(c: Computer) {
                    val newValue = c.cpu.PC
                    if (memory[newValue] == BRK) stop()
                }

            }
        }
    }

//    fun tsx() {
//        with(computer(0xa9, 0, //       LDA #$00
//                0x20, 5, 0,    //       JSR $0005
//                0xba,          // 0005: TSX
//                0xba)) {       //       TSX
//            assertRegister(cpu.X, 0)
//            run()
//            assertRegister(cpu.SP.S, 0xfd)
//            assertRegister(cpu.X, 0xfd)
//        }
//    }
}