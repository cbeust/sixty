@file:Suppress("UnnecessaryVariable")

package com.beust.app

import com.beust.sixty.*
import java.awt.Dimension
import java.nio.file.*
import javax.swing.GroupLayout
import javax.swing.JFrame


class Apple2Frame: JFrame() {
    val textScreenPanel: TextScreenPanel
    val hiresPanel: HiResScreenPanel

    init {
        val layout = GroupLayout(contentPane)
        contentPane.layout = layout
        title = "Cédric's Apple ][ emulator"

        isVisible = true //making the frame visible
        setSize(1000, 1000)

        val w = HiResScreenPanel.WIDTH * 2
        val h = HiResScreenPanel.HEIGHT * 2

        textScreenPanel = TextScreenPanel().apply {
            preferredSize = Dimension(w, h)
            setSize(w, h)
        }
        hiresPanel = HiResScreenPanel().apply {
            preferredSize = Dimension(w, h)
            setSize(w, h)
        }

        layout.apply {
            autoCreateGaps = true
            autoCreateContainerGaps = true
            setHorizontalGroup(createSequentialGroup()
                    .addComponent(textScreenPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Int.MAX_VALUE)
                    .addComponent(hiresPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Int.MAX_VALUE)
            )
            setVerticalGroup(createParallelGroup()
                    .addComponent(textScreenPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Int.MAX_VALUE)
                    .addComponent(hiresPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Int.MAX_VALUE)
            )
            pack()
        }

    }
}

fun createApple2Memory(): Memory {
    val result = Memory(65536).apply {

//        load("d:\\pd\\Apple Disks\\roms\\APPLE2E.ROM", 0xc000)
//        load("d:\\pd\\Apple Disks\\roms\\C000.dump", 0xc000)
        loadResource("Apple2e.rom", 0xd000, 0x1000, 0x3000)

        internalCxRom = true
        slotC3Rom = false
        // Load C100-C2FF in internal rom
        loadResource("Apple2e.rom", 0xc100, 0x100, 0x200)
        // Load C800-CFFF in internal rom
        loadResource("Apple2e.rom", 0xc800, 0x800, 0x800)
        internalCxRom = false
        slotC3Rom = true
        // Load C300-C3FF in slot rom
        loadResource("Apple2e.rom", 0xc300, 0x300, 0x100)
//        slotC3Rom = false
//        loadResource("Apple2_Plus.rom", 0xd000)
        loadResource("DISK2.ROM", 0xc600)
//        loadResource("DISK2.ROM", 0xc500)

        // Reset
        internalCxRom = false
        slotC3Rom = false
        Thread {
            runWatcher(this)
        }.start()

        // When restarting, no need to move the head 0x50 tracks
        this[0xc63c] = 4
    }

    return result
}

fun apple2Computer(debugMem: Boolean, diskController: DiskController): Computer {
    val memory = createApple2Memory()
    val frame = Apple2Frame().apply {
        addKeyListener(object : java.awt.event.KeyListener {
            override fun keyReleased(e: java.awt.event.KeyEvent?) {}
            override fun keyTyped(e: java.awt.event.KeyEvent?) {}

            override fun keyPressed(e: java.awt.event.KeyEvent) {
                val key = when (e.keyCode) {
                    10 -> 0x8d
                    else -> {
                        val result = e.keyCode.or(0x80)
                        println("Result: " + result.h() + " " + result.toChar())
                        result
                    }
                }
                memory.forceValue(0xc000, key)
                memory.forceValue(0xc010, 0x80)
            }
        })
    }

//    val listener = Apple2MemoryListener(frame.textScreenPanel, frame.hiresPanel) { -> debugMem }
//    val pcListener = Apple2PcListener()
//    val interceptor = Apple2MemoryInterceptor()

    val appleCpu = Cpu(memory = memory)
    val result = Computer(cpu = appleCpu)
//    listener.computer = result
//    interceptor.computer = result
//    pcListener.computer = result

    with(memory) {
        listeners.add(diskController)
//        listeners.add(DiskController(5, DISK_DOS_3_3))
        listeners.add(DebugMemoryListener(memory))
        listeners.add(ScreenListener(this, frame.textScreenPanel, frame.hiresPanel))
    }

    result.apply {
//        memory.listener = listener
//        memory.interceptor = interceptor
//            fillScreen(memory)
//            fillWithNumbers(memory)
//        memory[0x2027] = 0xdd
//            loadPic(memory)
        val start = memory[0xfffc].or(memory[0xfffd].shl(8))
        cpu.PC = start
//                run()
    }

    return result
}

private fun runWatcher(memory: Memory) {
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
                    memory.load(file.toFile().inputStream(), 0x300)
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
private fun loadPic(memory: Memory) {
    val bytes = Paths.get("d:", "PD", "Apple disks", "fishgame.pic").toFile().readBytes()
    (4..0x2004).forEach {
        memory[0x2000 + it - 4] = bytes[it].toInt()
    }
}

