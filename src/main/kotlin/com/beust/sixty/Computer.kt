package com.beust.sixty

interface MemoryInterceptor {
    class Response(val allow: Boolean, val value: Int)

    fun onRead(location: Int): Response
    fun onWrite(location: Int, value: Int): Response
}

open class MemoryListener {
    val lastMemDebug = arrayListOf<String>()
    open fun onRead(location: Int, value: Int) {}
    open fun onWrite(location: Int, value: Int){}
}

interface PcListener {
    fun onPcChanged(newValue: Int)
}

class Computer(val cpu: Cpu = Cpu(memory = Memory()),
        memoryListener: MemoryListener? = null,
        memoryInterceptor: MemoryInterceptor? = null,
        var pcListener: PcListener? = null
) {
    val pc get() = cpu.PC
    val memory = cpu.memory

    private var startTime: Long = 0

    init {
        memory.listener = memoryListener
        memory.interceptor = memoryInterceptor
    }

    private var stop: Boolean = false

    fun stop() {
        stop = true
    }

    fun byteWord(memory: Memory = cpu.memory, address: Int = cpu.PC + 1): Pair<Int, Int> {
        return memory[address] to memory[address].or(memory[address + 1].shl(8))
    }

    fun run(debugMemory: Boolean = false, debugAsm: Boolean = false): RunResult {
        startTime = System.currentTimeMillis()
        var cycles = 0
        var done = false
        var previousPc = 0
        while (! done && ! stop) {
            cycles++

            if (memory[cpu.PC] == 0x60 && cpu.SP.isEmpty()) {
                done = true
            } else {
//                if (cpu.PC == 0x2edc) {
//                    println(this)
//                    println("breakpoint: " + memory[0xe].h())
//                }
                previousPc = cpu.PC
                if (debugAsm) {
                    val inst = cpu.nextInstruction()
                    val (byte, word) = byteWord()
                    val debugString = formatPc(cpu, inst) + formatInstruction(inst, byte, word)
                    cpu.PC += inst.size
                    inst.run(this, byte, word)
                    println(debugString + " " + cpu.toString())
                } else {
                    val inst = cpu.nextInstruction()
                    val (byte, word) = byteWord()
                    cpu.PC += inst.size
                    inst.run(this, byte, word)
                }

                if (previousPc == cpu.PC) {
                    // Current functional tests highest score: 158489
                    println(this)
                    println("Forever loop after $cycles cycles")
                    println("")
                } else {
                    previousPc = cpu.PC
                }

                memory.listener?.lastMemDebug?.forEach {
                    println("  $it")
                }
                memory.listener?.lastMemDebug?.clear()
            }
            pcListener?.onPcChanged(cpu.PC)
        }
        return RunResult(System.currentTimeMillis() - startTime, cycles)
    }

    class RunResult(val durationMillis: Long, val cycles: Int)

    private fun formatPc(cpu: Cpu, inst: Instruction) = formatPc(cpu.PC, inst)

    private fun formatPc(pc: Int, inst: Instruction): String {
        val bytes = StringBuffer(inst.opCode.h())
        bytes.append(if (inst.size > 1) (" " + memory[pc + 1].h()) else "   ")
        bytes.append(if (inst.size == 3) (" " + memory[pc + 2].h()) else "   ")
        return String.format("%-5s: %-11s", pc.hh(), bytes.toString())
    }

    private fun formatInstruction(inst: Instruction, byte: Int, word: Int): String {
        return String.format("%-12s", inst.toString(this, byte, word))
    }

    fun disassemble(start: Int, length: Int = 10) {
        var pc = start
        repeat(length) {
            with(Cpu.nextInstruction(memory[pc])) {
                val byte = memory[pc + 1]
                val word = memory[pc + 1].or(memory[pc + 2].shl(8))
                println(formatPc(pc, this) + toString(pc, byte, word))
                pc += size
            }
        }
    }

    override fun toString(): String {
        return "{Computer cpu:$cpu}\n$memory"
    }

}