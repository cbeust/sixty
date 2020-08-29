package com.beust.sixty

class CpuTest: BaseTest() {
    override fun computer(vararg bytes: Int) = Computer(memory = Memory(bytes = *bytes))
}