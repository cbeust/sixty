package com.beust.sixty

import com.beust.app.BitPattern
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

@Test
class HiResScreenTest {
    /**
     * * PDCCBBAA PGGFFEED
     */
    @DataProvider
    fun bp() = arrayOf(
            arrayOf(0xdd, 0xbb, 1, 1, 1, 3, 1, 3, 1, 3, 1),
            arrayOf(0xf7, 0xee, 1, 1, 3, 1, 3, 2, 3, 1, 3)  // 1111_0111 1110_1110
    )

    @Test(dataProvider = "bp")
    fun bitPatterns(byte0: Int, byte1: Int, p0: Int, p1: Int, a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int) {
        val bp = BitPattern(byte0, byte1)
        assertThat(bp.aa).isEqualTo(a)
        assertThat(bp.bb).isEqualTo(b)
        assertThat(bp.cc).isEqualTo(c)
        assertThat(bp.dd).isEqualTo(d)
        assertThat(bp.ee).isEqualTo(e)
        assertThat(bp.ff).isEqualTo(f)
        assertThat(bp.gg).isEqualTo(g)
    }
}