package com.beust.sixty

import java.io.InputStream

class SimpleMemory(size: Int): IMemory {
    override val listeners = arrayListOf<MemoryListener>()
    override val lastMemDebug = arrayListOf<String>()
    private val bytes = IntArray(size)

    override fun get(address: Int): Int {
        val result = bytes[address]
        listeners.forEach { it.onRead(address, result) }
        return result
    }

    override fun set(address: Int, value: Int) {
        listeners.forEach { it.onWrite(address, value) }
        bytes[address] = value
    }

    override fun forceValue(address: Int, value: Int) {
        this[address] = value
    }

    override fun forceInternalRomValue(address: Int, value: Int) = forceValue(address, value)

    override fun load(bytes: ByteArray, address: Int, offset: Int, size: Int) {
        repeat(if (size == 0) bytes.size else size) {
            this.bytes[it + address] = bytes[it + offset].toInt().and(0xff)
        }
    }

    fun load(ins: InputStream, address: Int, fileOffset: Int, size: Int) {
        val allBytes = ins.readBytes()
        load(allBytes, address, fileOffset, size)
    }
}