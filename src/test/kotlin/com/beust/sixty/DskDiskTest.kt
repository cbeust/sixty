package com.beust.sixty

import com.beust.app.*
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test
import java.io.File

@Test
class DskDiskTest {
    private val ins get() = DskDiskTest::class.java.classLoader.getResource("Apple DOS 3.3.dsk")!!.openStream()
    val disk = DskDisk("Apple DOS 3.3.dsk", ins)

    fun sectorsAllDifferent() {
        repeat(3) { disk.nextByte() } // d5 aa 96
        repeat(2) { disk.nextByte() } // volume
        val (b0, b1) = disk.peekBytes(2)
        val t0 = SixAndTwo.pair4And4(b0, b1)
        disk.incPhase()
        val b2 = disk.nextByte()
        val b3 = disk.nextByte()
        val t1 = SixAndTwo.pair4And4(b2, b3)
        ""
    }

    @Test(enabled = true)
    fun disk() {
        val proDos = false
        val expected = ins.readAllBytes()
//        SixAndTwo.dump(disk)
        repeat(35) { track ->
            val trackContent = getOneTrack(disk, track)
            repeat(16) { sector ->
                val thisSec = trackContent.sectors[sector]!!//DskDisk.LOGICAL_SECTORS[sector]]!!
                val sec = thisSec.number
//                val logicalSector = if (sector == 15) 15 else ((sector * (if (proDos) 8 else 7)) % 15);
//                println("Track $track sector:$sector, logical: $sec")
                assertThat(sec)
                        .withFailMessage("Track $track: expected sector ${thisSec.number} but got ${sec}")
                        .isEqualTo(thisSec.number)
                repeat(256) { byte ->
                    val actual = thisSec.content[byte]
                    val index = DskDisk.TRACK_SIZE_BYTES * track + sec * 256 + byte
                    val exp = expected[index].toUByte().toInt()
//                    if (byte == 0) {
//                        println("T:${track.h()} S:${sec.h()} Received ${actual.h()}, expected ${exp.h()}")
//                    }
                    assertThat(actual)
                            .withFailMessage("T:$track S:$sec B:$byte  Expected ${exp.h()} but got ${actual.h()}")
                            .isEqualTo(exp)
                }
            }
            disk.incPhase()
            disk.incPhase()
        }
    }
}

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
        logDisk("Volume: $volume Track: $track Sector: $sector checksum: $checksumAddress")
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

fun testDisk() {
    val ins = Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz")!!.openStream()
    val ins2 = File("d:\\pd\\Apple DIsks\\woz2\\The Apple at Play.woz").inputStream()
    val disk: IDisk = WozDisk("The Apple at Play.woz", ins)
    getOneTrack(disk, 0)
}

