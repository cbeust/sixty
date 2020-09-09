package com.beust.sixty

import com.beust.app.BitStream
import com.beust.app.IBitStream
import com.beust.app.Woz
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class WozTest {
    fun bits() {
        val ins = Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz")!!.openStream()
        val bytes: ByteArray = ins.readAllBytes()
        val slice = bytes.slice(0x600 until bytes.size)
        val bitStream = BitStream(slice.toByteArray())
        val bitStream2 = BitStream2(slice.toByteArray())
        repeat(slice.size) {
            assertThat(bitStream.next()).isEqualTo(bitStream2.next())
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

    override fun next(): Int {
        val result = bits[position]
        position = (position + 1) % bytes.size
        return result
    }

}