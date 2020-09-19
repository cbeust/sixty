package com.beust.sixty

import java.io.File
import java.io.InputStream

@Suppress("UnnecessaryVariable", "BooleanLiteralArgument")
class Memory(val size: Int? = null) {
    var interceptor: MemoryInterceptor? = null
    val listeners = arrayListOf<MemoryListener>()
    val lastMemDebug = arrayListOf<String>()

    private var init = true

    var lastMem: String? = null
    private var store80On = false
    private var hires = false

    /** Affects $0-$1FF and $D000-$FFFF */
    private var zpMain = true
        set(f) {
            if (! init) logMem("zpMain: $f")
            field = f
        }
    private val mainZp = IntArray(0x200) { 0 }
    private val auxZp = IntArray(0x200) { 0 }

    /** $200 - $BFFF */
    private var readMain = true
        set(f) {
            if (! init) logMem("readMain: $f")
            field = f
        }
    var writeMain = true
        set(f) {
            if (! init) logMem("writeMain: $f")
            field = f
        }

    var page2 = false

    private val mainMemory = IntArray(0xc000) { 0 }
    private val auxMemory = IntArray(0xc000) { 0 }

    /** $C000-$CFFF */
    private val c0Memory = IntArray(0x1000) { 0 }

    inner class HighRam(val name: String) {
        /** d000-dfff */
        val bank1 = IntArray(0x1000) { 0 }
        /** d000-dfff */
        val bank2 = IntArray(0x1000) { 0 }
        /** e000-ffff */
        val rom = IntArray(0x2000) { 0 }

        var readBank1 = false
            set(f) {
                if(f) {
                    println("Selecting bank1")
                }
                field = f
            }
        var readBank2 = false
        var writeBank1 = false
        var writeBank2 = false

        operator fun get(i: Int): Int {
            val ea = i - 0xd000
            val result = when {
                i in 0xe000..0xffff -> rom[i - 0xe000]
                readBank1 -> bank1[ea]
                readBank2 -> bank2[ea]
                else -> ERROR("Should never happen")
            }
            if (result == 0x11 && i == 0xd17b) {
                println("BREAKPOINT D1")
            }
            println("Highram $name [" + i.hh() + "] returning ${result.h()}")
            return result
        }

        operator fun set(i: Int, value: Int) {
            val ea = i - 0xd000
            if (i == 0xd17b && ! init) {
                println("BREAKPOINT")
            }
            when {
                i in 0xe000..0xffff -> rom[i - 0xe000] = value
                writeBank1 -> bank1[ea] = value
                writeBank2 -> bank2[ea] = value
                else -> ERROR("Should never happen")
            }

            println("Highram $name [" + i.hh() + "]=" + value.hh())
        }
    }

    /** $D000-$DFFF */

    /** $D000-$FFFF */
    private val rom = IntArray(0x3000) { 0 }
    private val mainHighRam = HighRam("Main")
    private val auxHighRam = HighRam("Aux")
    private fun currentHighRam() = if (zpMain) {
//        println("Selecting main high ram")
        mainHighRam
    } else {
//        println("Selecting aux high ram")
        auxHighRam
    }

//    private val ram1 = IntArray(0x1000) { 0 }
//    private val ram2 = IntArray(0x1000) { 0 }
//    private val auxRam1 = IntArray(0x1000) { 0 }
//    private val auxRam2 = IntArray(0x1000) { 0 }

    /** $E000-$FFFF */
    private var readRom = true
//    private val romBank1 = IntArray(0x2000) { 0 }
//    private val romBank2 = IntArray(0x2000) { 0 }

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
    private var writeCount = 0

    private fun updateSoftSwitch(value: Int, reset: Int, set: Int, status: Int, handled: Boolean = false,
        message: String? = null): Int {
        when (value) {
            reset -> force { c0Memory[status] = 0 }
            set -> force { c0Memory[status] = 0x80 }
            else -> ERROR("Should never happen")
        }
        if (! handled) {
            if (message != null) {
                logMem(message)
            } else {
                NYI("Write at " + (0xc000 + value).hh() + " new status[" + status.h() + "]=" + c0Memory[status].h())
            }
        }
        return c0Memory[0]
    }

