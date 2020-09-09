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
//        this[0xfcac] = BEQ
//        this[0xfcb1] = BEQ
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

    val listener = object: MemoryListener(debugMem) {
        fun logMem(i: Int, value: Int, extra: String = "") {
            lastMemDebug.add("mem[${i.hh()}] = ${(value.and(0xff)).h()} $extra")
        }

        override fun onWrite(location: Int, value: Int) {
            if (location in 0x400..1142 && value in 0x41..0x5a) {
                println("Drawing text: "+ value.and(0xff).toChar())
                ""
//                textScreen.drawMemoryLocation(location, value)
            } else if (location in 0x2000..0x3fff) {
//                if (value != 0) println("Graphics: [$" + location.hh() + "]=$" + value.and(0xff).h())
//                graphicsScreen.drawMemoryLocation(memory, location, value)
            } else if (location in 0x300..0x3ff) {
//                println("mem[${location.hh()}]=${value.h()}")
            } else when(location) {
                0xc054 -> {} // LOWSCR
                0xc056 -> {} // LORES
                0x2e -> {
                    println("Reading track " + memory[0x2e].h())
                    ""
                }
                0x2d -> {
                    println("Reading sector " + memory[0x2d])
                    ""
                }
                0x478 -> {
                    val t = memory[0x478]
                    if (t == 0xa0) {
                        println("STOP")
                    }
                    println("---------------------------------------- Moving target track to " + t)
                    ""
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
                0xc697 -> with(c) {
                    when(cpu.Y) {
//                        2 -> println("Read volume ${cpu.A}")
//                        1 -> println("Read track ${cpu.A}")
                        0 -> {
                            println("Read sector ${cpu.A}")
                            ""
                        }
                        else -> {}

                    }
                }
//                0xc67d -> {
//                    println("Decoding data")
//                }
//                0xc699 -> {
//                    println("Testing carry")
//                }
                0xc6a6 -> {
                    println("Decoding data sector $" + memory[0x3d].h() + " into " + c.word(address = 0x26).hh())
                    ""
                }
//                0x822 -> {
//                    println("BMI for X: " + c.cpu.X)
//                }
//                0x829 -> {
//                    println("Decrementing $8ff: " + memory[0x8ff].hh())
//                }
                0x836 -> {
                    println("Reading next sector: "+ memory[0x3d].h())
                    ""
                }
                0x839 -> {
                    println("Next part of stage 2")
                }
                0xc6ed -> {
                    println("Incrementing $3d: " + memory[0x3d].h())
                }
//                0xc6e9 -> {
//                    if (c.cpu.Y == 0) {
//                        println("Incrementing Y: " + c.cpu.Y)
//                    }
//                }
//                0xc6da -> {
//                    if (c.cpu.X == 0) {
//                        println("Decrementing x: " + c.cpu.X)
//                    }
//                }
//                0xc6a6 -> {
//                    println("Decoding data")
//                }
//                0xc6f6 -> {
//                    println("Done decoding at address " + c.word(address = 0x26).hh())
//                    ""
//                }
//                0xc6e9 -> {
//                    println("Incrementing Y: " + c.cpu.Y)
//                    ""
//                }
//                0xc6eb -> {
//                    println("Incrementing target memory address")
//                    ""
//                }
            }
        }
    }

    val appleCpu = Cpu(memory = memory)
    val result = Computer(cpu = appleCpu, pcListener = pcListener)

    val interceptor = object: MemoryInterceptor {
        override val computer = result
        val disk = WozDisk(Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz").openStream())
        var magnets = BooleanArray(4) { _ -> false }
        var phase = 0

        private fun magnet(index: Int, state: Boolean) {
            if (state) {
                when(phase) {
                    0 -> {
                        if (index == 1) {
                            phase = 1
                            disk.incTrack()
                        } else if (index == 3) {
                            phase = 3
                            disk.decTrack()
                        }
                    }
                    1 -> {
                        if (index == 2) {
                            phase = 2
                            disk.incTrack()
                        } else if (index == 0) {
                            phase = 0
                            disk.decTrack()
                        }
                    }
                    2 -> {
                        if (index == 3) {
                            phase = 3
                            disk.incTrack()
                        } else if (index == 1) {
                            phase = 1
                            disk.decTrack()
                        }
                    }
                    3 -> {
                        if (index == 0) {
                            phase = 0
                            disk.incTrack()
                        } else if (index == 2) {
                            phase = 2
                            disk.decTrack()
                        }
                    }
                }
            }

            println("=== Track: "+ disk.track + " magnet $index=$state")
            magnets[index] = state
        }

        override fun onRead(location: Int, value: Int): MemoryInterceptor.Response {
            val byte = when(location) {
                in 0xc0e0 .. 0xc0e7 -> {
                    // Seek address: $b9a0
                    when (location) {
                        0xc0e0 -> magnet(0, false)
                        0xc0e1 -> magnet(0, true)
                        0xc0e2 -> magnet(1, false)
                        0xc0e3 -> magnet(1, true)
                        0xc0e4 -> magnet(2, false)
                        0xc0e5 -> magnet(2, true)
                        0xc0e6 -> magnet(3, false)
                        0xc0e7 -> magnet(3, true)
                        else -> ""
                    }
                    value
                }
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
//                    val v = if (value and 0x80 != 0) 0 else value
//                    val result = v.shl(1).or(disk.nextBit()).and(0xff)
//                    if (result == 0xd5 || result == 0x96 || result == 0xad) {
//                        val rh = result.h()
//                        println("MAGIC: $result")
//                    }
                    val result = disk.nextByte()
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

