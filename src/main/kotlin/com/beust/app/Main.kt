package com.beust.app

import com.beust.app.app.TextPanel
import com.beust.sixty.*
import com.beust.swt.IGraphics
import com.beust.swt.createWindows
import java.io.File

var DEBUG = false
// 6164: test failing LC writing
// check failing at 0x64f9
//val BREAKPOINT: Int? = 0x65f8 // 0x65c3 // 0x658d
val BREAKPOINT: Int? = null//0xc2de // 0xc2db
// val BREAKPOINT: Int? = 0x6036 // test break

val disk = 0

val DISK_DOS_3_3 = DskDisk("Apple DOS 3.3.dsk", File("src\\test\\resources\\Apple DOS 3.3.dsk").inputStream())
val WOZ_DOS_3_3 = WozDisk("DOS 3.3 System Master.woz", Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz").openStream())

val DISK = if (disk == 0)
    WOZ_DOS_3_3
else if (disk == 1)
    DISK_DOS_3_3
else if (disk == 2)
    DskDisk("audit.dsk", File("src/test/resources/audit.dsk").inputStream())
else
    DskDisk("Sherwood Forest.dsk", File("d:\\pd\\Apple disks\\Sherwood Forest.dsk").inputStream())
//    DskDisk(File("d:\\pd\\Apple disks\\Ultima I - The Beginning.woz").inputStream())

//val DISK2 = WozDisk(
//        File("d:\\pd\\Apple Disks\\woz2\\First Math Adventures - Understanding word problems.woz").inputStream())

fun main() {
    val choice = 2

    val pulseManager = PulseManager()

//    val diskController = DiskController()
//    val apple2Memory = Apple2Memory()
//    val c = Computer.create {
//        memory = apple2Memory
//        memoryListeners.add(Apple2MemoryListener(apple2Memory))
//        memoryListeners.add(diskController)
//    }.build()
//    val start = apple2Memory[0xfffc].or(apple2Memory[0xfffd].shl(8))
//    c.cpu.PC = start
//
//    pulseListeners.add(c)
//    pulseListeners.add(diskController)
//
//    while (true) {
//        pulseListeners.forEach { it
//            it.onPulse()
//        }
//    }
    var graphics: IGraphics? = null
    val fw = FileWatcher()

    when(choice) {
        1 -> {
            println("Running the following 6502 program which will display HELLO")
            val c = TestComputer.createComputer()
//            c.disassemble(start = 0, length = 15)
            pulseManager.addListener(c)
        }
        2 -> {
            val debugMem = false
            val debugAsm = DEBUG
//            frame()
            val dc = DiskController(6).apply {
                loadDisk(DISK)
                UiState.currentDisk.value = DISK
            }
            val keyProvider = object: IKeyProvider {
                override fun keyPressed(memory: IMemory, value: Int, shift: Boolean, control: Boolean) {
                    memory.forceInternalRomValue(0xc000, value.or(0x80))
                    memory.forceInternalRomValue(0xc010, 0x80)
                }
            }

            pulseManager.addListener(dc)
            val a2Memory = Apple2Memory()
            graphics = createWindows(a2Memory, keyProvider)
            val textPanel =  TextPanel(graphics.textScreen)

            val computer = Computer.create {
                memory = a2Memory
                memoryListeners.add(Apple2MemoryListener(textPanel))
                memoryListeners.add(dc)
            }.build()
            val start = a2Memory.word(0xfffc) // memory[0xfffc].or(memory[0xfffd].shl(8))
            computer.cpu.PC = start

            pulseManager.addListener(computer)
            Thread {
                fw.run(a2Memory)
            }.start()
        }
        3 -> {
            testDisk()
        }
        else -> {
            pulseManager.addListener(functionalTestComputer(false))
//            with(result) {
//                val sec = durationMillis / 1000
//                val mhz = String.format("%.2f", cycles / sec / 1_000_000.0)
//                println("Computer stopping after $cycles cycles, $sec seconds, $mhz MHz")
//            }
        }
    }

//    Thread {
//        pulseManager.run()
//    }.start()

    graphics?.run()
    fw.stop = true
}

fun testDisk() {
    val ins = Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz")!!.openStream()
    val ins2 = File("d:\\pd\\Apple DIsks\\woz2\\The Apple at Play.woz").inputStream()
    val disk: IDisk = WozDisk("The Apple at Play.woz", ins)
    getOneTrack(disk, 0)
}

data class Sector(val number: Int, val content: IntArray)
data class Track(val number: Int, val sectors: Map<Int, Sector>)
data class DiskContent(val tracks: List<Track>)

fun getOneTrack(disk: IDisk, track: Int): Track {
    val sectors = hashMapOf<Int, Sector>()
    val result = arrayListOf<IntArray>()
    fun pair() = disk.nextByte().shl(1).or(1).and(disk.nextByte()).and(0xff)
    repeat(16) { sector ->
        while (disk.peekBytes(3) != listOf(0xd5, 0xaa, 0x96)) {
            disk.nextByte()
        }
        val s = disk.nextBytes(3)
        val volume = pair()
        val readTrack = pair()
        if (track != -1 && readTrack != track) {
            ERROR("Tracks should match (expected track: ${track.h()}, got ${readTrack.h()} - sector: ${sector.h()}")
        }
        val sector = pair()
        val checksumAddress = pair()
        if (volume.xor(track).xor(sector) != checksumAddress) {
            ERROR("Checksum doesn't match")
        }
        println("Volume: $volume Track: $track Sector: $sector checksum: $checksumAddress")
        if (disk.nextBytes(3) != listOf(0xde, 0xaa, 0xeb)) {
            ERROR("Didn't find closing for address")
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
