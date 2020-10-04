package com.beust.app

import com.beust.sixty.Computer
import com.beust.sixty.FileWatcher
import com.beust.sixty.PulseManager
import java.io.File

val RUN = true
var DEBUG = false
val BREAKPOINT: Int? = null//0xc2de // 0xc2db

val DISK_DOS_3_3 = File("D:\\pd\\Apple disks\\Apple DOS 3.3.dsk") // File("src/test/resources/Apple DOS 3.3.dsk")
val WOZ_DOS_3_3 = File("src/test/resources/woz2/DOS 3.3 System Master.woz")

fun disk(s: String) = File("disks/$s")

val disks = listOf(
        DISK_DOS_3_3,
        WOZ_DOS_3_3,
        File("src/test/resources/audit.dsk"),
        disk("Blade_of_Blackpoole_A.dsk"), // 3
        disk("Sherwood_Forest.dsk") ,       // 4
        File("d:/pd/Apple disks/Ultima I - The Beginning.woz"), // 5
        disk("Rescue Raiders.dsk"), // 6
        disk("Ultima4.dsk") , // 7
        disk("Force 7.woz"), // 8
        disk("Masquerade-1.dsk") // 9
)

val DISK = disks[0]

fun main() {
    val pulseManager = PulseManager()
    val fw = FileWatcher()
    var c = Apple2Computer()
    val gc = GraphicContext({ -> c }) { ->
        c.memory
    }
    val memoryListener = Apple2MemoryListener({ -> c.memory }, gc.textScreen, gc.hiResWindow)
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
                } else {
                    pulseManager.removeListeners()
                    gc.clear()
                    c = Apple2Computer()
                    with(c) {
                        memory.listeners.add(memoryListener)
                        pulseListeners.forEach { pulseManager.addListener(it) }
                        memoryListeners.forEach { c.memory.listeners.add(it) }
                        pulseManager.addListener(this)
                    }
                }
            }
        }.start()
    }
    gc.run()
    fw.stop = true
    pulseManager.stop()
}

