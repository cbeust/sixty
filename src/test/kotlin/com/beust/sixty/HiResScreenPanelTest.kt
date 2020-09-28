package com.beust.sixty

import com.beust.app.BitPattern
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

@Test
class HiResScreenPanelTest {
    /**
     * * PDCCBBAA PGGFFEED
     */
    @DataProvider
    fun bp() = arrayOf(
            arrayOf(0xf7, 0xee, 1, 1, 3, 1, 3, 1, 3, 1, 3)  // 1111_0111 1110_1110
            , arrayOf(0xdd, 0xbb, 1, 1, 1, 3, 1, 3, 1, 3, 1)
            , arrayOf(0x6e, 0x00, 0, 0, 2, 3, 2, 1, 0, 0, 0) // 0110_1110 0000_0000
            , arrayOf(0xe0, 0x00, 1, 0, 0, 0, 2, 1, 0, 0, 0)  // 1110_0000 0000_0000
    )

    @Test(dataProvider = "bp")
    fun bitPatterns(byte0: Int, byte1: Int, p0: Int, p1: Int, a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int) {
        with(BitPattern(byte0, byte1)) {
            assertThat(this.p0).isEqualTo(p0)
            assertThat(this.p1).isEqualTo(p1)
            assertThat(aa).isEqualTo(a)
            assertThat(bb).isEqualTo(b)
            assertThat(cc).isEqualTo(c)
            assertThat(dd).isEqualTo(d)
            assertThat(ee).isEqualTo(e)
            assertThat(ff).isEqualTo(f)
            assertThat(gg).isEqualTo(g)
        }
    }
}