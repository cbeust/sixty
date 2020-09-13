package com.beust.sixty

import com.beust.app.ByteStream
import com.beust.app.DskDisk
import com.beust.app.SixAndTwo
import com.beust.app.getOneTrack
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class DskDiskTest {
    private val ins get() = DskDiskTest::class.java.classLoader.getResource("Apple DOS 3.3.dsk")!!.openStream()

    fun sectorsAllDifferent() {
        val disk = DskDisk(ins)
        repeat(3) { disk.nextByte() } // d5 aa 96
        repeat(2) { disk.nextByte() } // volume
        val (b0, b1) = disk.peekBytes(2)
        val t0 = SixAndTwo.pair4And4(b0, b1)
        disk.incTrack()
        val b2 = disk.nextByte()
        val b3 = disk.nextByte()
        val t1 = SixAndTwo.pair4And4(b2, b3)
        ""
    }

    fun disk() {
        val proDos = false
        val expected = ins.readAllBytes()
        val disk = DskDisk(ins)
        SixAndTwo.dump(disk)
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
                    if (byte == 0) {
                        println("Comparing $actual with $exp")
                    }
                    assertThat(actual)
                            .withFailMessage("T:$track S:$sec B:$byte  Expected ${exp.h()} but got ${actual.h()}")
                            .isEqualTo(exp)
                }
            }
            disk.incTrack()
        }
    }
}