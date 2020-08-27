package com.beust.app

import com.beust.sixty.Computer
import com.beust.sixty.Memory
import com.beust.sixty.MemoryListener
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.File
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
            when(event.code) {
                KeyCode.Q -> {
                    stage.close()
                }
            }
         }

//        val canvas = root.lookup("#canvas") as Canvas
        val canvas = Canvas(800.0, 400.0)
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
        val listener = object: MemoryListener {
            override fun onRead(location: Int, value: Int) {
            }

            override fun onWrite(location: Int, value: Int) {
                if (location >= 0x400 && location < 0x7ff) {
                    textScreen.drawMemoryLocation(location, value)
                }
            }

        }
        with(Computer(memory = Memory(size = 65536))) {
            memory.listener = listener
//            fillScreen(memory)
//            fillWithNumbers(memory)
            loadPic(memory)
            run()
        }
    }

    private fun loadPic(memory: Memory) {
        val bytes = Paths.get("d:", "PD", "Apple disks", "fishgame.pic").toFile().readBytes()
        (4..bytes.size - 1).forEach {
            memory.setByte(it - 4, bytes[it].toInt())
        }
    }

    private fun fillWithNumbers(memory: Memory) {
        (0..40).forEach { x ->
            (0..24).forEach { y ->
                memory.setByte(0x400 + (y * 40) + x, (x % 10) + '0'.toInt())
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

class HiResScreen(private val canvas: Canvas) {
    private val width = 140
    private val height = 192
    private val blockWidth = 5
    private val blockHeight = 5
    private val gap = 2
    private val fullWidth = (blockWidth + gap) * width + 40
    private val fullHeight = (blockHeight + gap) * height + 40
    private val interleaving = listOf(
            0, 0x80, 0x100, 0x180, 0x200, 0x280, 0x300, 0x380,
            0x28, 0xa8, 0x128, 0x1a8, 0x228, 0x2a8, 0x328, 0x3a8,
            0x50, 0xd0, 0x150, 0x1d0, 0x250, 0x2d0, 0x350, 0x3d0)

    init {
        with(canvas.graphicsContext2D) {
            fill = Color.BLACK
            fillRect(0.0, 0.0, fullWidth.toDouble(), fullHeight.toDouble())
        }
    }

    fun drawMemoryLocation(location: Int, value: Int) {
    }
}

class TextScreen(private val canvas: Canvas) {
    private val width = 40
    private val height = 24
    private val fontWidth = 10
    private val fontHeight = 10
    private val gap = 5
    private val fullWidth = (fontWidth + gap) * width + 40
    private val fullHeight = (fontHeight + gap) * height + 40

    init {
        with(canvas.graphicsContext2D) {
            fill = Color.BLACK
            fillRect(0.0, 0.0, fullWidth.toDouble(), fullHeight.toDouble())
        }
    }

    fun drawMemoryLocation(location: Int, value: Int) {
        val z = location - 0x400
        val x = z % width
        val y = z / width
        drawCharacter(x, y, value.toChar())
    }

    private fun drawCharacter(x: Int, y: Int, character: Char) {
        if (x < width && y < height) {
            val xx = x * (fontWidth + gap)
            val yy = y * (fontHeight + gap)
            with(canvas.graphicsContext2D) {
                fill = Color.WHITE
                fillText(character.toString(), xx.toDouble(), yy.toDouble())
            }
        }
    }
}