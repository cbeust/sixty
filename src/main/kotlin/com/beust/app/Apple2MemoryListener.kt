package com.beust.app

import com.beust.sixty.*

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