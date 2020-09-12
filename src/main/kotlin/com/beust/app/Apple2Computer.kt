@file:Suppress("UnnecessaryVariable")

package com.beust.app

import com.beust.sixty.Computer
import com.beust.sixty.Cpu
import com.beust.sixty.Memory
import java.awt.Color
import java.awt.Graphics
import java.nio.file.Paths
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.system.exitProcess


class ScreenPanel: JPanel() {
    data class DW(val string: String, val x: Int, val y: Int)
    private val content = Array(TextScreen.width * TextScreen.height) { 0x20 }
    private val fontWidth = 10
    private val fontHeight = 10
    private val gap = 2
    private val fullWidth = (fontWidth + gap) * TextScreen.width + 40
    private val fullHeight = (fontHeight + gap) * TextScreen.height + 40

    init {
        setBounds(0, 0, fullWidth, fullHeight)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.color = Color.black
        g.fillRect(0, 0, fullWidth, fullHeight)
        g.color = Color.green
        repeat(TextScreen.height) { y ->
            repeat(TextScreen.width) { x ->
                val xx = x * (fontWidth + gap)
                val yy = y * (fontHeight + gap) + 15
                val c = content[y * TextScreen.width + x].toChar().toString()
//                println("Drawing at ($x, $y) ($xx,$yy): $character")
                g.drawString(c, xx, yy)
            }
        }
    }

    fun drawCharacter(x: Int, y: Int, value: Int) {
        if (value in 0xa0..0xfe) {
            content[y * TextScreen.width + x] = value and 0x7f
            repaint()
        }
    }

}

class Apple2Frame: JFrame() {
    val screenPanel: ScreenPanel
    val hiresPanel: HiResScreen

    init {
        layout = null //using no layout managers
        isVisible = true //making the frame visible
        setSize(1000, 800)

        screenPanel = ScreenPanel().apply {
            background = Color.RED
        }
        add(screenPanel)
        hiresPanel = HiResScreen().apply {
//            background = Color.BLUE
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
    val textScreen = TextScreen(frame.screenPanel)

    val listener = Apple2MemoryListener(textScreen, frame.hiresPanel) { -> debugMem }
//    val pcListener = Apple2PcListener()
    val interceptor = Apple2MemoryInterceptor()

    val appleCpu = Cpu(memory = memory)
    val result = Computer(cpu = appleCpu)
    listener.computer = result
    interceptor.computer = result
//    pcListener.computer = result

    result.apply {
        memory.listener = listener
//        memory.interceptor = interceptor
//            fillScreen(memory)
//            fillWithNumbers(memory)
            loadPic(memory)
        val start = memory[0xfffc].or(memory[0xfffd].shl(8))
//        disassemble(0, 20)
        cpu.PC = start
//                run()
    }

    return result
}

private fun loadPic(memory: Memory) {
    val bytes = Paths.get("d:", "PD", "Apple disks", "fishgame.pic").toFile().readBytes()
    (4..bytes.size - 1).forEach {
        memory[0x2000 + it - 4] = bytes[it].toInt()
    }
}

