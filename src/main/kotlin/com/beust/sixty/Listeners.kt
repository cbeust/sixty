package com.beust.sixty

object DebugMemoryListener : MemoryListener {
    override val lastMemDebug = arrayListOf<String>()

    fun logMem(i: Int, value: Int, extra: String = "") {
        lastMemDebug.add("mem[${i.hh()}] = ${(value.and(0xff)).h()} $extra")
    }

    override fun onRead(location: Int, value: Int) {
    }

    override fun onWrite(location: Int, value: Int) {
        if (DEBUG_MEMORY) logMem(location, value)
    }

}