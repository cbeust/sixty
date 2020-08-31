package com.beust.sixty

import com.beust.app.StackPointer

class Apple2Test: BaseTest() {
    override fun createComputer(vararg bytes: Int): Computer {
        val memory = Memory(bytes = *bytes)
        val stackPointer = StackPointer(memory = memory)
        val listener = object: MemoryListener {
            override var lastMemDebug: String? = null

            override fun onRead(location: Int, value: Int) {
            }

            override fun onWrite(location: Int, value: Int) {
                if (location >= 0x400 && location < 0x7ff) {
                    lastMemDebug = "Writing on the text screen: $" + location.hh() + "=$" + value.h()
                } else if (location >= 0x2000 && location <= 0x3fff) {
                    lastMemDebug = "Writing in graphics"
                }
            }

        }
        return Computer(memory = memory)
    }

    fun tsx() {
        with(computer(0xa9, 0, //       LDA #$00
                0x20, 5, 0,    //       JSR $0005
                0xba,          // 0005: TSX
                0xba)) {       //       TSX
            assertRegister(cpu.X, 0)
            disassemble()
            run()
            assertRegister(cpu.SP.S, 0xfd)
            assertRegister(cpu.X, 0xfd)
        }
    }
}