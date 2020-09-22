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
//        slotC3Rom = true
//        internalCxRom = true
//        // Load C100-C2FF in internal rom
//        loadResource("Apple2e.rom", 0xc100, 0x100, 0x200)
//        // C400 internal
//        slotC3Rom = true
//        internalCxRom = true
//        loadResource("Apple2e.rom", 0xc400, 0x400, 0x400)
//        // Load C800-CFFF in internal
//        slotC3Rom = true
//        internalCxRom = true
//        loadResource("Apple2e.rom", 0xc800, 0x800, 0x800)
//        // Load C300-C3FF in internal rom
//        slotC3Rom = true
//        internalCxRom = true
//        loadResource("Apple2e.rom", 0xc300, 0x300, 0x100)

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
        this[0xc63c] = 4
    }

    return result
}

class Apple2Computer(val diskController: DiskController): IComputer, IPulse {
    var pcListener: PcListener? = null
    override val memory: IMemory = createApple2Memory()
    override val cpu : Cpu = Cpu(memory)

    private val computer = Computer(memory, cpu, pcListener)

    override fun onPulse(manager: PulseManager) = computer.onPulse(manager)
    override fun stop() = computer.stop()

//    val frame = Apple2Frame().apply {
//        addKeyListener(object : java.awt.event.KeyListener {
//            override fun keyReleased(e: java.awt.event.KeyEvent?) {}
//            override fun keyTyped(e: java.awt.event.KeyEvent?) {}
//
//            override fun keyPressed(e: java.awt.event.KeyEvent) {
//                val key = when (e.keyCode) {
//                    10 -> 0x8d
//                    else -> {
//                        val result = e.keyCode.or(0x80)
//                        println("Result: " + result.h() + " " + result.toChar())
//                        result
//                    }
//                }
//                memory.forceValue(0xc000, key)
//                memory.forceValue(0xc010, 0x80)
//            }
//        })
//    }
//
//    init {
//        with(memory) {
//            listeners.add(diskController)
//            //        listeners.add(DiskController(5, DISK_DOS_3_3))
//            listeners.add(DebugMemoryListener(memory))
//            listeners.add(Apple2MemoryListener(this, frame.textScreenPanel, frame.hiresPanel))
//        }
//
//        val start = memory[0xfffc].or(memory[0xfffd].shl(8))
//        cpu.PC = start
//    }
}

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
private fun loadPic(memory: IMemory) {
    val bytes = Paths.get("d:", "PD", "Apple disks", "fishgame.pic").toFile().readBytes()
    (4..0x2004).forEach {
        memory[0x2000 + it - 4] = bytes[it].toInt()
    }
}

