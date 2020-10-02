package com.beust.app

import com.beust.app.app.TextPanel
import com.beust.sixty.*
import com.beust.swt.ACTUAL_HEIGHT
import com.beust.swt.SwtContext
import com.beust.swt.createWindows
import org.eclipse.swt.widgets.Control
import java.io.File
import java.nio.file.Paths

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
        DISK_DOS_3_3, WOZ_DOS_3_3, File("src/test/resources/audit.dsk"),
        File("disks/Blade_of_Blackpoole_A.dsk"), // 3
        File("disks/Sherwood_Forest.dsk") ,       // 4
        File("d:/pd/Apple disks/Ultima I - The Beginning.woz"), // 5
        File("disks/Rescue Raiders.dsk") // 6
)

val DISK = disks[6]
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
                loadDisk(IDisk.create(DISK), 0)
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

            fun maybeResize(control: Control) {
                if (control == swtContext.hiResWindow || control == swtContext.hiRes2Window) {
                    control.display.asyncExec {
                        val fullHeight = ACTUAL_HEIGHT
                        val shortHeight = ACTUAL_HEIGHT * 5 / 6
                        val b = control.bounds
                        val newHeight = if (UiState.mainScreenMixed.value) shortHeight else fullHeight
                        control.setBounds(b.x, b.y, b.width, newHeight)
                        control.parent.layout()
                    }
                }
            }

            fun show(control: Control, old: Control?) {
                if (! control.isDisposed) with(control) {
                    display.asyncExec {
                        maybeResize(control)
                        swtContext.show(this)
                    }
                }
            }

            UiState.mainScreenHires.addListener { _, new ->
                if (UiState.mainScreenPage2.value) show(swtContext.hiRes2Window, swtContext.hiResWindow)
                    else show(swtContext.hiResWindow, swtContext.hiRes2Window)
            }
            UiState.mainScreenPage2.addListener { _, new ->
                if (!a2Memory.store80On) {
                    if (UiState.mainScreenText.value) {
                        //                    if (UiState.mainScreenPage2.value) show(new, textScreen2)
                        //                        else
                        show(swtContext.textScreen, null)
                    } else {
                        println("Page2 changed to $new, its value is " + UiState.mainScreenPage2.value)
                        if (UiState.mainScreenPage2.value) show(swtContext.hiRes2Window, swtContext.hiResWindow)
                        else show(swtContext.hiResWindow, swtContext.hiRes2Window)
                    }
                }
            }
            UiState.mainScreenText.addListener { _, new ->
//                if (UiState.mainScreenPage2) show(new, textScreen2)
//                else
                if (new) show(swtContext.textScreen, null)
            }
            UiState.mainScreenMixed.addListener { _, new ->
                val window = if (UiState.mainScreenPage2.value) swtContext.hiRes2Window else swtContext.hiResWindow
                maybeResize(window)
            }

            val textPanel1 =  TextPanel(0x400, swtContext.textScreen)

            val computer = Computer.create {
                memory = a2Memory
                memoryListeners.add(Apple2MemoryListener(a2Memory, textPanel1, swtContext.hiResWindow))
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

    if (RUN) {
        Thread {
            pulseManager.run()
        }.start()
    }
    swtContext?.run()
    fw.stop = true
    pulseManager.stop()
}

private fun loadPic(memory: IMemory) {
//    val bytes = Paths.get("d:", "PD", "Apple disks", "fishgame.pic").toFile().readBytes()
//    (4..0x2004).forEach {
//        memory[0x2000 + it - 4] = bytes[it].toInt()
//    }
}
