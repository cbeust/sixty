@file:Suppress("UnnecessaryVariable")

package com.beust.app

import com.beust.sixty.*
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
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

        // When restarting, don't move the head 50 tracks, only 1
        this[0xc63c] = 4
    }

    val listener = object: MemoryListener() {
        fun logMem(i: Int, value: Int, extra: String = "") {
            lastMemDebug.add("mem[${i.hh()}] = ${(value.and(0xff)).h()} $extra")
        }

        override fun onWrite(location: Int, value: Int) {
            if (location == 0x48f && value == 0xc1) {
                println("FOUND AN A")
            }
            if (location in 0x400..0x740 && value in 0xc1..0xda) {
                println("Drawing text: "+ value.and(0xff).toChar())
                ""
//                textScreen.drawMemoryLocation(location, value)
            } else if (location in 0x2000..0x3fff) {
//                if (value != 0) println("Graphics: [$" + location.hh() + "]=$" + value.and(0xff).h())
//                graphicsScreen.drawMemoryLocation(memory, location, value)
            } else if (location == 0x36) {
                if (value == 0x30) {
                    println("Watching 36")
                }
            } else if (location in 0x300..0x3ff) {
//                println("mem[${location.hh()}]=${value.h()}")
            } else when(location) {
                0xc054 -> {} // LOWSCR
                0xc056 -> {} // LORES
                0x2e -> {
                    println("----- Reading track $" + memory[0x2e].h())
                    ""
                }
                0x2d -> {
                    println("-----     Reading sector $" + memory[0x2d].h())
                    ""
                }
                0x42 -> {
                    if (value == 0x36) {
                        println("Writing $36 in $42")
                        ""
                    }
                }
                else -> {}
            }

            if (debugMem) logMem(location, value)
        }

    }
    val pcListener = object: PcListener {
        override fun onPcChanged(c: Computer) {
            val newValue = c.cpu.PC
            when(newValue) {
                0xc28b -> {
                    println("Waiting for key")
                }
//                0xc67d -> {
//                    println("Decoding data")
//                }
//                0xc699 -> {
//                    println("Testing carry")
//                }
//                0xc6a6 -> {
//                    println("Decoding data sector $" + memory[0x3d].h() + " into " + c.word(address = 0x26).hh())
//                    ""
//                }
//                0x822 -> {
//                    println("BMI for X: " + c.cpu.X)
//                }
//                0x829 -> {
//                    println("Decrementing $8ff: " + memory[0x8ff].hh())
//                }
//                0x836 -> {
//                    println("Reading next sector: "+ memory[0x3d].h())
//                    ""
//                }
//                0x839 -> {
//                    println("Next part of stage 2")
//                }
//                0xc6ed -> {
//                    println("Incrementing $3d: " + memory[0x3d].h())
//                }
                0xaca3 -> {
                    println("Storing potentially in $36 [$42]=" + memory[0x42].h() + memory[0x43].h())
                    ""
                }
                0x9fc5 -> {
                    // TODO
                    memory[0x36] = 0xf0
                    memory[0x37] = 0xfd
                }
                0x9eba -> {
                    // TODO
                    memory[0x38] = 0x1b
                    memory[0x39] = 0xfd

                }
            }
        }
    }

    val appleCpu = Cpu(memory = memory)
    val result = Computer(cpu = appleCpu, pcListener = pcListener)

    val interceptor = object: MemoryInterceptor {
        override val computer = result
        val disk = WozDisk(Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz").openStream())

        override fun onRead(location: Int, value: Int): MemoryInterceptor.Response {
            val result = when (location) {
                in StepperMotor.RANGE -> StepperMotor.onRead(location, value, disk)
                in SoftDisk.RANGE -> SoftDisk.onRead(location, value, disk)
                in SoftSwitches.RANGE -> SoftSwitches.onRead(location, value)
                else -> MemoryInterceptor.Response(true, value)
            }
            return result
        }

        override fun onWrite(location: Int, value: Int): MemoryInterceptor.Response {
            var result = MemoryInterceptor.Response(true, value)

            if (location in SoftSwitches.RANGE) {
                result = SoftSwitches.onWrite(location, value)
            } else if (location == 0x36 || location == 0x37) {
                if (location == 0x37 && value != 0xfd) {
                    println("Should not write here")
                    result = MemoryInterceptor.Response(true, value)
                } else if (value != 189 && value != 240) {
                    println("Writing to CSWL: $value")
                    return MemoryInterceptor.Response(true, value)
                    result = MemoryInterceptor.Response(true, value)
                }
            }
            return result
        }
    }

    result.apply {
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

