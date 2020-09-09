package com.beust.sixty

import com.beust.app.IBitStream
import com.beust.app.Woz
import org.testng.annotations.Test

@Test
class WozTest {
    fun bits() {
        val ins = Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz")!!.openStream()

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