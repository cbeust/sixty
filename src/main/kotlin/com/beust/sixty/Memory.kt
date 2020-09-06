package com.beust.sixty

import java.io.File
import java.io.InputStream

class Memory(val size: Int = 0x10000, vararg bytes: Int) {
    var interceptor: MemoryInterceptor? = null
    var listener: MemoryListener? = null
    val content: IntArray = IntArray(size)

    init {
        bytes.copyInto(content)
    }

    operator fun get(i: Int): Int {
        val result = if (interceptor != null) {
            val response = interceptor!!.onRead(i, content[i])
            if (response.override) response.value
            else content[i]
        } else {
            content[i]
        }

        listener?.onRead(i, result)
        return result.and(0xff)
    }

    operator fun set(i: Int, value: Int) {
        if (interceptor != null) {
            val response = interceptor!!.onWrite(i, value)
            if (response.override) {
                content[i] = value
            }
        } else {
            content[i] = value
        }
        listener?.onWrite(i, value)
    }

    override fun toString(): String {
        return content.slice(0..16).map { it.and(0xff).h()}.joinToString(" ")
    }

    fun init(address: Int, vararg bytes: Int) {
        var ii = address
        bytes.forEach { set(ii++, it) }
    }

    fun load(ins: InputStream, address: Int = 0) {
        ins.readBytes().forEachIndexed { index, v ->
            if (index + address < 0x10000) {
                content[index + address] = v.toInt()
            }
        }
    }

    fun load(file: String, address: Int = 0) {
        File(file).inputStream().use { ins ->
            load(ins, address)
        }
    }

    fun loadResource(name: String, address: Int) {
        load(this::class.java.classLoader.getResource(name).openStream(), address)
    }

    fun wordAt(word: Int): Int {
        return get(word + 1).shl(8).or(get(word))
    }

    fun clone(): Memory {
        return Memory().apply {
            init(0, *content)  // careful, aliasing content here
        }
//        return Memory(content)
//        val newContent = IntArray(size)
//        content.copyInto(newContent, 0, size)
//        return Memory().apply {
//            init(0, *content)
//        }
    }

    fun dump(address: Int, length: Int = 80) {
        val lineLength = 8
        repeat(length / lineLength) { line ->
            val sb = StringBuffer(String.format("%04x", address + (line * lineLength)) + ": ")
            repeat(8) { byte ->
                sb.append(String.format("%02x ", this[address + line * lineLength + byte]))
            }
            println(sb.toString())
        }
    }
}