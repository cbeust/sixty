package com.beust.sixty

import com.beust.app.LineCalculator
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

@Test
class TextScreenTest {
    private val calculator = LineCalculator()
    private val addresses = listOf(
            0x00, 0x80, 0x100, 0x180, 0x200, 0x280, 0x300, 0x380,
            0x28, 0xa8, 0x128, 0x1a8, 0x228, 0x2a8, 0x328, 0x3a8,
            0x50, 0xd0, 0x150, 0x1d0, 0x250, 0x2d0, 0x350, 0x3d0)

    @DataProvider
    fun dp(): Array<Array<Any>> {
        val result = Array<Array<Any>>(24) { Array(40) { 0 } }
        repeat(24) { y ->
            val start = addresses[y]
            repeat(40) { x ->
                val l = arrayListOf<Any>()
                l.add(x)
                l.add(y)
                val a = start + x + 0x400
                l.add(a)
                result[y] = l.toTypedArray()
            }
        }
        return result
    }

    @Test(dataProvider = "dp")
    fun coordinates(x: Int, y: Int, location: Int) {
        val p = calculator.coordinatesFor(location)
        assertThat(p!!.first).isEqualTo(x)
        assertThat(p.second).isEqualTo(y)
    }
}