package com.beust.app

import com.beust.sixty.*
import java.io.File
import java.util.concurrent.Executors

fun disk(s: String) = File("disks/$s")

val DISKS = listOf(
        disk("Apple DOS 3.3.dsk"), // 0  6
        disk("Apple DOS 3.3.woz"), // 1
        File("src/test/resources/audit.dsk"), // 2
        disk("Blade_of_Blackpoole_A.dsk"), // 3
        disk("Sherwood_Forest.dsk") ,       // 4
        File("d:/pd/Apple disks/Ultima I - The Beginning.woz"), // 5
        disk("Rescue Raiders.dsk"), // 6
        disk("Ultima4.dsk") , // 7
        disk("Force 7.woz"), // 8
        disk("Masquerade-1.dsk"), // 9
        disk("Bouncing Kamungas - Disk 1, Side A.woz"), // 10
        disk("King's Quest - A.woz"), // 11
        disk("Karateka.dsk"), // 12
        disk("Karateka.woz"), // 13
        disk("Commando - Disk 1, Side A.woz"), // 14
        disk("Planetfall - Disk 1, Side A.woz"), // 15
        disk("Sammy Lightfoot - Disk 1, Side A.woz"), // 16
        disk("Stargate - Disk 1, Side A.woz"), // 17
        disk("Blazing Paddles (Baudville).woz"), // 18
        disk("Hard Hat Mack - Disk 1, Side A.woz"),// 19
        disk("Stellar 7.woz") // 20
)

enum class NibbleStrategy(val hold:Int = 0) { LSS, BYTES(36), BITS(12) }
val NIBBLE_STRATEGY = NibbleStrategy.LSS
val RUN = true
var DEBUG = true
var DEBUG_ASM = false
var DEBUG_ASM_RANGE = false
var DEBUG_BITS = false
var TRACE_ON = false
var TRACE_CYCLES = 0x09D4FD12L
val BREAKPOINT: Int? = 0xd81e// 0xb980 // 0x9e52 // null // 0x5a0//0xc6a6//0x5f8 // 0x5f8 // 0x5a0
val BREAKPOINT_RANGE: IntRange? = null // 0x9e42..0x9e56 // = null// 0xbb00..0xbbff
val BREAKPOINT_WRITE: Int? = 0x2d // 0xd89f
val DISK = DISKS[7]
const val SPEED_FACTOR = 1

object Threads {
    val threadPool = Executors.newFixedThreadPool(2)
    val scheduledThreadPool = Executors.newScheduledThreadPool(2)

    fun stop() {
        threadPool.shutdown()
        scheduledThreadPool.shutdown()
    }
}

fun main() {

//    val lines = arrayListOf<String>()
//    File("src/main/kotlin/com/beust/sixty/Constants.kt").forEachLine { lines.add(it) }
//    repeat(256) {
//        val op = OPCODES[it]
//        val n = op.name + (if (op.addressingType != AddressingType.NONE) ("_" + op.addressingType.name) else "")
//        val name = if (n == "NOP") "NOP_${op.opcode.h()}" else n
//        println("$name(0x" + op.opcode.h().toLowerCase() + ", \"${op.name}\", ${op.size}, ${op.timing}, ${op.addressingType.name}),")
//    }

    val fw = FileWatcher()
    val gc = GraphicContext()
    var c = Apple2Computer(gc)
    gc.reset(c)
    Threads.threadPool.submit {
        fw.run(c.memory)
    }

    val runner = Runner(gc)
    if (RUN) {
        runner.runPeriodically(c)
        gc.run()
        runner.stop()
        fw.stop()
        Threads.stop()
    }
}

