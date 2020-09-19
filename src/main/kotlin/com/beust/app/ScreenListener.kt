package com.beust.app

import com.beust.sixty.Memory
import com.beust.sixty.MemoryListener
import com.beust.sixty.h
import com.beust.sixty.hh

class ScreenListener(private val memory: Memory, private val textScreen: TextScreenPanel,
        private val hiresScreen: HiResScreenPanel): MemoryListener() {
    override fun isInRange(address: Int): Boolean {
        return address in 0x400..0x800 || address in 0x2000..0x6000
    }

//    override fun onWrite(location: Int, value: Int) {
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

    override fun onWrite(location: Int, value: Int) {
        if (location in 0x400..0x7ff) {
            textScreen.drawMemoryLocation(location, value)
//            logLines.add("Writing on the text screen: $" + location.hh() + "=$" + value.h())
        } else if (location in 0x2000..0x4000) {
            hiresScreen.drawMemoryLocation(memory, location)
//            logLines.add("Writing in graphics")
        }
    }

}