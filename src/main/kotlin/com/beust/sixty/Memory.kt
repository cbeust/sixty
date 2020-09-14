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

    /** $D000-$DFFF */
    private val d000Bank1 = IntArray(0x1000) { 0 }
    /** $D000-$DFFF */
    private val d000Bank2 = IntArray(0x1000) { 0 }
    private var currentD0Bank = 1

    private fun switchToD0Bank1() {
        content.copyInto(d000Bank1, 0, 0xd000, 0xdfff)
        d000Bank1.copyInto(content, 0xd000)
        currentD0Bank = 1
    }

    private fun switchToD0Bank2() {
        content.copyInto(d000Bank1, 0, 0xd000, 0xdfff)
        d000Bank2.copyInto(content, 0xd000)
        currentD0Bank = 2
    }

    private var readRam = false
    private var writeRam = true
    private var readRom = true
    private var writeRom = false
    /** $E000-$FFFFF */
    private val e000bank = IntArray(0x2000) { 0 }

    private fun switchRamRom() {
        val tmp = IntArray(0x2000) { 0 }
        content.copyInto(tmp, 0, 0x2000, 0x3fff)
        e000bank.copyInto(content, 0xd000)
    }

    private fun switchToRam() {
        switchRamRom()
    }

    private fun switchToRom() {
        switchRamRom()
    }
    private var c081Count = 0

    /** Aux memory is $200-$BFFF */
    private val mainMemory = IntArray(0xbdff) { 0 }
    private val auxMemory = IntArray(0xbdff) { 0 }
    private var readMainMemory = true
    private var writeMainMemory = true

    private fun switchBanks(outBank: IntArray, inBank: IntArray) {
        content.copyInto(outBank, 0, 0x200, 0xbfff)
        inBank.copyInto(content, 0x200)
    }

    private fun mainMemoryForRead() {
        if (! readMainMemory) {
            readMainMemory = true
            switchBanks(auxMemory, mainMemory)
            println("CLRAUXRD read from main 48k (WR=-only")
        }
    }

    private fun mainMemoryForWrite() {
        if (! writeMainMemory) {
            writeMainMemory = true
            switchBanks(auxMemory, mainMemory)
            println("CLRAUXWR write to main 48k (WR-only)")
        }
    }

    private fun auxMemoryForRead() {
        if (readMainMemory) {
            readMainMemory = false
            switchBanks(mainMemory, auxMemory)
            println("SETAUXRD read from aux 48k (WR-only)")
        }
    }

    private fun auxMemoryForWrite() {
        if (writeMainMemory) {
            writeMainMemory = false
            switchBanks(mainMemory, auxMemory)
            println("SETAUXWR write to aux 48k (WR-ONLY")
        }
    }

    operator fun get(i: Int): Int {
        var result = content[i]
            when(i) {
                0xc080 -> {
                    switchToD0Bank2()
                    switchToRam()
                }
                0xc081 -> {
                    if (c081Count == 2) {
                        c081Count = 0
                        switchToD0Bank2()
                        switchToRom()
                        readRam = true
                        writeRam = false
                    } else {
                        c081Count++
                    }
                }
                0xc082 -> {
                    // Read ROM; write RAM; use $D000 bank 2
                    readRom = true
                    writeRom = false
                    switchToD0Bank2()
                    switchToRom()
                }
                0xc088 -> {
                    // Read RAM; no write; use $D000 bank 1
                    readRom = true
                    writeRom = false
                    writeRam = false
                    switchToD0Bank1()
                    switchToRom()
                }

                0xc0e8 -> {
                    NYI("Motor off")
                }
                0xc0e9 -> {
                    NYI("Motor on")
                }
                0xc0ea -> {
                    NYI("Drive 1")
                }
                0xc0eb -> {
                    NYI("Drive 2")
                }
                0xc0ed -> {
                    TODO("Clearing data latch not supported")
                }

                0xc0ec, 0xc0ee -> {
//                    val pos = disk.bitPosition
                    // Faster way for unprotected disks
                    result = DISK.nextByte()

                    // More formal way: bit by bit
//                    if (latch and 0x80 != 0) latch = 0
//                    latch = latch.shl(1).or(DISK.nextBit()).and(0xff)
//                    result = latch

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
        if (i >= 0xc000 && i < 0xc0ff) {
            println("BREAKPOINT")
        }
        when(i) {
            0xc002 -> {
                // Select main memory for reading in $0200...$BFFF
                mainMemoryForRead()
            }
            0xc003 -> {
                // Select aux memory for reading in $0200...$BFFF
                auxMemoryForRead()
            }
            0xc004 -> {
                // Select main memory for writing in $0200...$BFFF
                mainMemoryForWrite()
            }
            0xc005 -> {
                // Select aux memory for writing in $0200...$BFFF
                auxMemoryForWrite()
            }
            0xc006 -> {
                NYI("CLRCXROM use ROM on cards (WR-only")
            }
            0xc007 -> {
                NYI("SETCXROM use internal ROM (WR-only)")
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
        }
        if (i >= 0xc080 && i <= 0xc0ff) {
            println("         WRITE SOFT SWITCH " + i.hh())
        }

        if (i < 0xc000) {
            content[i] = value
            listener?.onWrite(i, value)
        } else {
//            when(i) {
//                0xc000 -> println("Writing to keyboard strobe")
//                0xc00c -> println("Turning 80 columns off")
//                0xc00e -> println("Primary character set")
//                0xe000 -> println("Trying to write in aux ram \$E000")
//                else -> println("Attempting to write in rom: " + i.hh())
//            }
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