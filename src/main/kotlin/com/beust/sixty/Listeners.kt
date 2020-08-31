package com.beust.sixty

object DebugMemoryListener : MemoryListener {
    override var lastMemDebug: String? = null

    fun logMem(i: Int, value: Int, extra: String = "") {
        lastMemDebug = "mem[${i.hh()}] = ${(value.and(0xff)).h()} $extra"
    }

    override fun onRead(location: Int, value: Int) {
    }

    override fun onWrite(location: Int, value: Int) {
        if (DEBUG_MEMORY) logMem(location, value)
    }

}