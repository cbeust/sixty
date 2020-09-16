package com.beust.app

import com.beust.sixty.h
import java.io.File

var DEBUG = false
// 6164: test failing LC writing
val BREAKPOINT = 0x6161

val disk = 0

val DISK_DOS_3_3 = DskDisk(File("src\\test\\resources\\Apple DOS 3.3.dsk").inputStream())

val DISK = if (disk == 0)
    WozDisk(Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz").openStream())
else if (disk == 1)
    DISK_DOS_3_3
else
    DskDisk(File("src/test/resources/audit.dsk").inputStream())

//val DISK2 = WozDisk(
//        File("d:\\pd\\Apple Disks\\woz2\\First Math Adventures - Understanding word problems.woz").inputStream())

fun main() {
    val choice = 2

    when(choice) {
        1 -> {
            println("Running the following 6502 program which will display HELLO")
            val c = TestComputer.createComputer()
            c.disassemble(start = 0, length = 15)
            c.run(_debugAsm = false)
        }
        2 -> {
            val debugMem = false
            val debugAsm = DEBUG
//            frame()
            apple2Computer(debugMem)
                    .run(debugMemory = debugMem, _debugAsm = debugAsm)//true, true)
        }
        3 -> {
            testDisk()
        }
        else -> {
            val result = functionalTestComputer(false).run()//true, true)
            with(result) {
                val sec = durationMillis / 1000
                val mhz = String.format("%.2f", cycles / sec / 1_000_000.0)
                println("Computer stopping after $cycles cycles, $sec seconds, $mhz MHz")
            }
        }
    }
}

fun testDisk() {
    val ins = Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz")!!.openStream()
    val ins2 = File("d:\\pd\\Apple DIsks\\woz2\\The Apple at Play.woz").inputStream()
    val disk: IDisk = WozDisk(ins)
    getOneTrack(disk, 0)
}

data class Sector(val number: Int, val content: IntArray)
data class Track(val number: Int, val sectors: Map<Int, Sector>)
data class DiskContent(val tracks: List<Track>)

fun getOneTrack(disk: IDisk, track: Int): Track {
    val sectors = hashMapOf<Int, Sector>()
    val result = arrayListOf<IntArray>()
    fun pair() = disk.nextByte().shl(1).or(1).and(disk.nextByte()).and(0xff)
    repeat(16) {
        while (disk.peekBytes(3) != listOf(0xd5, 0xaa, 0x96)) {
            disk.nextByte()
        }
        val s = disk.nextBytes(3)
        val volume = pair()
        val readTrack = pair()
        if (track != -1 && readTrack != track) {
            TODO("Tracks should match")
        }
        val sector = pair()
        val checksumAddress = pair()
        if (volume.xor(track).xor(sector) != checksumAddress) {
            TODO("Checksum doesn't match")
        }
        println("Volume: $volume Track: $track Sector: $sector checksum: $checksumAddress")
        if (disk.nextBytes(3) != listOf(0xde, 0xaa, 0xeb)) {
            TODO("Didn't find closing for address")
        }

        while (disk.peekBytes(3) != listOf(0xd5, 0xaa, 0xad)) {
            disk.nextByte()
        }
        disk.nextBytes(3)

        val buffer = IntArray(342)
        var checksum = 0
        for (i in buffer.indices) {
            val b = disk.nextByte()
            if (READ_TABLE[b] == null) {
                TODO("INVALID NIBBLE")
            }
            checksum = checksum xor READ_TABLE[b]!!
            if (i < 86) {
                buffer[buffer.size - i - 1] = checksum
            } else {
                buffer[i - 86] = checksum
            }
        }
        val bh = disk.peekBytes(1).first().h()
        checksum = checksum xor READ_TABLE[disk.nextByte()]!!
        if (checksum != 0) {
            TODO("BAD CHECKSUM")
        }

        val sectorData = IntArray(256)
        for (i in sectorData.indices) {
            val b1: Int = buffer[i]
            val lowerBits: Int = buffer.size - i % 86 - 1
            val b2: Int = buffer[lowerBits]
            val shiftPairs = i / 86 * 2
            // shift b1 up by 2 bytes (contains bits 7-2)
            // align 2 bits in b2 appropriately, mask off anything but
            // bits 0 and 1 and then REVERSE THEM...
            val reverseValues = intArrayOf(0x0, 0x2, 0x1, 0x3)
            val b = b1 shl 2 or reverseValues[b2 shr shiftPairs and 0x03]
            sectorData[i] = b
        }

        if (disk.nextBytes(3) != listOf(0xde, 0xaa, 0xeb)) {
            TODO("Didn't find closing for data")
        }
        val ls = DskDisk.LOGICAL_SECTORS[sector]
        sectors[ls] = Sector(ls, sectorData)
//        println("  Successfully read sector $sector (logical: $ls)")
    }
    return Track(track, sectors)
}
