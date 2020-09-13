package com.beust.sixty

import com.beust.app.ByteStream
import com.beust.app.DskDisk
import com.beust.app.getOneTrack
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class DskDiskTest {
    private val ins get() = DskDiskTest::class.java.classLoader.getResource("Apple DOS 3.3.dsk")!!.openStream()

    fun disk() {
        val expected = ins.readAllBytes()
        val d = DskDisk(ins)
        val bs = ByteStream(d.bitBuffer)
        val trackContent = getOneTrack(bs)
        repeat(1) { track ->
            repeat(16) { sector ->
                val sec = trackContent.sectors[sector]
                assertThat(sec.number).isEqualTo(sector)
                repeat(256) { byte ->
                    val actual = sec.content[byte]
                    val index = DskDisk.TRACK_SIZE_BYTES * track + sector * 256 + byte
                    val exp = expected[index].toUByte().toInt()
                    assertThat(actual)
                            .withFailMessage("T:$track S:$sector B:$byte  Expected ${exp.h()} but got ${actual.h()}")
                            .isEqualTo(exp)
                }
            }
        }
    }
}