package com.beust.sixty

class DebugMemoryListener(val debugMemory: Boolean = false) : MemoryListener() {
    fun logMem(i: Int, value: Int, extra: String = "") {
        lastMemDebug.add("mem[${i.hh()}] = ${(value.and(0xff)).h()} $extra")
    }

    override fun onWrite(location: Int, value: Int) {
        if (debugMemory && ! (location in 0x100 .. 0x1ff)) logMem(location, value)
    }

//    override fun onRead(location: Int, value: Int) {
//        if (debugMemory)
//            logMem(location, value)
//    }
}