@file:Suppress("UnnecessaryVariable")

package com.beust.app

import com.beust.sixty.Computer
import com.beust.sixty.Cpu
import com.beust.sixty.Memory
import com.beust.sixty.MemoryInterceptor
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.awt.Color
import java.awt.Graphics
import java.nio.file.Paths
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.system.exitProcess


class ScreenPanel: JPanel() {
    data class DW(val string: String, val x: Int, val y: Int)
    private val content = Array(TextScreen.width * TextScreen.height) { 0x20 }
    private val fontWidth = 10
    private val fontHeight = 10
    private val gap = 5
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

    init {
        layout = null //using no layout managers
        isVisible = true //making the frame visible
        setSize(1000, 800)

        screenPanel = ScreenPanel().apply {
            background = Color.RED
        }
        add(screenPanel)
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
//    val graphicsScreen = HiResScreen(Apple2App.canvas)

    val listener = Apple2MemoryListener(textScreen) { -> debugMem }
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
//            loadPic(memory)
        val start = memory[0xfffc].or(memory[0xfffd].shl(8))
//        disassemble(0, 20)
        cpu.PC = start
//                run()
    }

    return result
}

class Apple2App : Application() {
//    companion object {
//        private var _canvas: Canvas? = null
//        val canvas: Canvas by lazy { _canvas }
//    }

    override fun start(stage: Stage) {
        stage.title = "Main"
        stage.onCloseRequest = EventHandler {
            Platform.exit()
            exitProcess(0)
        }
        val url = this::class.java.classLoader.getResource("main.fxml")
        val loader = FXMLLoader(url)
        val res = url.openStream()
        val root = loader.load<AnchorPane>(res)
        val scene = Scene(root)
        stage.scene = scene

        scene.setOnKeyPressed { event: KeyEvent ->
            when (event.code) {
                KeyCode.Q -> {
                    stage.close()
                }
            }
        }

        canvas = root.lookup("#canvas") as Canvas
//        _canvas = Canvas(1000.0, 600.0)
//        root.children.add(_canvas)

        stage.show()
    }

    companion object {
        var canvas: Canvas? = null
    }
}
private fun loadPic(memory: Memory) {
    val bytes = Paths.get("d:", "PD", "Apple disks", "fishgame.pic").toFile().readBytes()
    (4..bytes.size - 1).forEach {
        memory[0x2000 + it - 4] = bytes[it].toInt()
    }
}

private fun fillWithNumbers(memory: Memory) {
    (0..40).forEach { x ->
        (0..24).forEach { y ->
            memory[0x400 + (y * 40) + x] = (x % 10) + '0'.toInt()
        }
    }
}

private fun fillScreen(memory: Memory) {
    memory.init(0, 0x4c, 5, 0, 0xea, 0xea,
            0xa9, 0x00, // LDA #0
            0x85, 0x3,  // STA 3
            0xa9, 0x04, // LDA #2
            0x85, 0x4,  // STA 4

            0xa0, 0x0, // LDY 0
            0xa9, 0x40, // LDA #$28
            0x91, 0x3, // STA ($03),Y
            0xc8, // INY
            0xd0, 0xf9, // BNE $5
            0xe6, 0x4, // INC $04
            0xa5, 0x4,  // LDA $04
            0xc9, 0x08, // CMP #$03
            0x90, 0xf1, // BCC $5
            0x60)
}