    private fun getOrSet(get: Boolean, i: Int, value: Int = 0): Int? {
        var result: Int? = null

        fun correctMem(condition: Boolean): IntArray {
            return if (condition) {
                if (page2) auxMemory else mainMemory
            } else {
                if (get) {
                    if (readMain) mainMemory else auxMemory
                } else {
                    if (writeMain) mainMemory else auxMemory
                }
            }
        }

        if (i < 0x200) {
            if (get) {
                result = if (zpMain) mainZp[i] else auxZp[i]
            } else {
                if (zpMain) mainZp[i] = value else auxZp[i] = value
            }
        } else if (i in 0x200..0xbfff) {
            val mem = when(i) {
                in 0x400..0x7ff -> correctMem(store80On)
                in 0x2000..0x3fff -> correctMem(store80On && hires)
                else -> if (get) {
                    if (readMain) mainMemory else auxMemory
                } else {
                    if (writeMain) mainMemory else auxMemory
                }
            }
            if (get)
                result = mem[i]
            else
                mem[i] = value
        } else if (i in 0xc000..0xcfff) {
            if (get) {
                val address = i - 0xc000
                result = when (i) {
                    0xC000 -> {
                        // KBD/CLR80STORE
                        c0Memory[address]
                    }
                    0xc010 -> {
                        // KBDSTRB
                        c0Memory[0] = c0Memory[0].and(0x7f)
                        c0Memory[0]
                    }
                    in 0xc001..0xc00f -> {
                        c0Memory[0]
                    }
                    in 0xc080..0xc08f -> handleRam(get, i)
                    0xc050, 0xc051 -> updateSoftSwitch(address, 0x50, 0x51, 0x1a)
                    0xc052, 0xc053 -> updateSoftSwitch(address, 0x52, 0x53, 0x1b)
                    0xc054, 0xc055 -> {
                        page2 = i == 0xc055
                        updateSoftSwitch(address, 0x54, 0x55, 0x1c, message = "Page2 is $page2")
                    }
                    0xc056, 0xc057 -> {
                        hires = i == 0xc057
                        updateSoftSwitch(address, 0x56, 0x57, 0x1d, message = "Hires is $hires")
                    }
                    else -> {
//                        println("Reading from unhandled " + i.hh())
                        c0Memory[address]
                    }
                }
            } else { // set
                val address = i - 0xc000
                if (i in 0xc080..0xc08f) handleRam(get, i)
                else if (!init) when(i) {
                    0xc000, 0xc001 -> {
                        store80On = i == 0xc001
                        logMem("store80n: $store80On")
                        updateSoftSwitch(address, 0, 1, 0x18, handled = true)
                    }
                    0xc002, 0xc003 -> {
                        if (! store80On) {
                            readMain = i == 0xc002
                            updateSoftSwitch(address, 2, 3, 0x13)
                        }
                    }
                    0xc004, 0xc005 -> {
                        // | ACTION | ADDRESS       |
                        // |de W    | $C004 / 49156 | WRITE TO MAIN 48K |
                        if (! store80On) {
                            writeMain = i == 0xc004
                            updateSoftSwitch(address, 4, 5, 0x14, true)
                        }
                    }
                    0xc006, 0xc007 -> updateSoftSwitch(address, 6, 7, 0x15)
                    0xc008, 0xc009 -> {
                        if (i == 0xc009) {
                            println("BREAKPOINT SET ZP")
                        }
                        zpMain = i == 0xc008
                        updateSoftSwitch(address, 8, 9, 0x16, true)
                    }
                    0xc00a, 0xc00b -> updateSoftSwitch(address, 0xa, 0xb, 0x17)
                    0xc00c, 0xc00d -> updateSoftSwitch(address, 0xc, 0xd, 0x1f)
                    0xc00e, 0xc00f -> updateSoftSwitch(address, 0xe, 0xf, 0x1e)
                    0xc010 -> {
                        // KBDSTRB
                        c0Memory[0] = c0Memory[0].and(0x7f)
                        c0Memory[0]
                    }
                    0xc050, 0xc051 -> updateSoftSwitch(address, 0x50, 0x51, 0x1a)
                    0xc052, 0xc053 -> updateSoftSwitch(address, 0x52, 0x53, 0x1b)
                    0xc054, 0xc055 -> {
                        page2 = i == 0xc055
                        updateSoftSwitch(address, 0x54, 0x55, 0x1c)
                    }
                } else {
                    // init is true
                    c0Memory[i - 0xc000] = value
                }
            }
        } else if (i in 0xd000..0xffff) {
            val ea = i - 0xd000
            if (get) {
                result = when {
                    currentHighRam().readBank1 || currentHighRam().readBank2 -> currentHighRam()[i]
                    else -> rom[ea]
                }
            } else {
                when {
                    currentHighRam().writeBank1 || currentHighRam().writeBank2 -> currentHighRam()[i] = value
                    init -> rom[ea] = value
                }
            }
        }
//
//            fun memName(mem: IntArray?): String = when (mem) {
//                rom -> "rom"
//                auxRam1 -> "auxram1"
//                auxRam2 -> "auxram2"
//                ram1 -> "ram1"
//                ram2 -> "ram2"
//                else -> "denied"
//            }
//            if (! init && i == 0xd17b) {
//                println("Breakpoint")
//            }
//            lastMem = memName(mem)
//            if (mem != null) {
//                if (get) {
//                    result = mem[ea]
//                } else {
//                    if (i == 0xd17b) {
//                        println("BREAKPOINT D1")
//                    }
//                    mem[ea] = value
//                }
//            } else{
//                println("null mem")
//            }
            result
//        } else {  // 0xe000-0xffff
//            val ea = i - 0xe000
//            val bug = true
//            if (! bug) {
//                if (get) {
//                    result = mem[i]
//                } else {
//                    if (! init && i == 0xfe1f) {
//                        println("BREAK")
//                    }
//                    if (init) mem[i] = value
//                }
//            } else {
////                if (! init && i == 0xfe1f) {
////                    println("BREAKPOINT")
////                }
//                if (get) {
//                    result = when {
//                        readRom -> rom[i - 0xd000]
//                        readBank1 -> romBank1[ea]
//                        readBank2 -> romBank2[ea]
//                        else -> ERROR("Should never happen")
//                    }
//                } else {
//                    when {
//                        writeBank1 -> romBank1[ea] = value
//                        writeBank2 -> romBank2[ea] = value
//                        init -> {
//                            rom[i - 0xd000] = value
//                            romBank1[ea] = value
//                            romBank2[ea] = value
//                        }
//                        else -> println("Can't write to rom")
//                    }
//                }
//            }
//        }

        if (get && result == null) {
            TODO("Should not happen")
        }
        return result
    }

