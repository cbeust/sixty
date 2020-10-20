package com.beust.app

import com.beust.sixty.FileWatcher
import com.beust.sixty.Runner
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
        disk("Planetfall - Disk 1, Side A.woz") // 15
)

val RUN = true
var DEBUG = false
val BREAKPOINT: Int? = null // 0x5a0//0xc6a6//0x5f8 // 0x5f8 // 0x5a0 //0x5f8 // 0x59e // null//0x556//0xc2de // 0xc2db
val DISK = DISKS[14]
const val SPEED_FACTOR = 4

object Threads {
    val threadPool = Executors.newFixedThreadPool(2)
    val scheduledThreadPool = Executors.newScheduledThreadPool(2)

    fun stop() {
        threadPool.shutdown()
        scheduledThreadPool.shutdown()
    }
}

fun main() {

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

