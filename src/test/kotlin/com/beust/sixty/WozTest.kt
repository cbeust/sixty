package com.beust.sixty

import com.beust.app.BitBitStream
import com.beust.app.IBitStream
import com.beust.app.Woz
import com.beust.app.WozDisk
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class WozTest {
    fun bits() {
        val ins = Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz")!!.openStream()
        val bytes: ByteArray = ins.readAllBytes()
        val slice = bytes.slice(0x600 until bytes.size)
        val bitStream = BitBitStream(slice)
        val bitStream2 = BitStream2(slice.toByteArray())
        var position1 = 0
        var position2 = 0
        repeat(slice.size) {
            val (a, b) = bitStream.next(position1)
            val (c, d) = bitStream2.next(position2)
            assertThat(b).isEqualTo(d)
            position1 = a
            position2 = c
        }
    }

    fun bytes() {
        val ins = Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz")!!.openStream()
        val disk = WozDisk("DOS 3.3.woz", ins)

        val ins2 = Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz")!!.openStream()
        val bytes: ByteArray = ins2.readAllBytes()
        val size = bytes.size - 0x600
        var position1 = 0
        repeat(35) { track ->
            val bitStream = disk.bitStream
            repeat(size) {
                var latch = 0
                val byte = disk.nextByte()
                if (latch and 0x80 != 0) latch = 0
                while (latch and 0x80 == 0) {
                    val (newPosition, bit) = bitStream.next(position1)
                    latch = latch.shl(1).or(bit)
                    position1 = newPosition
                }
                val byte2 = latch
                assertThat(byte)
                        .withFailMessage("Failure at track $track, position $position1")
                        .isEqualTo(byte2)
            }
            disk.incTrack()
            disk.incTrack()
        }
    }
}

class BitStream2(val bytes: ByteArray): IBitStream() {
    private val bits = arrayListOf<Int>()

    init {
        bytes.forEach { b ->
            var i = 7
            repeat(8) {
                bits.add(b.bit(i--))
            }
        }
    }

    override fun next(position: Int): Pair<Int, Int> {
        val result = bits[position]
        val newPosition = (position + 1) % bytes.size
        return Pair(newPosition, result)
    }

}