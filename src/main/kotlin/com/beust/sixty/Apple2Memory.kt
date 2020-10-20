package com.beust.sixty

import com.beust.app.UiState
import java.io.File
import java.io.InputStream

@Suppress("UnnecessaryVariable", "BooleanLiteralArgument")
class Apple2Memory(val size: Int? = null): IMemory {
    override val lastMemDebug = arrayListOf<String>()
    override val listeners = arrayListOf<MemoryListener>()

    var init = true

    var store80On = false
        set(f) {
            field = f
            UiState.store80On.value = f
        }

    private var readAux = false
        set(f) {
            field = f
            UiState.readAux.value = f
        }
    private var writeAux = false
        set(f) {
            field = f
            UiState.writeAux.value = f
        }

    private var textSet = true
        set(f) {
            field = f
            UiState.mainScreenText.value = f
        }
    private var mixed  = false
        set(f) {
            field = f
            UiState.mainScreenMixed.value = f
        }
    var hires = false
        set(f) {
            field = f
            UiState.mainScreenHires.value = f
        }
    var page2 = false
        set(f) {
            field = f
            UiState.mainScreenPage2.value = f
        }

    var internalC8Rom: Boolean = false
    var internalCxRom: Boolean = false
        set(f) {
            internalC8Rom = false
            UiState.internalCxRom.value = f
            field = f
        }
    var slotC3Rom: Boolean = false
        set(f) {
            UiState.slotC3Rom.value = f
            internalC8Rom = false
            field = f
        }
    private var video80 = false
    private var altChar = false

    override fun toString() =
            "{Memory readAux:$readAux writeAux:$writeAux altZp:$altZp store80:$store80On page2: $page2" +
                    " intCxRom:$internalCxRom slotC3Rom:$slotC3Rom incC8Rom:$internalC8Rom"

    /** Affects $0-$1FF and $D000-$FFFF */
    private var altZp = false
        set(f) {
            UiState.altZp.value = f
            field = f
        }

    private val mainZp = IntArray(0x200) { 0 }
    private val auxZp = IntArray(0x200) { 0 }

    private val mainMemory = IntArray(0xc000) { 0 }
    private val auxMemory = IntArray(0xc000) { 0 }

