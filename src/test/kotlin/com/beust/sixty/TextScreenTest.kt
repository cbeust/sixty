package com.beust.sixty

import com.beust.app.LineCalculator
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

@Test
class TextScreenTest {
    private val calculator = LineCalculator()

    @DataProvider
    fun dp() = arrayOf(
            arrayOf(0, 0, 0x400),
            arrayOf(1, 1, 0x481),
            arrayOf(2, 2, 0x502),
            arrayOf(3, 3, 0x583),
            arrayOf(4, 4, 0x604),
            arrayOf(39, 23, 0x7f7)
    )

    @Test(dataProvider = "dp")
    fun coordinates(x: Int, y: Int, location: Int) {
        val p = calculator.coordinatesFor(location)
        assertThat(p!!.first).isEqualTo(x)
        assertThat(p!!.second).isEqualTo(y)
    }
}