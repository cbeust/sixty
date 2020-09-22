package com.beust.app

import com.beust.sixty.DebugMemoryListener
import com.beust.sixty.IMemory
import com.beust.sixty.MemoryListener
import com.beust.sixty.h

class Apple2MemoryListener(private val memory: IMemory): MemoryListener() {
    override fun isInRange(address: Int): Boolean {
        return address in 0x400..0x800 || address in 0x2000..0x6000
    }

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

    init {
//        with(memory) {
//            listeners.add(diskController)
//            //        listeners.add(DiskController(5, DISK_DOS_3_3))
//            listeners.add(DebugMemoryListener(memory))
//            listeners.add(Apple2MemoryListener(this, frame.textScreen, frame.hiresPanel))
//        }

//        val start = memory[0xfffc].or(memory[0xfffd].shl(8))
//        cpu.PC = start
    }

    override fun onWrite(location: Int, value: Int) {
        if (location in 0x400..0x7ff) {
            frame.textScreen.drawMemoryLocation(location, value)
//            logLines.add("Writing on the text screen: $" + location.hh() + "=$" + value.h())
        } else if (location in 0x2000..0x4000) {
            frame.hiresScreen.drawMemoryLocation(memory, location)
//            logLines.add("Writing in graphics")
        }
    }
}
//class Apple2MemoryListener(private val textScreen: TextScreenPanel,
//        private val hiresScreen: HiResScreenPanel,
//        val debugMem: () -> Boolean): MemoryListener() {
//    val disk = WozDisk(Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz").openStream())
//
//    lateinit var computer: Computer
//    fun logMem(i: Int, value: Int, extra: String = "") {
//        lastMemDebug.add("mem[${i.hh()}] = ${(value.and(0xff)).h()} $extra")
//    }
//
//    override fun onRead(location: Int, value: Int) {
//        when (location) {
//            in StepperMotor.RANGE -> StepperMotor.onRead(location, value, disk)
//            in SoftDisk.RANGE -> SoftDisk.onRead(location, value, disk)
//            in SoftSwitches.RANGE -> SoftSwitches.onRead(computer, location, value)
//            else -> value
//        }
//    }
//
//    override fun onWrite(location: Int, value: Int) {
//        val memory = computer!!.memory
//        if (location in 0x400..0x7ff) {
//            textScreen.drawMemoryLocation(location, value)
//        } else if (location in 0x2000..0x3fff) {
////            if (value != 0) println("Graphics: [$" + location.hh() + "]=$" + value.and(0xff).h())
//            hiresScreen.drawMemoryLocation(memory, location)
//        } else {
//            if (location == 0xfe1f) {
//                println("Writing to ${location.hh()}")
//            }
//        }
//
//        if (debugMem()) logMem(location, value)
//    }
//
//}