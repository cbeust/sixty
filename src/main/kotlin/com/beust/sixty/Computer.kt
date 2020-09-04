package com.beust.sixty

interface MemoryInterceptor {
    class Response(val allow: Boolean, val value: Int)

    fun onRead(location: Int): Response
    fun onWrite(location: Int, value: Int): Response
}

interface MemoryListener {
    val lastMemDebug: ArrayList<String>
    fun onRead(location: Int, value: Int)
    fun onWrite(location: Int, value: Int)
}

interface PcListener {
    fun onPcChanged(newValue: Int)
}

class Computer(val cpu: Cpu = Cpu(memory = Memory()), val memory: Memory,
        memoryListener: MemoryListener? = null,
        memoryInterceptor: MemoryInterceptor? = null,
        var pcListener: PcListener? = null
) {
    val pc by lazy { cpu.PC}
    val operand by lazy { memory[pc + 1] }
    val word by lazy { memory[pc + 2].shl(8).or(memory[pc + 1]) }

    private var startTime: Long = 0

    init {
        memory.listener = memoryListener
        memory.interceptor = memoryInterceptor
    }

    private var stop: Boolean = false

    fun stop() {
        stop = true
    }

    fun run() {
        startTime = System.currentTimeMillis()
        var cycles = 0
        var done = false
        var previousPc = 0
        while (! done && ! stop) {
            cycles++

            if (memory[cpu.PC] == 0x60 && cpu.SP.isEmpty()) {
                done = true
            } else {
                val inst = cpu.nextInstruction(this)
                if (inst == null) {
                    val s = cpu.PC.hh()
                    TODO("$s: $" + cpu.memory[cpu.PC].h() + ", cycles: $cycles")
                }
//                if (cpu.PC == 0x3484) {
//                    println(this)
//                    println("breakpoint: " + memory[0xe].h())
//                }
                previousPc = cpu.PC
                if (DEBUG_ASM) {
                    val debugString = formatPc(cpu, inst) + formatInstruction(inst)
                    inst.run(this)
                    // If the instruction modified the PC (e.g. JSR, JMP, BRK, RTS, RTI), don't change it
                    println("$cycles - " + debugString + " " + cpu.toString())
                } else {
                    inst.run(this)
                }
                if (! inst.changedPc) {
                    cpu.PC += inst.size
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
        val sec = (System.currentTimeMillis() - startTime) / 1000
        println("Computer stopping after $cycles cycles, $sec seconds, ${cycles / sec} cycles/sec")
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
                val inst = cpu.nextInstruction(this, noThrows = true)!!
                result.add(disassemble(cpu, inst, print))
                cpu.PC += inst.size
                pc += inst.size
                if (--n <= 0) done = true
            }
        }
        return result
    }

    private fun formatPc(cpu: Cpu, inst: Instruction): String {
        val pc = cpu.PC
        val bytes = StringBuffer(inst.opCode.h())
        bytes.append(if (inst.size > 1) (" " + memory[pc + 1].h()) else "   ")
        bytes.append(if (inst.size == 3) (" " + memory[pc + 2].h()) else "   ")
        return String.format("%-5s: %-10s", pc.hh(), bytes.toString())
    }

    private fun formatInstruction(inst: Instruction): String {
        return String.format("%-12s", inst.toString())
    }

    private fun disassemble(cpu: Cpu, inst: Instruction, print: Boolean): String {
        val pc = cpu.PC
        val bytes = StringBuffer(inst.opCode.h())
        bytes.append(if (inst.size > 1) (" " + memory[pc + 1].h()) else "   ")
        bytes.append(if (inst.size == 3) (" " + memory[pc + 2].h()) else "   ")
        val cpuString = String.format("%8s ${cpu}", " ")

        val result = String.format("%-5s %-10s %-12s %s", pc.hh() + ":", bytes.toString(), inst.toString(), cpuString)

        if (print) println(result)
        return result
    }

    override fun toString(): String {
        return "{Computer cpu:$cpu}\n$memory"
    }

}