package com.beust.sixty

import com.beust.app.BREAKPOINT
import com.beust.app.DEBUG
import org.slf4j.LoggerFactory

interface MemoryInterceptor {
    /** If override is true, the returned value should be used instead of the one initially provided */
    data class Response(val allow: Boolean, val value: Int)

    val computer: Computer
    fun onRead(location: Int, value: Int): Response
    fun onWrite(location: Int, value: Int): Response
}

abstract class MemoryListener {
    val logLines = arrayListOf<String>()
    abstract fun isInRange(address: Int): Boolean
    open fun onRead(location: Int, value: Int): Int? = null
    open fun onWrite(location: Int, value: Int){}
}

interface PcListener {
    fun onPcChanged(c: Computer)
}

class Computer(val cpu: Cpu = Cpu(memory = Memory()),
//        memoryListener: MemoryListener? = null,
        memoryInterceptor: MemoryInterceptor? = null,
        var pcListener: PcListener? = null
): IPulse {
    private val log = LoggerFactory.getLogger("Breakpoint")
    val pc get() = cpu.PC
    val memory = cpu.memory

    private var startTime: Long = 0

    init {
//        memory.listener = memoryListener
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

    var cycles = 0
    var track = 0
    var sector = 0

    override fun onPulse(): PulseResult {
        step()
        return PulseResult(stop)
    }

    fun run(debugMemory: Boolean = false, _debugAsm: Boolean = false): RunResult {
        var done = false
        startTime = System.currentTimeMillis()
        while (!stop) {
            cycles++
            val done = step(debugMemory, _debugAsm)
            if (done) stop = true
        }

        return RunResult(System.currentTimeMillis() - startTime, cycles)
    }

    fun step(debugMemory: Boolean = false, _debugAsm: Boolean = false): Boolean {
        var debugAsm = _debugAsm
        var previousPc = 0
        val opCode = memory[cpu.PC]
        var done = false
        cycles++

        if (opCode == 0x60 && cpu.SP.isEmpty()) {
            done = true
        } else {
//                if (cycles >= 1156500) {
//                    DEBUG = true
//                }
            if (cpu.PC  == 0x6000) {
                DEBUG = true
            }
            if (BREAKPOINT != null && cpu.PC == BREAKPOINT) {
                log.debug(this.toString())
                log.debug("breakpoint")
                DEBUG = true
            }
//                if (cpu.PC == 0xc696) {
//                    if  (cpu.Y == 1) {
//                        sector = cpu.A
//                        if (track == 0 && sector == 10) {
//                            println("--- Read track $track sector ${cpu.A}")
//                        }
//                    } else if (cpu.Y == 2) {
//                        track = cpu.A
//                    }
//                }

            try {
//                    DEBUG = cycles >= 15348000
                debugAsm = DEBUG
                if (DEBUG) {
                    val (byte, word) = byteWord()
                    val debugString = formatPc(cpu.PC, opCode) + formatInstruction(opCode, cpu.PC, byte, word)
                    previousPc = cpu.PC
                    cpu.PC += SIZES[opCode]
                    cpu.nextInstruction(previousPc)
                    if (debugAsm) println(debugString + " " + cpu.toString() + " " + cycles)
                    if (true) { // debugMemory) {
                        memory.listeners.forEach {
                            it.logLines.forEach { println(it) }
                            it.logLines.clear()
                        }
                    }
                    ""
                } else {
                    previousPc = cpu.PC
                    cpu.PC += SIZES[opCode]
                    cpu.nextInstruction(previousPc)
                }
            } catch(ex: Throwable) {
                ex.printStackTrace()
                println("Failed at cycle $cycles")
                throw ex
            }

        if (previousPc == cpu.PC) {
                // Current functional tests highest score: 158489
                println(this)
                println("Forever loop after $cycles cycles")
                println("")
            } else {
                previousPc = cpu.PC
            }

            memory.lastMemDebug?.forEach {
                println("  $it")
            }
            memory.lastMemDebug?.clear()
        }
        pcListener?.onPcChanged(this)
        return done
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
        println("===================")
    }

    override fun toString(): String {
        return "{Computer cpu:$cpu}\n$memory"
    }

}