package com.beust.sixty

interface IMemory {
    operator fun get(address: Int): Int
    operator fun set(address: Int, value: Int)

    /**
     * Used when the value has to be set regardless of any other consideration (e.g. it's coming
     * from the emulator code, such as the keyboard).
     */
    fun forceValue(address: Int, value: Int)

    fun load(allBytes: ByteArray, address: Int = 0, offset: Int = 0, size: Int = 0)
    val listeners: ArrayList<MemoryListener>
    val lastMemDebug: ArrayList<String>

    fun init(address: Int, vararg bytes: Int) {
        var ii = address
        bytes.forEach { set(ii++, it) }
    }

    fun word(address: Int): Int = this[address].or(this[address + 1].shl(8))

    fun loadResource(name: String, address: Int = 0, fileOffset: Int = 0, size: Int = 0) {
        val bytes = this::class.java.classLoader.getResource(name).openStream().readAllBytes()
        load(bytes, address, fileOffset, if (size == 0) bytes.size else size)
    }

    fun dump(address: Int, length: Int = 80) {
        val lineLength = 8
        repeat(length / lineLength) { line ->
            val sb = StringBuffer(String.format("%04x", address + (line * lineLength)) + ": ")
            repeat(8) { byte ->
                sb.append(String.format("%02x ", this[address + line * lineLength + byte]))
            }
            sb.append(" ")
            repeat(8) { byte ->
                val c = (this[address + line * lineLength + byte] and 0x7f).toChar()
                sb.append(String.format("%c", c))
            }
            println(sb.toString())
        }
        println("===")
    }
}