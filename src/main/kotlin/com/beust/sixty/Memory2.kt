package com.beust.sixty

import java.io.File
import java.io.InputStream
import kotlin.reflect.KProperty

@Suppress("UnnecessaryVariable", "BooleanLiteralArgument")
class Memory(val size: Int? = null) {
    var interceptor: MemoryInterceptor? = null
    val listeners = arrayListOf<MemoryListener>()
    val lastMemDebug = arrayListOf<String>()

    private var init = true

    var lastMem: String? = null
    private var store80On = false
    private var readAux = false
    private var writeAux = false
    private var hires = false
    private var page2 = false
    private var internalCxRom : Boolean by LoggedProp(true)
    private var internalC3Rom  : Boolean by LoggedProp(true)
    private var video80 = false
    private var altChar = false
    private var textSet = true
    private var mixed = false

    override fun toString() =
            "{Memory readAux:$readAux writeAux:$writeAux altZp:$altZp store80:$store80On page2: $page2" +
                    " c3Rom:$internalC3Rom cxRom:$internalCxRom"

    /** Affects $0-$1FF and $D000-$FFFF */
    private var altZp = false
    private val mainZp = IntArray(0x200) { 0 }
    private val auxZp = IntArray(0x200) { 0 }

    private val mainMemory = IntArray(0xc000) { 0 }
    private val auxMemory = IntArray(0xc000) { 0 }

    inner class C0Page {
        private val slot = IntArray(0x1000) { 0 }
        private val internal = IntArray(0x1000) { 0 }

//        fun current(address: Int): String = "Current C0 memory: " + (if (mem(address) == slot) "slot" else "internal")

        private fun mem(address: Int): IntArray {
            val result = when {
                address in 0xc000..0xc0ff -> internal
                ! internalCxRom && ! internalC3Rom -> {
                    when(address) {
                        in 0xc300..0xc3ff -> internal
                        else -> slot
                    }
                }
                ! internalCxRom && internalC3Rom -> slot
                else -> internal
            }
//            if (! init) println("Current: " + if (result == slot) "slot" else "internal")

            return result
        }

        operator fun get(address: Int): Int {
            if (! init && address == 0xc300) {
                println("Request for c300")
                ""
            }
            return (address - 0xc000).let { mem(it)[it] }
        }

        operator fun set(address: Int, value: Int) {
            val m = mem(address)
            if (init || m != internal) {
                (address - 0xc000).let {
                    if (! init) {
                        println("BREAKPOINT INTERNAL")
                    }
                    mem(it)[it] = value
                }
            } else {
                println("Declining write to ${address.hh()}=${value.h()}")
                ""
            }
        }
    }

    private val c0Memory = C0Page()

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
    private fun currentHighRam() = if (altZp) auxHighRam else mainHighRam

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

    private fun getOrSet(get: Boolean, i: Int, value: Int = 0): Int? {
        var result: Int? = null

        fun correctMem(condition: Boolean): IntArray {
            return if (condition) {
                if (page2) auxMemory else mainMemory
            } else {
                if (get) {
                    if (readAux) auxMemory else mainMemory
                } else {
                    if (writeAux) auxMemory else mainMemory
                }
            }
        }

        if (i < 0x200) {
            if (get) {
                result = if (altZp) auxZp[i] else mainZp[i]
            } else {
                if (altZp) auxZp[i] = value else mainZp[i] = value
            }
        } else if (i in 0x200..0xbfff) {
            val mem = when(i) {
                in 0x400..0x7ff -> correctMem(store80On)
                in 0x2000..0x3fff -> correctMem(store80On && hires)
                else -> if (get) {
                    if (readAux) auxMemory else mainMemory
                } else {
                    if (writeAux) auxMemory else mainMemory
                }
            }
            if (get)
                result = mem[i]
            else
                mem[i] = value
        } else if (i in 0xc000..0xcfff) {
            fun status(b: Boolean) = if (b) 0x80 else 0

            // Switches that are both read and write
            when(i) {
                0xc050 -> textSet = false
                0xc051 -> textSet = true
                0xc052 -> mixed = false
                0xc053 -> mixed = true
                0xc054 -> page2 = false
                0xc055 -> page2 = true
                0xc056 -> hires = false
                0xc057 -> hires = true
            }

            if (get) {
                // Read switches
                result = when (i) {
                    0xC000 -> {
                        // KBD/CLR80STORE
                        c0Memory[i]
                    }
                    0xc010 -> {
                        // KBDSTRB
                        c0Memory[0xc000] = c0Memory[0xc000].and(0x7f)
                        c0Memory[0xc000]
                    }
                    in 0xc001..0xc00f -> {
                        c0Memory[0xc000]
                    }
                    0xc013 -> status(readAux)
                    0xc014 -> status(writeAux)
                    0xc015 -> status(internalCxRom)
                    0xc016 -> status(altZp)
                    0xc017 -> status(internalC3Rom)
                    0xc018 -> status(store80On)
                    // 0xc019: VBL
                    0xc01a -> status(textSet)
                    0xc01b -> status(mixed)
                    0xc01c -> status(page2)
                    0xc01d -> status(hires)
                    0xc01e -> status(altChar)
                    0xc01f -> status(video80)
                    in 0xc080..0xc08f -> handleRam(get, i)
                    // Understanding the Apple IIe, 5-28
                    in 0xc100..0xcfff -> c0Memory[i]
                    else -> {
//                        println("Reading from unhandled " + i.hh())
                        c0Memory[i]
                    }
                }
            } else {
                // Write switches
                if (i in 0xc080..0xc08f) handleRam(get, i)
                else if (!init) when(i) {
                    0xc000 -> store80On = false
                    0xc001 -> store80On = true
                    0xc002 -> readAux = false
                    0xc003 -> readAux = true
                    0xc004 -> writeAux = false
                    0xc005 -> writeAux = true
                    0xc006 -> internalCxRom = false
                    0xc007 -> internalCxRom = true
                    0xc008 -> altZp = false
                    0xc009 -> altZp = true
                    0xc00a -> internalC3Rom = false
                    0xc00b -> internalC3Rom = true
                    0xc00c -> video80 = false
                    0xc00d -> video80 = true
                    0xc00e -> altChar = false
                    0xc00f -> altChar = true
                    0xc010 -> {
                        // KBDSTRB
                        c0Memory[0] = c0Memory[0].and(0x7f)
                        c0Memory[0]
                    }
                } else {
                    // init is true
//                    println("Initializing C0 memory with " + c0Memory.current(i))
                    c0Memory[i] = value
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