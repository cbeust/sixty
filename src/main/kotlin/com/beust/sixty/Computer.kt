package com.beust.sixty

open class BaseMemoryListener {
    val lastMemDebug = arrayListOf<String>()
}

interface MemoryInterceptor {
    class Response(val override: Boolean, val value: Int)

    fun onRead(location: Int, value: Int): Response
    fun onWrite(location: Int, value: Int): Response
}

open class MemoryListener(val debugMem: Boolean = false): BaseMemoryListener() {
    open fun onRead(location: Int, value: Int) {}
    open fun onWrite(location: Int, value: Int){}
}

interface PcListener {
    fun onPcChanged(newValue: Int)
}

interface ICpu {
     fun nextInstruction(pc: Int, debugMemory: Boolean = false, debugAsm: Boolean = false)
}

class Computer(val cpu: Cpu2 = Cpu2(memory = Memory()),
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
            val opCode = memory[cpu.PC]

            if (opCode == 0x60 && cpu.SP.isEmpty()) {
                done = true
            } else {
//                if (cpu.PC == 0x2a66) {
//                    println(this)
//                    println("breakpoint: " + memory[0xe].h())
//                }

                val (byte, word) = byteWord()
                val debugString = formatPc(cpu.PC, opCode) + formatInstruction(opCode, cpu.PC, byte, word)
                previousPc = cpu.PC
                cpu.PC += SIZES[opCode]
                cpu.nextInstruction(previousPc)
                if (debugAsm) println(debugString + " " + cpu.toString())
//                if (debugAsm) {
//                    val inst = cpu.nextInstruction()
//                    val (byte, word) = byteWord()
//                    val debugString = formatPc(cpu, inst) + formatInstruction(inst, byte, word)
//                    cpu.PC += inst.size
//                    inst.run(this, byte, word)
//                    println(debugString + " " + cpu.toString())
//                } else {
//                    val inst = cpu.nextInstruction()
//                    val (byte, word) = byteWord()
//                    cpu.PC += inst.size
//                    inst.run(this, byte, word)
//                    if (cpu.PC == 0xc6f8) {
//                        stop()
//                    }
//                }

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

    private fun formatPc(cpu: Cpu, inst: Instruction) = formatPc(cpu.PC, inst.opCode)

    private fun formatPc(pc: Int, opCode: Int): String {
        val size = SIZES[opCode]
        val bytes = StringBuffer(opCode.h())
        bytes.append(if (size > 1) (" " + memory[pc + 1].h()) else "   ")
        bytes.append(if (size == 3) (" " + memory[pc + 2].h()) else "   ")
        return String.format("%-5s: %-11s", pc.hh(), bytes.toString())
    }

    private fun formatInstruction(opCode: Int, pc: Int, byte: Int, word: Int): String {
        val addressing = instructionModes[opCode]
        val name = NAMES[opCode]
        return String.format("%s %-12s", name, addressing.toString(pc, byte, word))
    }

    private fun formatInstruction(inst: Instruction, byte: Int, word: Int): String {
        return String.format("%-12s", inst.toString(this, byte, word))
    }

    fun disassemble(start: Int, length: Int = 10) {
        var pc = start
        val opCode = memory[pc]
        repeat(length) {
            with(Cpu.nextInstruction(memory[pc])) {
                val byte = memory[pc + 1]
                val word = memory[pc + 1].or(memory[pc + 2].shl(8))
                println(formatPc(pc, opCode) + toString(pc, byte, word))
                pc += size
            }
        }
    }

    override fun toString(): String {
        return "{Computer cpu:$cpu}\n$memory"
    }

}