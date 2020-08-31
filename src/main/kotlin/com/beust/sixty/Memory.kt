package com.beust.sixty

import java.io.File

class Memory(val size: Int = 0x10000, vararg bytes: Int) {
    var interceptor: MemoryInterceptor? = null
    var listener: MemoryListener? = null
    val content: IntArray = IntArray(size)

    init {
        bytes.copyInto(content)
    }

    operator fun get(i: Int): Int {
        val result = if (interceptor != null) {
            val response = interceptor!!.onRead(i)
            if (response.allow) response.value
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
            if (response.allow) {
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

    fun load(file: String, address: Int) {
//        File(file).readBytes().map { it.toInt() }.toIntArray().copyInto(content, address)
        File(file).readBytes().forEachIndexed { index, v ->
            if (index + address < 0x10000) {
                content[index + address] = v.toInt()
            }
        }
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
}