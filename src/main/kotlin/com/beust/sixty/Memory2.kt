package com.beust.sixty

import com.beust.app.DISK
import com.beust.app.StepperMotor
import java.io.File
import java.io.InputStream

@Suppress("UnnecessaryVariable")
class Memory(val size: Int? = null) {
    var interceptor: MemoryInterceptor? = null
    var listener: MemoryListener? = null

    private var readMain = true
    private var writeMain = true

    /** $0-$1FF */
    private val lowMemory = IntArray(0x200) { 0 }

    /** $200 - $BFFF */
    private val mainMemory = IntArray(0xc000) { 0 }

    /** $200 - $BFFF */
    private val auxMemory = IntArray(0xc000) { 0 }

    /** $C000-$CFFF */
    private val c0Memory = IntArray(0x1000) { 0 }

    /** $D000-$FFFF */
    private val rom = IntArray(0x3000) { 0 }

    /** $D000-$DFFF */
    private val ram1 = IntArray(0x1000) { 0 }

    /** $D000-$DFFF */
    private val ram2 = IntArray(0x1000) { 0 }

    /** $E000-$FFFF */
    private val lcRam = IntArray(0x2000) { 0 }

    private var readRom = true
    private var readBank1 = true
    private var readBank2 = false
    private var writeBank1 = true
    private var writeBank2 = false

    private var init = true

    fun force(block: () -> Unit) {
        init = true
        block()
        init = false
    }

    fun init(address: Int, vararg bytes: Int) {
        var ii = address
        bytes.forEach { set(ii++, it) }
    }

    private val mem = IntArray(0x10000) { 0 }

    private fun getOrSet(get: Boolean, i: Int, value: Int = 0): Int? {
        var result: Int? = null

        if (i < 0x200) {
            if (get) {
                if (i < 0) {
                    println("PROBLEM")
                }
                result = lowMemory[i]
            } else {
                lowMemory[i] = value
            }
        } else if (i in 0x200..0xbfff) {
            if (get) {
                result = if (readMain) mainMemory[i]
                    else auxMemory[i]
            } else {
                if (writeMain) mainMemory[i] = value
                else auxMemory[i] = value
            }
        } else if (i in 0xc000..0xcfff) {
            if (get) {
                fun memory(rom: Boolean, rb1: Boolean, wb1: Boolean, rb2: Boolean, wb2: Boolean) {
                    readRom = rom
                    readBank1 = rb1
                    writeBank1 = wb1
                    readBank2 = rb2
                    writeBank2 = wb2
                }
                result = when (i) {
                    0xc010 -> {
                        c0Memory[0] = c0Memory[0] and 0x7f
                        c0Memory[0]
                    }
                    0xc082 -> {
                        //| ACTION | ADDRESS        | READ | WRITE? | $D0 |
                        //|   de R | $C082 / 49282  | ROM  | NO     | 2 |
                        memory(true, false, false, true, false)
                        0
                    }
                    0xc083 -> {
                        //| ACTION | ADDRESS        | READ | WRITE? | $D0 |
                        //      RR | $C083 / 49283  | RAM  | YES    | 2 |
                        memory(false, false, false, true, true)
                        0
                    }
                    0xc088 -> {
                        //| ACTION | ADDRESS        | READ | WRITE? | $D0 |
                        //|      R | $C088 / 49288  | RA M | NO     | 1 |
                        memory(false, true, false, false, false)
                        0
                    }
                    0xc089 -> {
                        //| ACTION | ADDRESS        | READ | WRITE? | $D0 |
                        //|     RR | $C089 / 49289  | ROM  | YES    | 1 |
                        memory(true, false, true, false, false)
                        0
                    }
                    0xc08a -> {
                        //| ACTION | ADDRESS        | READ | WRITE? | $D0 |
                        //| R      | $C08A / 49290  | ROM | NO      | 1   |
                        memory(true, false, false, false, false)
                        0
                    }
                    0xc08b -> {
                        //| ACTION | ADDRESS        | READ | WRITE? | $D0 |
                        //|     RR | $C08B / 49291  | RAM  | YES    | 1   |
                        memory(false, true, true, false, false)
                        0
                    }
                    0xc0ec -> {
                        //                    val pos = disk.bitPosition
                        // Faster way for unprotected disks
                        val r = DISK.nextByte()
                        r

                        // More formal way: bit by bit
                        //                    if (latch and 0x80 != 0) latch = 0
                        //                    latch = latch.shl(1).or(DISK.nextBit()).and(0xff)
                        //                    result = latch
                    }
                    in StepperMotor.RANGE -> {
                        StepperMotor.onRead(i, value, DISK)
                    }
                    else -> {
                        c0Memory[i - 0xc000]
                    }
                }
            } else {
                if (init) {
                    c0Memory[i - 0xc000] = value
                } else {
//                    handleC0(i, value)
                }
            }
        } else if (i in 0xd000..0xdfff) {
            val ea = i - 0xd000
            if (i == 0xd17b) {
                println("BREAKPOINT")
            }
            if (get) {
                    result = rom[ea]
//                result = when {
//                    readRom -> rom[ea]
//                    readBank1 -> ram1[ea]
//                    else -> ram2[ea]
//                }
            } else {
                if (init) rom[ea] = value
//                if (writeBank1) ram1[ea] = value
//                else if (writeBank2) ram2[ea] = value
//                else if (init) rom[ea] = value
            }
        } else {  // 0xe000-0xffff
            if (get) {
                 result = mem[i]
            } else {
                if (init) mem[i] = value
            }
//            if (get) {
//                result = if (readBank1 || readBank2) lcRam[i - 0xe000]
//                    else mem[i]
//            } else {
//                if (writeBank1 || writeBank2) {
//                    lcRam[i - 0xe000] = value
//                } else if (init) {
//                    mem[i] = value
//                }
//            }
        }

        if (get && result == null) {
            TODO("Should not happen")
        }
        return result
    }

    operator fun get(address: Int) : Int {
        val result = getOrSet(true, address)
//        listener?.onRead(address, result!!)
        return result!!.and(0xff)
    }

    operator fun set(address: Int, value: Int) {
        getOrSet(false, address, value)
        listener?.onWrite(address, value)
    }

    /**
     * Write from $C000-$DFFF
     */
    private fun manageWrite(content: IntArray, i: Int, value: Int) {
    }

    fun loadResource(name: String, address: Int) {
        load(this::class.java.classLoader.getResource(name).openStream(), address)
    }

    fun load(file: String, address: Int = 0) {
        File(file).inputStream().use { ins ->
            load(ins, address)
        }
    }

    fun load(ins: InputStream, address: Int = 0) {
        init = true
        ins.readBytes().forEachIndexed { index, v ->
            if (index + address < 0x10000) {
                this[index + address] = v.toInt()
            }
        }
        init = false
    }

    fun forceValue(i: Int, value: Int) {
        init = true
        this[i] = value
        init = false

    }
}