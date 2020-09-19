package com.beust.sixty

class DebugMemoryListener(val memory: Memory, private val debugMemory: Boolean = false) : MemoryListener() {
    private fun logMem(i: Int, value: Int, extra: String = "") {
        logLines.add("  mem[${i.hh()}] = ${(value.and(0xff)).h()} $extra " + memory.lastMem)
        ""
    }

    override fun isInRange(address: Int) = true

    override fun onWrite(location: Int, value: Int) {
        if (location == 0xd17b) {
//        if (true && ! (location in 0x100 .. 0x1ff))
            logMem(location, value)
//        if (location == 0xd17b && value != 0x44) {
//            logMem("BUG HERE D17B")
        }
    }

//    override fun onRead(location: Int, value: Int) {
//        if (debugMemory)
//            logMem(location, value)
//    }
}