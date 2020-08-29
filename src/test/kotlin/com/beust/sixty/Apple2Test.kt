package com.beust.sixty

import com.beust.app.Apple2StackPointer

class Apple2Test: BaseTest() {
    override fun computer(vararg bytes: Int): Computer {
        val memory = Memory(bytes = *bytes)
        val stackPointer = Apple2StackPointer(memory = memory)
        return Computer(Cpu(SP = stackPointer), memory)
    }

    fun tsx() {
        with(computer(0xa9, 0, 0x20, 5, 0, 0xba)) {
            assertRegister(cpu.X, 0)
            run()
            assertRegister(cpu.SP.S, 0xfd)
            assertRegister(cpu.X, 0xfd)
        }
    }
}