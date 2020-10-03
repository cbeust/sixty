@file:Suppress("UnnecessaryVariable")

package com.beust.app

import com.beust.app.app.TextPanel
import com.beust.sixty.*
import com.beust.swt.ACTUAL_HEIGHT
import com.beust.swt.SwtContext
import com.beust.swt.createWindows
import org.eclipse.swt.widgets.Control
import java.nio.file.*

class Apple2Computer() {
    fun run(pulseManager: PulseManager): Pair<SwtContext?, IComputer> {
        val fw = FileWatcher()

        val debugMem = false
        val debugAsm = DEBUG
//            frame()
        val dc = DiskController(6).apply {
            loadDisk(IDisk.create(DISK), 0)
            UiState.currentDisk1File.value = DISK
        }
        val keyProvider = object: IKeyProvider {
            override fun keyPressed(memory: IMemory, value: Int, shift: Boolean, control: Boolean) {
                memory.forceInternalRomValue(0xc000, value.or(0x80))
                memory.forceInternalRomValue(0xc010, 0x80)
            }
        }

        pulseManager.addListener(dc)
        val a2Memory = Apple2Memory()
        val swtContext = createWindows(a2Memory, keyProvider)

        fun maybeResize(control: Control) {
            if (control == swtContext.hiResWindow) {
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

        fun show(control: Control) {
            if (! control.isDisposed) with(control) {
                display.asyncExec {
                    maybeResize(control)
                    swtContext.show(this)
                }
            }
        }

        UiState.mainScreenHires.addListener { _, new ->
            if (new) show(swtContext.hiResWindow)
        }
        UiState.mainScreenPage2.addAfterListener { _, _ ->
            if (!a2Memory.store80On) {
                if (UiState.mainScreenText.value) {
                    show(swtContext.textScreen)
                } else {
                    show(swtContext.hiResWindow)
                }
            }
        }
        UiState.mainScreenText.addListener { _, new ->
            if (new) show(swtContext.textScreen)
        }
        UiState.mainScreenMixed.addAfterListener { _, new ->
            maybeResize(swtContext.hiResWindow)
        }

        val textPanel1 =  TextPanel(0x400, swtContext.textScreen)

        val computer = Computer.create {
            memory = a2Memory
            memoryListeners.add(Apple2MemoryListener(a2Memory, textPanel1, swtContext.hiResWindow))
            memoryListeners.add(dc)
        }.build()
        swtContext.computer = computer
        val start = a2Memory.word(0xfffc) // memory[0xfffc].or(memory[0xfffd].shl(8))
        computer.cpu.PC = start
//        loadPic(a2Memory)

        return swtContext to computer
    }

    private fun loadPic(memory: IMemory) {
//    val bytes = Paths.get("d:", "PD", "Apple disks", "fishgame.pic").toFile().readBytes()
//    (4..0x2004).forEach {
//        memory[0x2000 + it - 4] = bytes[it].toInt()
//    }
    }

}

fun createApple2Memory(): Apple2Memory {
    val result = Apple2Memory(65536).apply {

//        load("d:\\pd\\Apple Disks\\roms\\APPLE2E.ROM", 0xc000)
//        load("d:\\pd\\Apple Disks\\roms\\C000.dump", 0xc000)
        loadResource("Apple2e.rom", 0xd000, 0x1000, 0x3000)

        val bytes = this::class.java.classLoader.getResource("Apple2e.rom").openStream().readAllBytes()

        loadCxxxInInternal(bytes, 0x100, 0xeff, 0x100)

        internalCxRom = false
        slotC3Rom = true
        // C600 in slot
        loadResource("DISK2.ROM", 0xc600)

        // Reset
        internalCxRom = false
        slotC3Rom = false
//        slotC3WasReset = false
        internalC8Rom = false
        Thread {
            runWatcher(this)
        }.start()

        // When restarting, no need to move the head 0x50 tracks
//        this[0xc63c] = 4
    }

    return result
}

//class Apple2Computer(val diskController: DiskController): IComputer, IPulse {
//    var pcListener: PcListener? = null
//    override val memory: IMemory = createApple2Memory()
//    override val cpu : Cpu = Cpu(memory)
//
//    private val computer = Computer(memory, cpu, pcListener)
//
//    override fun onPulse(manager: PulseManager) = computer.onPulse(manager)
//    override fun stop() = computer.stop()
//}

private fun runWatcher(memory: IMemory) {
    val watcher = FileSystems.getDefault().newWatchService()

    val dir = Paths.get("asm")
    dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)
    var done = false
    while(! false) {
        val key = watcher.take()
        key.pollEvents().forEach { event ->
            if (event.kind() != StandardWatchEventKinds.OVERFLOW) {
                val ev = event as WatchEvent<Path>
                val filename = ev.context()
                if (filename.toString() == "a") {
                    val file = Paths.get(dir.toAbsolutePath().toString(), filename.toString())
                    println("Reloading $file")
                    memory.load(file.toFile().inputStream().readAllBytes(), 0x300)
                }
                println("$filename modified")
                if (key.isValid) {
                    key.reset()
                } else {
                    done = true
                }
            }
        }
    }
}
