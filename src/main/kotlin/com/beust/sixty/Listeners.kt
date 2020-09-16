package com.beust.sixty

class DebugMemoryListener(private val debugMemory: Boolean = false) : MemoryListener() {
    fun logMem(i: Int, value: Int, extra: String = "") {
        lastMemDebug.add("mem[${i.hh()}] = ${(value.and(0xff)).h()} $extra")
    }

    override fun isInRange(address: Int) = true

    override fun onWrite(location: Int, value: Int) {
        if (location == 0x3f) {
            println("3f modified: " + value)
        }
        if (debugMemory && ! (location in 0x100 .. 0x1ff)) logMem(location, value)
    }

//    override fun onRead(location: Int, value: Int) {
//        if (debugMemory)
//            logMem(location, value)
//    }
}