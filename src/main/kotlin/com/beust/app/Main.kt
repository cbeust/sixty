package com.beust.app

import A2Computer
import GraphicContext
import com.beust.sixty.Computer
import com.beust.sixty.FileWatcher
import com.beust.sixty.PulseManager
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

fun disk(s: String) = File("disks/$s")

val disks = listOf(
        DISK_DOS_3_3,
        WOZ_DOS_3_3,
        File("src/test/resources/audit.dsk"),
        disk("Blade_of_Blackpoole_A.dsk"), // 3
        disk("/Sherwood_Forest.dsk") ,       // 4
        File("d:/pd/Apple disks/Ultima I - The Beginning.woz"), // 5
        disk("/Rescue Raiders.dsk"), // 6
        disk("/Ultima4.dsk") , // 7
        disk("/Force 7.woz") // 8
)

val DISK = disks[0]
//val DISK = if (disk == 0)
//    WOZ_DOS_3_3
//else if (disk == 1)
//    DISK_DOS_3_3
//else if (disk == 2)
//    disks[2]
//else
////    disk("/Sherwood_Forest.dsk")
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

//    val computer: () -> IPulse = when(choice) {
//        1 -> {
//            { ->
//                println("Running the following 6502 program which will display HELLO")
//                TestComputer.createComputer()
//            }
////            c.disassemble(start = 0, length = 15)
////            pulseManager.addListener(c)
//        }
//        2 -> {
//            { -> Apple2Computer().run() }
//        }
//        else -> {
//            TODO("Should not happen")
//        }
//    }

    var c = A2Computer()
    val gc = GraphicContext({ -> c }) { ->
        c.memory
    }
    val memoryListener = Apple2MemoryListener({ -> c.memory }, gc.textScreen, gc.hiResWindow)
    c.memory.listeners.add(memoryListener)

    c.pulseListeners.forEach { pulseManager.addListener(it) }
    c.memoryListeners.forEach { c.memory.listeners.add(it) }

//    val p = Apple2Computer().run(pulseManager)
//    val swtContext = p.first
//    val computer = { -> p.second }
    if (RUN) {
        Thread {
            var stop = false
            while (! stop) {
                val status = pulseManager.run()
                if (status == Computer.RunStatus.STOP) {
                    stop = true
                } else {
                    pulseManager.removeListeners()
                    println("Creating a new A2Computer")
                    gc.clear()
                    c = A2Computer()
                    c.memory.listeners.add(memoryListener)
                    c.pulseListeners.forEach { pulseManager.addListener(it) }
                    c.memoryListeners.forEach { c.memory.listeners.add(it) }
                    println("Done")
                    pulseManager.addListener(c)
                }
            }
        }.start()
    }
    gc.run()
    fw.stop = true
    pulseManager.stop()
}

