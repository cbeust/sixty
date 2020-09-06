package com.beust.app

import com.beust.sixty.*
import javafx.application.Application
import javafx.application.Application.launch
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

fun apple2Computer(debugMem: Boolean): Computer {
//    val textScreen = TextScreen(Apple2App.canvas)
//    val graphicsScreen = HiResScreen(Apple2App.canvas)
    val memory = Memory(65536).apply {
//        load("d:\\pd\\Apple Disks\\roms\\APPLE2E.ROM", 0xc000)
//        load("d:\\pd\\Apple Disks\\roms\\C000.dump", 0xc000)
        loadResource("Apple2e.rom", 0xc000)
        loadResource("DISK2.ROM", 0xc600)
        this[0xfcac] = BEQ
        this[0xfcb1] = BEQ
//        load("d:\\pd\\Apple Disks\\dos", 0x9600)
//        load("d:\\pd\\Apple Disks\\a000.dmp", 0)
        // JMP $C600
//        this.init(pc, 0x4c, 0, 0xc6)
//        // jsr $fded
//        this.init(pc, 0xa9, 0x41, 0x20, 0xed, 0xfd, 0x60)
        // Draw a line
//        this.init(pc, JSR, 0xe2, 0xf3, // hgr
//                LDX_IMM, 3,   // a2
//                JSR, 0xf0, 0xf6, // HCOLOR (set color to white)
//                LDA_IMM, 0,   // a9
//                TAY,          // a8
//                TAX,          // aa
//                JSR, 0x57, 0xf4,  // HPLOT
//                LDA_IMM, 0x17, // a9
//                LDX_IMM, 1,    // a2
//                JSR, 0x3a, 0xf5  // HLINE TO 279,0
//        )
//        this[0x36] = 0xbd
//        this[0x37] = 0x9e
    }

    val interceptor = object: MemoryInterceptor {
        val woz = Woz(Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz").openStream())

        override fun onRead(location: Int, value: Int): MemoryInterceptor.Response {
            val byte = when(location) {
                0xc0e8 -> {
                    println("Turning motor off")
                    value
                }
                0xc0e9 -> {
                    println("Turning motor on")
                    value
                }
                0xc0ea -> {
                    println("Turning on drive 1")
                    value
                }
                0xc0eb -> {
                    println("Turning on drive 2")
                    value
                }
                0xc0ec -> {
                    val result = woz.bitStream.nextByte()
                    if (result == 0xd5) {
                        println("Woz returning byte ${result.h()}")
                    }
                    result
                }
                else -> value
            }
            return MemoryInterceptor.Response(true, byte)
//            if (location >= 0xc080 && location <= 0xc0ff) {
//                return MemoryInterceptor.Response(true, 0xd5)
//            } else {
//                return MemoryInterceptor.Response(false, value)
//            }
        }

        override fun onWrite(location: Int, value: Int): MemoryInterceptor.Response {
            return MemoryInterceptor.Response(true, value)
        }

    }
    val listener = object: MemoryListener(debugMem) {
        fun logMem(i: Int, value: Int, extra: String = "") {
            lastMemDebug.add("mem[${i.hh()}] = ${(value.and(0xff)).h()} $extra")
        }

        override fun onWrite(location: Int, value: Int) {
            if (location >= 0x400 && location < 0x7ff) {
//                println("Drawing text: "+ value.and(0xff).toChar())
//                textScreen.drawMemoryLocation(location, value)
            } else if (location >= 0x2000 && location <= 0x3fff) {
//                if (value != 0) println("Graphics: [$" + location.hh() + "]=$" + value.and(0xff).h())
//                graphicsScreen.drawMemoryLocation(memory, location, value)
            } else when(location) {
                0xc054 -> {} // LOWSCR
                0xc056 -> {} // LORES
                else -> {}
            }

            if (debugMem) logMem(location, value)
        }

    }

    val appleCpu = Cpu2(memory = memory)
    val result = Computer(cpu = appleCpu).apply {
        memory.listener = listener
        memory.interceptor = interceptor
//            fillScreen(memory)
//            fillWithNumbers(memory)
//            loadPic(memory)
        if (false) {
            memory[0] = 0
        }
        val start = memory[0xfffc].or(memory[0xfffd].shl(8))
//        disassemble(0, 20)
        cpu.PC = start
//                run()
    }

//    Application.launch(Apple2App::class.java)

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

//        val canvas = root.lookup("#canvas") as Canvas
//        _canvas = Canvas(1000.0, 600.0)
//        root.children.add(_canvas)

        stage.show()

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

