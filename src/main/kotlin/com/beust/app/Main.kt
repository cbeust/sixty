package com.beust.app

import com.beust.sixty.*
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
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main() {
    Application.launch(Main::class.java)
}

class Main : Application() {

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

//        val canvas = root.lookup("#canvas") as Canvas
        val canvas = Canvas(1000.0, 600.0)
        root.children.add(canvas)

//        root.widthProperty().addListener { e: Observable? ->
//            canvas.width = width
//            blockWidth = (canvas.width / (Display.WIDTH + SPACE)).toInt()
//        }
//        root.heightProperty().addListener { e: Observable? ->
//            canvas.height = height
//            blockHeight = (canvas.height / (Display.HEIGHT + SPACE)).toInt()
//        }


//        with(canvas.parent as AnchorPane) {
//            children.add(canvas)
//            canvas.widthProperty().bind(widthProperty())
//            canvas.heightProperty().bind(heightProperty())
//        }
//        val gc = canvas.graphicsContext2D
//        gc.fill = Color.RED
//        gc.fillText("Hello from text", 10.0, 10.0)

        stage.show()

        val textScreen = TextScreen(canvas)
        val graphicsScreen = HiResScreen(canvas)
        val pc = 0x300
        val memory = Memory(65536).apply {
            load("d:\\pd\\Apple Disks\\apple2eu.rom", 0xc000)
            load("d:\\pd\\Apple Disks\\dos", 0x9600)
            load("d:\\pd\\Apple Disks\\a000.dmp", 0)
            this.init(pc, 0xa9, 0x41, 0x20, 0xed, 0xfd)
            this[0x36] = 0xbd
            this[0x37] = 0x9e
        }

        val listener = object: MemoryListener {
            override fun onRead(location: Int, value: Int) {
            }

            override fun onWrite(location: Int, value: Int) {
                if (location >= 0x400 && location < 0x7ff) {
                    textScreen.drawMemoryLocation(location, value)
                } else if (location >= 0x2000 && location <= 0x3fff) {
                    graphicsScreen.drawMemoryLocation(memory, location, value)
                }
            }

        }
        val appleCpu = Cpu(SP = Apple2StackPointer(memory = memory))
        with(Computer(memory = memory, cpu = appleCpu)) {
            memory.listener = listener
//            fillScreen(memory)
//            fillWithNumbers(memory)
            loadPic(memory)
            if (false) {
                memory[0] = 0
            }
            cpu.PC = pc
//            DEBUG_ASM = true
            DEBUG_MEMORY = true
            run()
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
}

