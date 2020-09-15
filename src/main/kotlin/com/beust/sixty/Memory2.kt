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
                result = when (i) {
                    0xc010 -> {
                        c0Memory[0] = c0Memory[0] and 0x7f
                        c0Memory[0]
                    }
                    0xc083, 0xc08b -> {
                        if (c083Count == 0) {
                            readRom = false
                            readBank1 = true
                            readBank2 = false
                            writeBank1 = true
                            writeBank2 = false
                            c083Count++
                        } else if (c083Count == 1) {
                            readRom = false
                            readBank1 = false
                            readBank2 = true
                            writeBank1 = false
                            writeBank2 = true
                            c083Count = 0
                        }
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
                    handleC0(i, value)
                }
            }
        } else {
            if (get) {
                result = mem[i]
            } else if (init) {
                mem[i] = value
            }
        }

        if (get && result == null) {
            TODO("Should not happen")
        }
        return result
    }

    private fun _getOrSet(get: Boolean, i: Int, value: Int = 0): Int? {
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
                result = mainMemory[i]
//                result = if (readMain) mainMemory[i]
//                    else auxMemory[i]
            } else {
                mainMemory[i] = value
//                if (writeMain) mainMemory[i] = value
//                else auxMemory[i] = value
            }
        } else if (i in 0xc000..0xcfff) {
            if (get) {
                result = when (i) {
                    0xc010 -> {
                        c0Memory[0] = c0Memory[0] and 0x7f
                        c0Memory[0]
                    }
                    0xc083, 0xc08b -> {
//                        if (c083Count == 0) {
//                            readRom = false
//                            readBank1 = true
//                            readBank2 = false
//                            writeBank1 = true
//                            writeBank2 = false
//                            c083Count++
//                        } else if (c083Count == 1) {
//                            /*
//                            $C083 or $C08B enables the language card RAM in "read/write" mode,
//            with the ROM completely disabled. This is used when exeucting an
//            operating system (e.g. ProDOS or Pascal) from the language card space,
//            where part of the RAM is used as buffering memory, for example. The two
//            locations select different RAM banks in the $D000-$DFFF area.
//                             */
//                            readRom = false
//                            readBank1 = false
//                            readBank2 = true
//                            writeBank1 = false
//                            writeBank2 = true
//                            c083Count = 0
//                        }
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
                if (i == 0xc004) {
                    println("BREAKPOINT")
                }
                if (init) {
                    c0Memory[i - 0xc000] = value
                } else {
                    handleC0(i, value)
                }
            }
        } else if (i in 0xd000..0xdfff) {
            if (i == 0xd17b) {
                println("BREAKPOINT")
            }
            if (get) {
                result = rom[i - 0xd000]
//                result = when {
//                    readRom -> rom[i - 0xd000]
//                    readBank1 -> ram1[i - 0xd000]
//                    else -> ram2[i - 0xd000]
//                }
            } else {
//                if (writeBank1) ram1[i - 0xd000] = value
//                else if (writeBank2) ram2[i - 0xd000] = value
            }
        } else {
            if (get) {
                result = rom[i - 0xd000]
            } else {
//                if (init || ! readRom)
                    rom[i - 0xd000] = value
            }
        }

        if (get && result == null) {
            TODO("Should never happen")
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

    private var c083Count = 0

    private fun handleC0(i: Int, value: Int) {
        when(i) {
            0xc000 -> {
                // ignore
            }
            0xc001 -> {
                NYI("80STORE ON")
            }
            0xc002 -> {
                // Select main memory for reading in $0200...$BFFF
                readMain = true
            }
            0xc003 -> {
                // Select aux memory for reading in $0200...$BFFF
                readMain = false
            }
            0xc004 -> {
                // Select main memory for writing in $0200...$BFFF
                writeMain = true
            }
            0xc005 -> {
                // Select aux memory for writing in $0200...$BFFF
                writeMain = false
            }
            0xc006 -> {
//                NYI("Enable slot ROM from \$C100-\$CFFF")
            }
            0xc007 -> {
                NYI("INTCXROMON Enable main ROM from \$C100-\$CFFF")
            }
            0xc008 -> {
                // Select main memory for reading & writing in $0000...$01FF & $D000...$FFFF
                NYI("CLRAUXZP use main zero page, stack, and LC (WR-only)")
            }
            0xc009 -> {
                // Select aux memory for reading & writing in $0000...$01FF & $D000...$FFFF
                NYI("SETAUXZP use alt zero page, stack, and LC (WR-only)")
            }
            0xc00a -> {
                NYI("CLRC3ROM use internal Slot 3 ROM (WR-only)")
            }
            0xc00b -> {
                NYI("SETC3ROM use external Slot 3 ROM (WR-only)")
            }
            0xc00c -> {
                NYI("CLR80VID = disable 80-column display mode (WR-only)")
            }
            0xc00d -> {
                NYI("SET80VID enable 80-column display mode (WR-only)")
            }
            0xc00e -> {
                NYI("CLRALTCH use main char set- norm LC, Flash UC (WR-only)")
            }
            0xc00f -> {
                NYI("SETALTCH use alt char set- norm inverse, LC; no Flash (WR-only)")
            }
            0xc080 -> {
                // Read RAM bank 2; no write
                readBank1 = false
            }
            0xc081 -> {
                // Read ROM; write RAM bank 2
//                if (c081Count == 2) {
//                    c081Count = 0
//                    writeBank1 = false
//                    readRom = true
//                } else {
//                    readRom = true
//                    c081Count++
//                }
            }
            0xc082 -> {
                NYI("\$C082")
                // Read ROM; write RAM; use $D000 bank 2
//                readRom = true
//                writeRom = false
//                switchToD0Bank2()
//                switchToRom()
            }
            0xc088 -> {
                NYI("\$C088")
                // Read RAM; no write; use $D000 bank 1
//                readRom = true
//                writeRom = false
//                writeRam = false
//                switchToD0Bank1()
//                switchToRom()
            }
            0xc08a -> {
                NYI("0xc08a")
            }
            else -> {
                val ah = i.hh()
//                NYI("UNEXPECTED LOCATION: ${i.hh()}")
            }
        }
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