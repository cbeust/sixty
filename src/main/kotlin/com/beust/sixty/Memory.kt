package com.beust.sixty

import com.beust.app.StepperMotor
import com.beust.app.Woz
import com.beust.app.WozDisk
import java.io.File
import java.io.InputStream

class Memory(val size: Int = 0x10000, vararg bytes: Int) {
    private val disk = WozDisk(Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz").openStream())

    var interceptor: MemoryInterceptor? = null
    var listener: MemoryListener? = null
    val content: IntArray = IntArray(size)

    init {
        bytes.copyInto(content)
    }

    operator fun get(i: Int): Int {
        val result =
            when(i) {
                0xc0ec -> disk.nextByte()
                0xc010 -> {
                    content[0xc000] = content[0xc000] and 0x7f
                    content[0xc010]
                }
                in StepperMotor.RANGE -> {
                    StepperMotor.onRead(i, content[i], disk)
                }
                else -> content[i]
            }
//        val result = if (interceptor != null) {
//            val response = interceptor!!.onRead(i, content[i])
//            if (response.allow) {
//                content[i] = response.value
//            }
//            content[i]
//        } else {
//            content[i]
//        }

        listener?.onRead(i, result)
        return result.and(0xff)
    }

    operator fun set(i: Int, value: Int) {
//        if (interceptor != null) {
//            val response = interceptor!!.onWrite(i, value)
//            if (response.allow) {
//                content[i] = value
//                listener?.onWrite(i, value)
//            } else {
//                // No need to notify the listener, that change was vetoed
//            }
//        } else {
        if (i < 0xc000) {
            content[i] = value
            listener?.onWrite(i, value)
        }
    }

    fun forceValue(i: Int, value: Int) {
        content[i] = value
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