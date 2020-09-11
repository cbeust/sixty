package com.beust.app

import com.beust.sixty.*

class Apple2MemoryListener(val textScreen: TextScreen, val debugMem: () -> Boolean): MemoryListener() {
    val disk = WozDisk(Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz").openStream())

    lateinit var computer: Computer
    fun logMem(i: Int, value: Int, extra: String = "") {
        lastMemDebug.add("mem[${i.hh()}] = ${(value.and(0xff)).h()} $extra")
    }

    override fun onRead(location: Int, value: Int) {
        when (location) {
            in StepperMotor.RANGE -> StepperMotor.onRead(location, value, disk)
            in SoftDisk.RANGE -> SoftDisk.onRead(location, value, disk)
            in SoftSwitches.RANGE -> SoftSwitches.onRead(computer, location, value)
            else -> value
        }
    }

    override fun onWrite(location: Int, value: Int) {
        val memory = computer!!.memory
        if (location == 0xe000) {
            println("BREAKPOINT")
        }

        if (location == 0x7d0) {
            println("Storing into 0x7d0: " + value.toChar() + " " + value.h())
            ""
        }
        if (location == 0x48f && value == 0xc1) {
            println("FOUND AN A")
        }
        if (location in 0x400..0x7ff) {
            println("Drawing text: "+ value.and(0x7f).toChar())
            textScreen.drawMemoryLocation(location, value)
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

        if (debugMem()) logMem(location, value)
    }

}