    inner class C0Page {
        private val slot = IntArray(0x1000) { 0 }
        val internal = IntArray(0x1000) { 0 }

        fun current(address: Int): String = if (mem(address) == slot) "slot" else "internal"

        private fun mem(address: Int): IntArray {
            if (address in 0x300..0x3ff && ! slotC3Rom) internalC8Rom = true

            val result = if (internalC8Rom && address in 0x800..0xdff) internal
            else when {
                address in 0x000..0xff -> internal
                !internalCxRom && !slotC3Rom -> {
                    when (address) {
                        in 0x300..0x3ff -> internal
                        else -> slot
                    }
                }
                !internalCxRom && slotC3Rom -> slot
                else -> internal
            }
//            if (! init) println("Current: " + if (result == slot) "slot" else "internal")

            return result
        }

        operator fun get(address: Int): Int {
            return (address - 0xc000).let { mem(it)[it] }
        }

        operator fun set(address: Int, value: Int) {
            if (init && address >= 0xc100 && address % 256 == 0) {
                logMem("Loading bank ${address.hh()} in " + current(address - 0xc000))
            }
            (address - 0xc000).let {
                mem(it)[it] = value
            }
        }

//        fun loadInSlot(bytes: ByteArray, offset: Int = 0, size: Int = bytes.size, destOffset: Int = 0) {
//            repeat(size) { index ->
//                slot[index + destOffset] = bytes[index + offset].toInt().and(0xff)
//            }
//        }

        fun loadInInternal(bytes: ByteArray, offset: Int = 0, size: Int = bytes.size, destOffset: Int = 0) {
            repeat(size) { index ->
                internal[index + destOffset] = bytes[index + offset].toInt().and(0xff)
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
            return result
        }

        operator fun set(i: Int, value: Int) {
            val ea = i - 0xd000
            when {
                i in 0xe000..0xffff -> rom[i - 0xe000] = value
                writeBank1 -> bank1[ea] = value
                writeBank2 -> bank2[ea] = value
                else -> ERROR("Should never happen")
            }
        }
    }

    /** $D000-$DFFF */

    /** $D000-$FFFF */
    private val rom = IntArray(0x3000) { 0 }
    private val mainHighRam = HighRam("Main")
    private val auxHighRam = HighRam("Aux")
    private fun currentHighRam() = if (altZp) auxHighRam else mainHighRam

    /** $E000-$FFFF */
    private var readRom = true

    fun force(block: () -> Unit) {
        init = true
        block()
        init = false
    }

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
            val mem = when (i) {
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
            when (i) {
                0xc050 -> textSet = false
                0xc051 -> textSet = true
                0xc052 -> mixed = false
                0xc053 -> mixed = true
                0xc054 -> page2 = false
                0xc055 -> page2 = true
                0xc056 -> hires = false
                0xc057 -> hires = true
                0xcfff -> internalC8Rom = false
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
                    0xc017 -> status(slotC3Rom)
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
                if (init) c0Memory[i] = value
                else {
                    // Write switches
                    if (i in 0xc080..0xc08f) handleRam(get, i)
                    else if (!init) when (i) {
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
                        0xc00a -> slotC3Rom = false
                        0xc00b -> slotC3Rom = true
                        0xc00c -> video80 = false
                        0xc00d -> video80 = true
                        0xc00e -> altChar = false
                        0xc00f -> altChar = true
                        0xc010 -> {
                            // KBDSTRB
                            c0Memory[0xc000] = c0Memory[0xc000].and(0x7f)
                            c0Memory[0xc000]
                        }
                    }
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

        if (get && result == null) {
            TODO("Should not happen")
        }
        return result
    }

    override operator fun get(address: Int): Int {
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

    override operator fun set(address: Int, value: Int) {
        getOrSet(false, address, value)
        listeners.forEach {
            if (it.isInRange(address)) it.onWrite(address, value)
        }
    }

    private fun handleRam(get: Boolean, i: Int): Int {
        fun log(address: Int) {
            val readBank1 = currentHighRam().readBank1
            val writeBank1 = currentHighRam().writeBank1
            val writeBank2 = currentHighRam().writeBank2
            if (!init) logMem(address.hh() + " writeCount:$writeCount" +
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

        when (i) {
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
                if (!init) {
                    if (get) {
                        when (i) {
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

    fun load(file: String, address: Int = 0, fileOffset: Int = 0, size: Int = -1) {
        File(file).inputStream().use { ins ->
            load(ins, address, fileOffset, size)
        }
    }

    override fun load(allBytes: ByteArray, address: Int, offset: Int, size: Int) {
        val max = if (size <= 0) allBytes.size else size
        repeat(max) { index ->
            val v = allBytes[index + offset]
            if (index + address < 0x10000) {
                this[index + address] = v.toInt()
            }
        }
    }

    fun load(ins: InputStream, address: Int, fileOffset: Int, size: Int) {
        init = true
        val allBytes = ins.readBytes()
        load(allBytes, address, fileOffset, size)
        init = false
    }

//    fun loadCxxxInSlot(bytes: ByteArray, offset: Int = 0, size: Int = bytes.size, destOffset: Int = 0) {
//        c0Memory.loadInSlot(bytes, offset, size, destOffset)
//    }

    private fun loadCxxxInInternal(bytes: ByteArray, offset: Int = 0, size: Int = bytes.size, destOffset: Int = 0) {
        c0Memory.loadInInternal(bytes, offset, size, destOffset)
    }

    override fun forceValue(i: Int, value: Int) {
        synchronized(this) {
            init = true
            this[i] = value
            init = false
        }
    }

    override fun forceInternalRomValue(i: Int, value: Int) {
        if (i !in 0xc000..0xcfff) {
            ERROR("SHOULD BE IN 0xc000-0xcfff RANGE")
        }
        synchronized(this) {
            c0Memory.internal[i - 0xc000] = value
        }
    }

    init {
//        load("d:\\pd\\Apple Disks\\roms\\APPLE2E.ROM", 0xc000)
//        load("d:\\pd\\Apple Disks\\roms\\C000.dump", 0xc000)
        loadResource("Apple2e.rom", 0xd000, 0x1000, 0x3000)

        val bytes = this::class.java.classLoader.getResource("Apple2e.rom").openStream().readAllBytes()

        loadCxxxInInternal(bytes, 0x100, 0xeff, 0x100)
//        slotC3Rom = true
//        internalCxRom = true
//        // Load C100-C2FF in internal rom
//        loadResource("Apple2e.rom", 0xc100, 0x100, 0x200)
//        // C400 internal
//        slotC3Rom = true
//        internalCxRom = true
//        loadResource("Apple2e.rom", 0xc400, 0x400, 0x400)
//        // Load C800-CFFF in internal
//        slotC3Rom = true
//        internalCxRom = true
//        loadResource("Apple2e.rom", 0xc800, 0x800, 0x800)
//        // Load C300-C3FF in internal rom
//        slotC3Rom = true
//        internalCxRom = true
//        loadResource("Apple2e.rom", 0xc300, 0x300, 0x100)

        internalCxRom = false
        slotC3Rom = true
        // C600 in slot
        loadResource("DISK2.ROM", 0xc600)

        // Reset
        internalCxRom = false
        slotC3Rom = false
//        slotC3WasReset = false
        internalC8Rom = false
//        Thread {
//            runWatcher(this)
//        }.start()

        // When restarting, no need to move the head 0x50 tracks
//        this[0xc63c] = 4
        init = false
    }

    fun save(fileName: String, address: Int, length: Int) {
        val bytes = arrayListOf<Byte>()
        repeat(length) {
            bytes.add(this[address + it].toByte())
        }

        File(fileName).writeBytes(bytes.toByteArray())
        log("Saved file $fileName, address: \$${address.hh()}, length: ${length.hh()}")
    }
}


