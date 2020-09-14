package com.beust.sixty

class MemoryBanks(private val content: IntArray) {

    companion object {
        private val RANGE = 0xc000..0xc010
        fun isInRange(address: Int) = address in RANGE || address in 0xc080..0xc08b
    }

    fun onRead(address: Int) {
        when(address) {
            0xc010 -> {
                content[0xc000] = content[0xc000] and 0x7f
                content[0xc000]
            }
            in 0xc000..0xc00f -> {
                content[0xc000]
            }
        }
    }

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

    fun onWrite(address: Int) {
        when(address) {
            0xc000 -> {
                // ignore
            }
            0xc001 -> {
                NYI("80STORE ON")
            }
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
                NYI("Enable slot ROM from \$C100-\$CFFF")
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
            0xc083, 0xc08b -> {
                /*
                $C083 or $C08B enables the language card RAM in "read/write" mode,
with the ROM completely disabled. This is used when exeucting an
operating system (e.g. ProDOS or Pascal) from the language card space,
where part of the RAM is used as buffering memory, for example. The two
locations select different RAM banks in the $D000-$DFFF area.
                 */
                TODO("LANGUAGE CARD")
            }
            0xc088 -> {
                // Read RAM; no write; use $D000 bank 1
                readRom = true
                writeRom = false
                writeRam = false
                switchToD0Bank1()
                switchToRom()
            }
            0xc08a -> {
                TODO("ROMONLY1")
            }
            else -> {
                val ah = address.hh()
                TODO("UNEXPECTED LOCATION: ${address.hh()}")
            }
        }
    }
}