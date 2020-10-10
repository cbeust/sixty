package com.beust.sixty

import com.beust.app.Apple2MemoryListener
import com.beust.app.BREAKPOINT
import com.beust.app.DEBUG
import com.beust.app.UiState
import org.slf4j.LoggerFactory

interface MemoryInterceptor {
    /** If override is true, the returned value should be used instead of the one initially provided */
    data class Response(val allow: Boolean, val value: Int)

    val computer: Computer
    fun onRead(location: Int, value: Int): Response
    fun onWrite(location: Int, value: Int): Response
}

interface IKeyProvider {
    fun keyPressed(memory: IMemory, value: Int, shift: Boolean = false, control: Boolean = false)
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

interface IComputer: IPulse {
    val memory: IMemory
    val cpu : Cpu
    fun reboot()
}

class ComputerBuilder {
    lateinit var memory: IMemory
    var memoryListeners = arrayListOf<MemoryListener>()
    var pcListener: PcListener? = null

    fun build(): IComputer {
       val result = Computer(memory, Cpu(memory), pcListener)
        memory.listeners.addAll(memoryListeners)
        return result
    }
}

class Computer(override val memory: IMemory, override val cpu: Cpu, val pcListener: PcListener?): IComputer {
    private val log = LoggerFactory.getLogger("Breakpoint")
    val pc get() = cpu.PC

    init {
        UiState.debug.addListener { _, new ->
            DEBUG = new
        }
    }

    companion object {
        fun create(init: ComputerBuilder.() -> Unit): ComputerBuilder {
            val result = ComputerBuilder()
            result.init()
            return result
        }
    }

    private var startTime: Long = 0
    enum class RunStatus { RUN, STOP, REBOOT }
    private var runStatus = RunStatus.RUN

    override fun stop() {
        runStatus = RunStatus.STOP
    }

    override fun reboot() {
        runStatus = RunStatus.REBOOT
    }

    fun word(memory: IMemory = cpu.memory, address: Int = cpu.PC + 1): Int = memory.word(address)

    fun byteWord(memory: IMemory = cpu.memory, address: Int = cpu.PC + 1): Pair<Int, Int> {
        return memory[address] to word(memory, address)
    }

    var cycles = 0
    var track = 0
    var sector = 0
    private var wait = 0

    override fun onPulse(manager: PulseManager): PulseResult {
        if (wait == 0) {
            wait = step() - 1
        } else {
            wait--
        }
        return PulseResult(runStatus)
    }

    fun run(debugMemory: Boolean = false, _debugAsm: Boolean = false): RunResult {
        var done = false
        startTime = System.currentTimeMillis()
        while (runStatus == RunStatus.RUN) {
            cycles++
            val done = step(debugMemory, _debugAsm)
        }

        return RunResult(System.currentTimeMillis() - startTime, cycles)
    }

    fun step(debugMemory: Boolean = false, _debugAsm: Boolean = false): Int {
        var debugAsm = _debugAsm
        var previousPc = 0
        val opCode = memory[cpu.PC]
        var done = false
        cycles++
        var timing = 0

        if (opCode == 0x60 && cpu.SP.isEmpty()) {
            done = true
        } else {
            if (BREAKPOINT != null) {
                if (cpu.PC in (BREAKPOINT - 20)..BREAKPOINT) {
                    DEBUG = true
                }
            }
////                if (cycles > 1180035) {
////                    println("cycle breakpoint")
////                }
//                if (cpu.PC == BREAKPOINT) {
//                    log.debug(this.toString())
//                    log.debug("breakpoint")
//                }
//            }
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
                    if (cpu.PC == BREAKPOINT) {
                        println("BREAKPOINT $memory")
                        println("A: " + cpu.A + " track: " + memory[0x2e] + " sector: " + memory[0x2d]
                                + " checksum: "  + memory[0x2d6 + cpu.Y].h())
                        if (cpu.A != 0) {
                            println("FAILURE")
                        }
                        ""
                    }
                    cpu.PC += SIZES[opCode]
                    timing = cpu.nextInstruction(previousPc)
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
                    timing = cpu.nextInstruction(previousPc)
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
        return timing
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