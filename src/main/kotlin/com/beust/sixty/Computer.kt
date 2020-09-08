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
    fun onPcChanged(c: Computer)
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

    fun word(memory: Memory = cpu.memory, address: Int = cpu.PC + 1): Int
        = memory[address].or(memory[address + 1].shl(8))

    fun byteWord(memory: Memory = cpu.memory, address: Int = cpu.PC + 1): Pair<Int, Int> {
        return memory[address] to word(memory, address)
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
//                if (cpu.PC == 0xc6f8) {
//                    println(this)
//                    println("breakpoint: " + memory[0xe].h())
//                }

                if (debugAsm) {
                    val (byte, word) = byteWord()
                    val debugString = formatPc(cpu.PC, opCode) + formatInstruction(opCode, cpu.PC, byte, word)
                    previousPc = cpu.PC
                    cpu.PC += SIZES[opCode]
                    cpu.nextInstruction(previousPc)
                    if (debugAsm) println(debugString + " " + cpu.toString())
                    if (0xc67a == cpu.PC) {
                        println("PROBLEM")
                    }
                    if (0xc6cb == cpu.PC) {
                        disassemble(0xc6cb)
                    }
                    ""
                } else {
                    previousPc = cpu.PC
                    cpu.PC += SIZES[opCode]
                    cpu.nextInstruction(previousPc)
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
            pcListener?.onPcChanged(this)
        }
        return RunResult(System.currentTimeMillis() - startTime, cycles)
    }

    class RunResult(val durationMillis: Long, val cycles: Int)

    private fun formatPc(pc: Int, opCode: Int): String {
        val size = SIZES[opCode]
        val bytes = StringBuffer(opCode.h())
        bytes.append(if (size > 1) (" " + memory[pc + 1].h()) else "   ")
        bytes.append(if (size == 3) (" " + memory[pc + 2].h()) else "   ")
        return String.format("%-5s: %-11s", pc.hh(), bytes.toString())
    }

    private fun formatInstruction(opCode: Int, pc: Int, byte: Int, word: Int): String {
        val addressing = ADDRESSING_TYPES[opCode]
        val name = OPCODE_NAMES[opCode]
        return String.format("%s %-12s", name, addressing.toString(pc, byte, word))
    }

    private fun formatInstruction(inst: Instruction, byte: Int, word: Int): String {
        return String.format("%-12s", inst.toString(this, byte, word))
    }

    fun disassemble(start: Int, length: Int = 10) {
        var pc = start
        repeat(length) {
            val opCode = memory[pc]
            val addressing = ADDRESSING_TYPES[opCode]
            val byte = memory[pc + 1]
            val word = memory[pc + 1].or(memory[pc + 2].shl(8))
            println(formatPc(pc, opCode) + formatInstruction(opCode, pc, byte, word) + " " + cpu.toString())
            pc += SIZES[opCode]
        }
    }

    override fun toString(): String {
        return "{Computer cpu:$cpu}\n$memory"
    }

}