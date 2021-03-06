package com.beust.sixty

import com.beust.app.*

//interface MemoryInterceptor {
//    /** If override is true, the returned value should be used instead of the one initially provided */
//    data class Response(val allow: Boolean, val value: Int)
//
//    val computer: Computer
//    fun onRead(location: Int, value: Int): Response
//    fun onWrite(location: Int, value: Int): Response
//}

interface IKeyProvider {
    fun keyPressed(memory: IMemory, value: Int, shift: Boolean = false, control: Boolean = false)
    fun keyReleased(memory: IMemory, value: Int, shift: Boolean = false, control: Boolean = false)
    fun onController(memory: IMemory, c: GraphicContext.Controller)
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

interface IComputer {
    val memory: IMemory
    val cpu : Cpu
    fun step(): Computer.RunStatus
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
    val pc get() = cpu.PC

    init {
        UiState.debugAsm.addListener { _, new ->
            DEBUG_ASM = new
        }
//        UiState.diskStates[0].currentSector.addAfterListener { _, new ->
//            println("New sector read at PC " + cpu.PC.hh() + " : " + new)
//            ""
//        }
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

    fun stop() {
        runStatus = RunStatus.STOP
    }

    override fun reboot() {
        runStatus = RunStatus.REBOOT
    }

    fun word(memory: IMemory = cpu.memory, address: Int = cpu.PC + 1): Int = memory.word(address)

    fun byteWord(memory: IMemory = cpu.memory, address: Int = cpu.PC + 1): Pair<Int, Int> {
        return memory[address] to word(memory, address)
    }

    var track = 6
    var sector = 0
    private var wait = 0

    override fun step(): RunStatus {
        if (wait == 0) {
            wait = advanceCpu() - 1
        } else {
            wait--
        }
        return runStatus
    }

    fun run(debugMemory: Boolean = false, _debugAsm: Boolean = false): RunResult {
        var done = false
        startTime = System.currentTimeMillis()
        while (runStatus == RunStatus.RUN) {
            Cycles.cycles++
            val done = advanceCpu(debugMemory, _debugAsm)
        }

        return RunResult(System.currentTimeMillis() - startTime, Cycles.cycles)
    }

    fun advanceCpu(debugMemory: Boolean = false, _debugAsm: Boolean = false): Int {
        var previousPc = 0
        val opCode = memory[cpu.PC]
        var done = false
        var timing = 0

        if (opCode == 0x60 && cpu.SP.isEmpty()) {
            done = true
        } else {
            if (cpu.PC == BREAKPOINT) {
                ""
            }
            if (BREAKPOINT_RANGE != null) {
                DEBUG_ASM_RANGE = cpu.PC in BREAKPOINT_RANGE
                if (! DEBUG_ASM) DEBUG_ASM = DEBUG_ASM_RANGE
            }

            try {
//                    DEBUG = cycles >= 15348000
                if (cpu.PC < 0xf000 && (DEBUG || DEBUG_ASM || DEBUG_ASM_RANGE || TRACE_ON)) {
                    if (TRACE_ON && TRACE_CYCLES != 0L) {
                        Cycles.reset(TRACE_CYCLES)
                        TRACE_CYCLES = 0
                    }
                    previousPc = cpu.PC
                    cpu.PC += Op.opCodes[opCode].size
                    timing = cpu.nextInstruction(previousPc)
                    if (DEBUG_ASM || TRACE_ON) {
                        val (byte, word) = byteWord(address = previousPc + 1)
                        val debugString = formatPc(previousPc, opCode) + formatInstruction(opCode, previousPc,
                                byte, word)
                        val fullString = debugString + " ($timing) " + cpu.toString()
                        if (DEBUG_ASM) logAsm(fullString)
                        if (TRACE_ON) logAsmTrace(fullString)
                    }
                    if (true) { // debugMemory) {
                        memory.listeners.forEach {
                            it.logLines.forEach { println(it) }
                            it.logLines.clear()
                        }
                    }
                    ""
                } else {
                    previousPc = cpu.PC
                    cpu.PC += Op.opCodes[opCode].size
                    timing = cpu.nextInstruction(previousPc)
                }
            } catch(ex: Throwable) {
                ex.printStackTrace()
                println("Failed at cycle ${Cycles.cycles}")
                throw ex
            }

        if (previousPc == cpu.PC) {
                // Current functional tests highest score: 158489
                println(this)
                println("Forever loop after ${Cycles.cycles} cycles")
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

    class RunResult(val durationMillis: Long, val cycles: Long)

    private fun formatPc(pc: Int, opCode: Int): String {
        val size = Op.opCodes[opCode].size
        val bytes = StringBuffer(opCode.h())
        bytes.append(if (size > 1) (" " + memory[pc + 1].h()) else "  ")
        bytes.append(if (size == 3) (" " + memory[pc + 2].h()) else "  ")
        return String.format("%-4s: %-11s", pc.hh(), bytes.toString())
    }

    private fun formatInstruction(opCode: Int, pc: Int, byte: Int, word: Int): String {
        val op = Op.opCodes[opCode]
        return String.format("%s %-12s", op.opName, op.addressingType.toString(pc, byte, word))
    }

    private fun formatInstruction(inst: Instruction, byte: Int, word: Int): String {
        return String.format("%-12s", inst.toString(this, byte, word))
    }

    fun disassemble(start: Int, length: Int = 10) {
        var pc = start
        repeat(length) {
            val opCode = memory[pc]
            val addressing = Op.opCodes[opCode].addressingType
            val byte = memory[pc + 1]
            val word = memory[pc + 1].or(memory[pc + 2].shl(8))
            println(formatPc(pc, opCode) + formatInstruction(opCode, pc, byte, word) + " " + cpu.toString())
            pc += Op.opCodes[opCode].size
        }
        println("===================")
    }

    override fun toString(): String {
        return "{Computer cpu:$cpu}\n$memory"
    }

}