package com.beust.app

import com.beust.sixty.Computer
import com.beust.sixty.FileWatcher
import com.beust.sixty.PulseManager
import java.io.File

val RUN = true
var DEBUG = false
val BREAKPOINT: Int? = null//0xc6a6//0x5f8 // 0x5f8 // 0x5a0 //0x5f8 // 0x59e // null//0x556//0xc2de // 0xc2db

val DISK_DOS_3_3 = File("D:\\pd\\Apple disks\\Apple DOS 3.3.dsk") // File("src/test/resources/Apple DOS 3.3.dsk")
val WOZ_DOS_3_3 = File("src/test/resources/woz2/DOS 3.3 System Master.woz")

fun disk(s: String) = File("disks/$s")

val disks = listOf(
        DISK_DOS_3_3,
        WOZ_DOS_3_3,
        File("src/test/resources/audit.dsk"), // 2
        disk("Blade_of_Blackpoole_A.dsk"), // 3
        disk("Sherwood_Forest.dsk") ,       // 4
        File("d:/pd/Apple disks/Ultima I - The Beginning.woz"), // 5
        disk("Rescue Raiders.dsk"), // 6
        disk("Ultima4.dsk") , // 7
        disk("Force 7.woz"), // 8
        disk("Masquerade-1.dsk"), // 9
        disk("Bouncing Kamungas - Disk 1, Side A.woz") // 10
)

val DISK = disks[4]

fun main() {
    val pulseManager = PulseManager()
    val fw = FileWatcher()
    var c = Apple2Computer()
    val gc = GraphicContext({ -> c }) { ->
        c.memory
    }
    val memoryListener = Apple2MemoryListener({ -> c.memory }, gc.textWindow, gc.hiResWindow)
    c.memory.listeners.add(memoryListener)

    c.pulseListeners.forEach { pulseManager.addListener(it) }
    c.memoryListeners.forEach { c.memory.listeners.add(it) }

    if (RUN) {
        Thread {
            var stop = false
            while (! stop) {
                val status = pulseManager.run()
                if (status == Computer.RunStatus.STOP) {
                    stop = true
                } else if (status == Computer.RunStatus.REBOOT) {
                    pulseManager.removeListeners()
                    gc.clear()
                    c = Apple2Computer()
                    with(c) {
                        memory.listeners.add(memoryListener)
                        pulseListeners.forEach { pulseManager.addListener(it) }
                        memoryListeners.forEach { c.memory.listeners.add(it) }
//                        pulseManager.addListener(this)
                    }
                } // else STOP
            }
        }.start()
    }
    gc.run()
    fw.stop = true
    pulseManager.stop()
}

