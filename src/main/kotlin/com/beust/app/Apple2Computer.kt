@file:Suppress("UnnecessaryVariable")

package com.beust.app

import com.beust.sixty.Computer
import com.beust.sixty.Cpu
import com.beust.sixty.Memory
import java.awt.Color
import java.nio.file.Paths
import javax.swing.JFrame


class Apple2Frame: JFrame() {
    val textScreenPanel: TextScreenPanel
    val hiresPanel: HiResScreenPanel

    init {
        layout = null //using no layout managers
        isVisible = true //making the frame visible
        setSize(1000, 1000)

        val w = TextScreenPanel.fullWidth
        val h = TextScreenPanel.fullHeight

        textScreenPanel = TextScreenPanel().apply {
            setBounds(0, 0, w, h)
        }
        add(textScreenPanel)
        hiresPanel = HiResScreenPanel().apply {
            setBounds(w + 10, 0, w, h)
        }
        add(hiresPanel)
    }
}

fun apple2Computer(debugMem: Boolean): Computer {
    val memory = Memory(65536).apply {

//        load("d:\\pd\\Apple Disks\\roms\\APPLE2E.ROM", 0xc000)
//        load("d:\\pd\\Apple Disks\\roms\\C000.dump", 0xc000)
//        loadResource("Apple2e.rom", 0xc000)
        loadResource("Apple2_Plus.rom", 0xd000)
        loadResource("DISK2.ROM", 0xc600)

        // When restarting, no need to move the head 0x50 tracks
        this[0xc63c] = 4
    }

    val frame = Apple2Frame().apply {
        addKeyListener(object: java.awt.event.KeyListener {
            override fun keyReleased(e: java.awt.event.KeyEvent?) {}
            override fun keyTyped(e: java.awt.event.KeyEvent?) {}

            override fun keyPressed(e: java.awt.event.KeyEvent) {
                val key = when(e.keyCode) {
                    10 -> 0x8d
                    else -> e.keyCode.or(0x80)
                }
                memory.forceValue(0xc000, key)
                memory.forceValue(0xc010, 0x80)
            }
        })
    }

    val listener = Apple2MemoryListener(frame.textScreenPanel, frame.hiresPanel) { -> debugMem }
//    val pcListener = Apple2PcListener()
    val interceptor = Apple2MemoryInterceptor()

    val appleCpu = Cpu(memory = memory)
    val result = Computer(cpu = appleCpu)
    listener.computer = result
//    interceptor.computer = result
//    pcListener.computer = result

    result.apply {
        memory.listener = listener
//        memory.interceptor = interceptor
//            fillScreen(memory)
//            fillWithNumbers(memory)
            loadPic(memory)
        val start = memory[0xfffc].or(memory[0xfffd].shl(8))
        cpu.PC = start
//                run()
    }

    return result
}

private fun loadPic(memory: Memory) {
    val bytes = Paths.get("d:", "PD", "Apple disks", "fishgame.pic").toFile().readBytes()
    (4..0x2004).forEach {
        memory[0x2000 + it - 4] = bytes[it].toInt()
    }
}

