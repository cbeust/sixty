package com.beust.sixty

import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream

@Suppress("UnnecessaryVariable", "BooleanLiteralArgument")
class Memory(val size: Int? = null) {
    private val log = LoggerFactory.getLogger("Memory")

    var interceptor: MemoryInterceptor? = null
    val listeners = arrayListOf<MemoryListener>()
    val lastMemDebug = arrayListOf<String>()

    fun logMem(i: Int, value: Int, extra: String = "") {
        lastMemDebug.add("mem[${i.hh()}] = ${(value.and(0xff)).h()} $extra")
    }

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
    private var readBank1 = false
    private var readBank2 = false
    private var writeBank1 = false
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
    private var writeCount = 0

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
                result = when (i) {
                    in 0xc080..0xc08d -> handleRam(get, i)
                    else -> {
                        c0Memory[i - 0xc000]
                    }
                }
            } else {
                if (i in 0xc080..0xc08d) handleRam(get, i)
                else if (!init) when(i) {
                    0xc002 -> {
                        // | ACTION | ADDRESS       |
                        // |de W    | $C002 / 49156 | READ FROM MAIN 48K |
                        readMain = true
                    }
                    0xc003 -> {
                        // | ACTION | ADDRESS       |
                        // |de W    | $C003 / 49156 | READ FROM AUX 48K |
                        readMain = false
//                        c0Memory[0x13] = 0x80
                    }
                    0xc004 -> {
                        // | ACTION | ADDRESS       |
                        // |de W    | $C004 / 49156 | WRITE TO MAIN 48K |
                        writeMain = true
                    }
                    0xc005 -> {
                        // | ACTION | ADDRESS       |
                        // |de W    | $C005 / 49156 | WRITE TO AUX 48K |
                        writeMain = false
                    }

                }
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
                result = when {
                    readRom -> rom[ea]
                    readBank1 -> ram1[ea]
                    else -> ram2[ea]
                }
            } else {
                when {
                    writeBank1 -> ram1[ea] = value
                    writeBank2 -> ram2[ea] = value
                    init -> rom[ea] = value
                }

            }
        } else {  // 0xe000-0xffff
            val bug = true
            if (! bug) {
                if (get) {
                    result = mem[i]
                } else {
                    if (init) mem[i] = value
                }
            } else {
//                if (! init && i == 0xfe1f) {
//                    println("BREAKPOINT")
//                }
                if (get) {
                    result = if (readBank1 || readBank2) lcRam[i - 0xe000]
                    else mem[i]
                } else {
                    if (writeBank1 || writeBank2) {
                        lcRam[i - 0xe000] = value
                    } else if (init) {
                        lcRam[i - 0xe000] = value
                        mem[i] = value
                    }
                }
            }
        }

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
        getOrSet(false, address, value)
        listeners.forEach {
            if (it.isInRange(address)) it.onWrite(address, value)
        }
    }

    private fun handleRam(get: Boolean, i: Int) : Int {
        fun memory(address: Int, rom: Boolean, rb1: Boolean, wb1: Boolean, rb2: Boolean, wb2: Boolean) {
            readRom = rom
            readBank1 = rb1
            writeBank1 = wb1
            readBank2 = rb2
            writeBank2 = wb2
            log.debug(address.hh() + " writeCount:$writeCount" +
                    " Read:" + (if (readRom) "rom" else if (readBank1) "ram1" else "ram2") +
                    " Write:" + (if (writeBank1) "ram1" else if (writeBank2) "ram2" else "rom"))
        }

        if (get) {
            when(i) {
                0xc080, 0xc084 -> {
                    writeCount = 0
                    memory(i, false, false, false, true, false) // read 2
                }
                0xc088, 0xc08c -> {
                    writeCount = 0
                    memory(i, false, true, false, false, false) // read 1
                }
                0xc081, 0xc085 -> {
                    if (writeCount < 2) writeCount++
                    if (writeCount == 1) {
                        memory(i, true, false, false, false, false) // read rom
                    } else if (writeCount == 2) {
                        memory(i, true, false, false, false, true) // read rom, write ram 2
                    }
                }
                0xc089, 0xc08d -> {
                    if (writeCount < 2) writeCount++
                    if (writeCount == 1) {
                        memory(i, true, false, false, false, false) // read rom
                    } else if (writeCount == 2) {
                        memory(i, true, false, true, false, false) // read rom, write ram 1
                    }
                }
                0xc082, 0xc086, 0xc08a, 0xc08e -> {
                    writeCount = 0
                    memory(i, true, false, false, false, false) // read rom
                }
                0xc083, 0xc087 -> {
                    if (writeCount < 2) writeCount++
                    if (writeCount == 1) {
                        memory(i, false, false, false, true, false) // read ram 2
                    } else if (writeCount == 2) {
                        memory(i, false, false, false, true, true) // read/write ram 2
                    }
                }
                0xc08b, 0xc08f -> {
                    if (writeCount < 2) writeCount++
                    if (writeCount == 1) {
                        memory(i, false, true, false, false, false) // read ram 1
                    } else if (writeCount == 2) {
                        memory(i, false, true, true, false, false) // read/write ram 1
                    }
                }
            }
        } else {

        }
        return 0
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
                if (index+address == 0xd000) {
                    println("BREAKPOINT")
                }
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