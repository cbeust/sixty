package com.beust.sixty

import com.beust.app.*
import java.io.File
import java.io.InputStream

class Memory(val size: Int = 0x10000, vararg bytes: Int) {
    var interceptor: MemoryInterceptor? = null
    var listener: MemoryListener? = null
    val content: IntArray = IntArray(size)

    init {
        bytes.copyInto(content)
    }

    private var latch = 0
    private var d5 = false
    private var d5aa = false
    private var d5aa96 = false
    private var count = 0
    private var byte0 = 0
    private var volume = 0
    private var track = 0
    private var sector = 0

    private fun pair(b1: Int, b2: Int) = b1.shl(1).or(1).and(b2)

    operator fun get(i: Int): Int {
        var result = content[i]
            when(i) {
                0xc0e8 -> {
                    println("Motor off")
                }
                0xc0e9 -> {
                    println("Motor on")
                }
                0xc0ea -> {
                    println("Drive 1")
                }
                0xc0eb -> {
                    println("Drive 2")
                }
                0xc0ed -> {
                    TODO("Clearing data latch not supported")
                }

                0xc0ec, 0xc0ee -> {
//                    val pos = disk.bitPosition
                    // Faster way for unprotected disks
                    result = DISK.nextByte()

//                    if (d5aa96) {
//                        when (count) {
//                            0 -> byte0 = result
//                            1 -> volume = pair(byte0, result)
//                            2 -> byte0 = result
//                            3 -> track = pair(byte0, result)
//                            4 -> byte0 = result
//                            5 -> {
//                                sector = pair(byte0, result)
//                                println("Read volume $volume, track $track, sector $sector")
//                                count = -1
//                                d5 = false
//                                d5aa = false
//                                d5aa96 = false
//                            }
//                        }
//                        count++
//                    } else if (result == 0xd5) d5 = true
//                    else if (result == 0xaa && d5) d5aa = true
//                    else if (d5aa) {
//                        if (result == 0x96) d5aa96 = true
//                        else d5aa = false
//                    }

//                    return result

                    // More formal way: bit by bit
//                    if (latch and 0x80 != 0) latch = 0
//                    latch = latch.shl(1).or(DISK.nextBit()).and(0xff)

//                    return latch
                }
                0xc010 -> {
                    content[0xc000] = content[0xc000] and 0x7f
                    content[0xc010]
                }
                in StepperMotor.RANGE -> {
                    StepperMotor.onRead(i, content[i], DISK)
                }
                else -> {
                    if (i >= 0xc080 && i <= 0xc0ff) {
                        println("READ SOFT SWITCH " + i.hh())
                    }
                    content[i]
                }
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

//        listener?.onRead(i, result)
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
//        if (i == 0x3f && value == 0x1b) {
//            DEBUG = true
//            val v = value.h()
//            println("Modifying $3F: $value")
//        }
        if (i >= 0xc080 && i <= 0xc0ff) {
            println("         WRITE SOFT SWITCH " + i.hh())
        }

        if (i < 0xc000) {
            content[i] = value
            listener?.onWrite(i, value)
        } else {
            when(i) {
                0xc000 -> println("Writing to keyboard strobe")
                0xc00c -> println("Turning 80 columns off")
                0xc00e -> println("Primary character set")
                0xe000 -> println("Trying to write in aux ram \$E000")
                else -> println("Attempting to write in rom: " + i.hh())
            }
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