@file:Suppress("UnnecessaryVariable")

package com.beust.app

import com.beust.sixty.*
import java.nio.file.*


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
