package com.beust.app

import com.beust.app.app.TextPanel
import com.beust.sixty.*
import com.beust.swt.SwtContext
import com.beust.swt.createWindows
import java.io.File
import java.nio.file.Paths

var DEBUG = false
// 6164: test failing LC writing
// check failing at 0x64f9
//val BREAKPOINT: Int? = 0x65f8 // 0x65c3 // 0x658d
val BREAKPOINT: Int? = null//0xc2de // 0xc2db
// val BREAKPOINT: Int? = 0x6036 // test break

val disk = 0

val DISK_DOS_3_3 = File("src/test/resources/Apple DOS 3.3.dsk")
val WOZ_DOS_3_3 = File("src/test/resources/woz2/DOS 3.3 System Master.woz")

val DISK = if (disk == 0)
    WOZ_DOS_3_3
else if (disk == 1)
    DISK_DOS_3_3
else if (disk == 2)
    File("src/test/resources/audit.dsk")
else
    File("d:\\pd\\Apple disks\\Sherwood Forest.dsk")
//    DskDisk(File("d:\\pd\\Apple disks\\Ultima I - The Beginning.woz").inputStream())

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
    var swtContext: SwtContext? = null
    val fw = FileWatcher()

    when(choice) {
        1 -> {
            println("Running the following 6502 program which will display HELLO")
            val c = TestComputer.createComputer()
//            c.disassemble(start = 0, length = 15)
            pulseManager.addListener(c)
        }
        2 -> {
            val debugMem = false
            val debugAsm = DEBUG
//            frame()
            val dc = DiskController(6).apply {
                loadDisk(IDisk.create(DISK))
                UiState.currentDiskFile.value = DISK
            }
            val keyProvider = object: IKeyProvider {
                override fun keyPressed(memory: IMemory, value: Int, shift: Boolean, control: Boolean) {
                    memory.forceInternalRomValue(0xc000, value.or(0x80))
                    memory.forceInternalRomValue(0xc010, 0x80)
                }
            }

            pulseManager.addListener(dc)
            val a2Memory = Apple2Memory()
            swtContext = createWindows(a2Memory, keyProvider)
            val textPanel =  TextPanel(swtContext.textScreen)

            val computer = Computer.create {
                memory = a2Memory
                memoryListeners.add(Apple2MemoryListener(textPanel, swtContext.hiResWindow))
                memoryListeners.add(dc)
            }.build()
            val start = a2Memory.word(0xfffc) // memory[0xfffc].or(memory[0xfffd].shl(8))
            computer.cpu.PC = start
            loadPic(a2Memory)

            pulseManager.addListener(computer)
            Thread {
                fw.run(a2Memory)
            }.start()
        }
        else -> {
            pulseManager.addListener(functionalTestComputer(false))
//            with(result) {
//                val sec = durationMillis / 1000
//                val mhz = String.format("%.2f", cycles / sec / 1_000_000.0)
//                println("Computer stopping after $cycles cycles, $sec seconds, $mhz MHz")
//            }
        }
    }

//    Thread {
//        pulseManager.run()
//    }.start()

    swtContext?.run()
    fw.stop = true
}

private fun loadPic(memory: IMemory) {
    val bytes = Paths.get("d:", "PD", "Apple disks", "fishgame.pic").toFile().readBytes()
    (4..0x2004).forEach {
        memory[0x2000 + it - 4] = bytes[it].toInt()
    }
}