    operator fun get(address: Int) : Int {
        var result = getOrSet(true, address)
        var actual: Int? = null
        listeners.forEach {
            if (it.isInRange(address)) actual = it.onRead(address, result!!)
            if (actual != null) {
                result = actual
            }
        }
        return actual ?: result!!.and(0xff)
    }

    operator fun set(address: Int, value: Int) {
        if (address == 0x6040 && value == 0) {
            println("mem breakpoint")
        }
        getOrSet(false, address, value)
        listeners.forEach {
            if (it.isInRange(address)) it.onWrite(address, value)
        }
    }

    private fun handleRam(get: Boolean, i: Int) : Int {
        fun log(address: Int) {
            val readBank1 = currentHighRam().readBank1
            val writeBank1 = currentHighRam().writeBank1
            val writeBank2 = currentHighRam().writeBank2
            if (! init) logMem(address.hh() + " writeCount:$writeCount" +
                    " Read:" + (if (readRom) "rom" else if (readBank1) "ram1" else "ram2") +
                    " Write:" + (if (writeBank1) "ram1" else if (writeBank2) "ram2" else "no write"))
        }
        fun enableRomRead(address: Int) {
            readRom = true
            currentHighRam().readBank1 = false
            currentHighRam().readBank2 = false
            log(address)
        }
        fun enableRam1Read(address: Int) {
            readRom = false
            currentHighRam().readBank1 = true
            currentHighRam().readBank2 = false
            log(address)
        }
        fun enableRam2Read(address: Int) {
            readRom = false
            currentHighRam().readBank1 = false
            currentHighRam().readBank2 = true
            log(address)
        }
        fun enableRam1Write(address: Int) {
            currentHighRam().writeBank1 = true
            currentHighRam().writeBank2 = false
            log(address)
        }
        fun enableRam2Write(address: Int) {
            currentHighRam().writeBank1 = false
            currentHighRam().writeBank2 = true
            log(address)
        }
        fun disableWrite(address: Int) {
            currentHighRam().writeBank1 = false
            currentHighRam().writeBank2 = false
            log(address)
        }

        when(i) {
            0xc080, 0xc084 -> {
                writeCount = 0
                enableRam2Read(i)
                disableWrite(i)
            }
            0xc088, 0xc08c -> {
                writeCount = 0
                enableRam1Read(i)
                disableWrite(i)
            }
            0xc082, 0xc086, 0xc08a, 0xc08e -> {
                writeCount = 0
                enableRomRead(i)
                disableWrite(i)
            }
            else -> {
                if (! init) {
                    if (get) {
                        when(i) {
                            0xc081, 0xc085 -> {
                                if (writeCount < 2) writeCount++
                                enableRomRead(i)
                                if (writeCount == 2) {
                                    enableRam2Write(i)
                                }
                            }
                            0xc089, 0xc08d -> {
                                if (writeCount < 2) writeCount++
                                enableRomRead(i)
                                if (writeCount == 2) {
                                    enableRam1Write(i)
                                }
                            }
                            0xc083, 0xc087 -> {
                                if (writeCount < 2) writeCount++
                                enableRam2Read(i)
                                if (writeCount == 2) {
                                    enableRam2Write(i)
                                }
                            }
                            0xc08b, 0xc08f -> {
                                if (writeCount < 2) writeCount++
                                enableRam1Read(i)
                                if (writeCount == 2) {
                                    enableRam1Write(i)
                                }
                            }
                        }
                    } else { // write
                        when (i) {
                            0xc081, 0xc085, 0xc089, 0xc08d -> {
                                writeCount = 0
                                enableRomRead(i)
                            }
                            0xc083, 0xc087 -> {
                                writeCount = 0
                                enableRam2Read(i)
                            }
                            0xc08b, 0xc08f -> {
                                writeCount = 0
                                enableRam1Read(i)
                            }
                        }
                    }
                }
            }
        }
        return 0
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