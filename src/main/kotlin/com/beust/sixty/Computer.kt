package com.beust.sixty

interface MemoryInterceptor {
    class Response(val allow: Boolean, val value: Int)

    fun onRead(location: Int): Response
    fun onWrite(location: Int, value: Int): Response
}

interface MemoryListener {
    fun onRead(location: Int, value: Int)
    fun onWrite(location: Int, value: Int)
}

class Computer(val cpu: Cpu = Cpu(memory = Memory()), val memory: Memory,
        memoryListener: MemoryListener? = null,
        memoryInterceptor: MemoryInterceptor? = null) {
    init {
        memory.listener = memoryListener
        memory.interceptor = memoryInterceptor
    }

    fun run() {
        var n = 0
        var done = false
        var previousPc = 0
        while (! done) {
            if (memory[cpu.PC] == 0x60 && cpu.SP.isEmpty()) {
                done = true
            } else {
                val inst = cpu.nextInstruction(this)
                if (cpu.PC == 0x800) {
                    println(this)
                    println("Breakpoint")
                }
                inst.run()
                if (DEBUG_ASM) disassemble(cpu, inst, print = true)
                cpu.PC += inst.size
                n++
                if (previousPc == cpu.PC) {
                    // Current functional tests highest score: $9d7
                    println(this)
                    println("Forever loop")
                } else {
                    previousPc = cpu.PC
                }
            }
        }
    }

    fun clone(): Computer {
        return Computer(cpu.clone(), Memory(memory.size, *memory.content))
    }

    fun disassemble(add: Int = cpu.PC, length: Int = 10, print: Boolean = true): List<String> {
        val result = arrayListOf<String>()
        with (clone()) {
            cpu.PC = add
            var pc = cpu.PC
            var done = false
            var n = length
            while (! done) {
                val p = memory[pc]
                val inst = cpu.nextInstruction(this, noThrows = true)
                result.add(disassemble(cpu, inst, print))
                cpu.PC += inst.size
                pc += inst.size
                if (--n <= 0) done = true
            }
        }
        return result
    }

    private fun disassemble(cpu: Cpu, inst: Instruction, print: Boolean): String {
        val pc = cpu.PC
        val bytes = StringBuffer(inst.opCode.h())
        bytes.append(if (inst.size > 1) (" " + memory[pc + 1].h()) else "   ")
        bytes.append(if (inst.size == 3) (" " + memory[pc + 2].h()) else "   ")
        val cpuString = String.format("%8s ${cpu}", " ")
        val result = (pc.h() + ": " + bytes.toString() + "  " + inst.toString()) + " " + cpuString
        if (print) println(result)
        return result
    }

    override fun toString(): String {
        return "{Computer cpu:$cpu}\n$memory"
    }

}