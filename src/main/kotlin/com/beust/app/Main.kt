package com.beust.app

import com.beust.app.app.TextPanel
import com.beust.sixty.*
import com.beust.swt.ACTUAL_HEIGHT
import com.beust.swt.SwtContext
import com.beust.swt.createWindows
import org.eclipse.swt.widgets.Control
import java.io.File

val RUN = true
var DEBUG = false
// 6164: test failing LC writing
// check failing at 0x64f9
//val BREAKPOINT: Int? = 0x65f8 // 0x65c3 // 0x658d
val BREAKPOINT: Int? = null//0xc2de // 0xc2db
// val BREAKPOINT: Int? = 0x6036 // test break

val DISK_DOS_3_3 = File("D:\\pd\\Apple disks\\Apple DOS 3.3.dsk") // File("src/test/resources/Apple DOS 3.3.dsk")
val WOZ_DOS_3_3 = File("src/test/resources/woz2/DOS 3.3 System Master.woz")

val disks = listOf(
        DISK_DOS_3_3,
        WOZ_DOS_3_3,
        File("src/test/resources/audit.dsk"),
        File("disks/Blade_of_Blackpoole_A.dsk"), // 3
        File("disks/Sherwood_Forest.dsk") ,       // 4
        File("d:/pd/Apple disks/Ultima I - The Beginning.woz"), // 5
        File("disks/Rescue Raiders.dsk"), // 6
        File("disks/Ultima4.dsk")  // 7
)

val DISK = disks[0]
//val DISK = if (disk == 0)
//    WOZ_DOS_3_3
//else if (disk == 1)
//    DISK_DOS_3_3
//else if (disk == 2)
//    disks[2]
//else
////    File("disks/Sherwood_Forest.dsk")
//    disks[3]
////    DskDisk(File("d:\\pd\\Apple disks\\Ultima I - The Beginning.woz").inputStream())

//val DISK2 = WozDisk(
//        File("d:\\pd\\Apple Disks\\woz2\\First Math Adventures - Understanding word problems.woz").inputStream())

fun main() {
    val choice = 2

    val pulseManager = PulseManager()

//    val diskController = DiskController()
//    val apple2Memory = Apple2Memory()
//    val c = Computer.create {
//        memory = apple2Memory
//        memoryListeners.add(Apple2MemoryListener(apple2Memory))
//        memoryListeners.add(diskController)
//    }.build()
//    val start = apple2Memory[0xfffc].or(apple2Memory[0xfffd].shl(8))
//    c.cpu.PC = start
//
//    pulseListeners.add(c)
//    pulseListeners.add(diskController)
//
//    while (true) {
//        pulseListeners.forEach { it
//            it.onPulse()
//        }
//    }
    val fw = FileWatcher()

    val computer: () -> IPulse = when(choice) {
        1 -> {
            { ->
                println("Running the following 6502 program which will display HELLO")
                TestComputer.createComputer()
            }
//            c.disassemble(start = 0, length = 15)
//            pulseManager.addListener(c)
        }
        2 -> {
            { -> Apple2Computer().run() }
        }
        else -> {
            TODO("Should not happen")
        }
    }

    if (RUN) {
        Thread {
            var stop = false
            var c = computer()
            pulseManager.addListener(c)
            while (! stop) {
                val status = pulseManager.run()
                if (status == Computer.RunStatus.STOP) {
                    stop = true
                } else {
                    pulseManager.removeListener(c)
                    c = computer()
                    pulseManager.addListener(c)
                }
            }
        }.start()
    }
    swtContext?.run()
    fw.stop = true
    pulseManager.stop()
}

