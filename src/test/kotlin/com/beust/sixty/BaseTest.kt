package com.beust.sixty

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

fun assertFlag(n: String, flag: Boolean, expected: Int) {
    assertThat(flag.int()).withFailMessage("Expected flag $n to be $expected but found ${flag.int()}")
            .isEqualTo(expected)
}

fun assertRegister(register: Int, expected: Int) {
    assertThat(register).isEqualTo(expected)
}

fun assertNotRegister(register: Int, expected: Int) {
    assertThat(register).isNotEqualTo(expected)
}

@Test
abstract class BaseTest {
    abstract fun computer(vararg bytes: Int): Computer

    fun inxy() {
        // inx * 2, iny * 2
        with(computer(0xe8, 0xe8, 0xc8, 0xc8, 0)) {
            assertRegister(cpu.X, 0)
            assertRegister(cpu.Y, 0)
            run()
            assertRegister(cpu.X, 2)
            assertRegister(cpu.Y, 2)
        }
    }

    fun rts() {
        with(computer(
                0x20, 0x8, 0,   // jsr $8
                0xa9, 0x12,     // lda #$12
                0x60,
                0xea, 0xea,  // nop
                0x60   // rts
        )) {
            assertNotRegister(cpu.A, 0x12)
            run()
            assertRegister(cpu.A, 0x12)
        }
    }

    fun jsr() {
        // jsr $1234
        with(computer(0x20, 0x34, 0x12, 0xEA, 0xEA, 0xEA)) {
            assertThat(cpu.SP.isEmpty())
            cpu.nextInstruction(this).let { inst ->
                inst.run()
                assertThat(cpu.PC).isEqualTo(0x1234 - inst.size)
                // Need to test SP
            }
        }
    }

    fun lda() {
        // lda #$23
        val expected = 0x23
        with(computer(0xa9, expected)) {
            assertThat(cpu.A).isNotEqualTo(expected)
            cpu.nextInstruction(this).run()
            assertThat(cpu.A).isEqualTo(expected)
        }
    }

    fun staZp() {
        // lda #$23
        val expected = 0x23
        with(computer(0xa9, expected, 0x85, 0x8, 0)) {
            assertThat(memory[0x8]).isNotEqualTo(expected)
            run()
            assertThat(memory[0x8]).isEqualTo(expected)
        }
    }

    @Test(enabled = false)
    fun Computer.assertMemory(index: Int, value: Int) {
        assertThat(memory[index]).isEqualTo(value)
    }

    fun staIndY() {
        // memory(4) points to address 8, then we add Y(2) to it to produce 10. Store $42 in memory(10)
        // STA ($4), Y
        with(computer(0x91, 4, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0)) {
            assertThat(memory[10]).isNotEqualTo(0x42)
            cpu.A = 0x42
            cpu.Y = 2
            cpu.nextInstruction(this).run()
            assertMemory(10, 0x42)
        }
    }

    private fun cmpFlags(y: Int, compared: Int, n: Int, z: Int, c: Int) {
        with(computer(0xc0, compared)) {
            cpu.Y = y
            with(cpu.P) {
                assertFlag("N", N, 0)
                assertFlag("Z", Z, 0)
                assertFlag("C", C, 0)
            }
            CpyImm(this).run()
            with(cpu.P) {
                assertFlag("N", N, n)
                assertFlag("Z", Z, z)
                assertFlag("C", C, c)
            }
        }
    }

    fun cpyImm() {
        // LDY #$80, CPY #$7F
        cmpFlags(0x80, 0x7f, 0, 0, 1) // P: 0x31  N=0 Z=0 C=1

        // LDY #$1, CPY #$FF
        cmpFlags(1, 0xff, 0, 0, 0) // P: 0x31  N=0 Z=0 C=1
    }

    fun bne() {
        with(computer(0xd0, 0, 0x90, 1, 0x60, 0xa9, 1, 0x60)) {
            assertRegister(cpu.A, 0)
            run()
            assertRegister(cpu.A, 1)
        }
    }

    fun bpl() {
        with(computer(0xa9, 0x10, // lda #$10
                0xc9, 9,          // cmp #$9
                0x10, 1,          // bpl 7
                0x60,
                0xa9, 0x42,        // 0007: lda #$42
                0)) {
            assertRegister(cpu.A, 0)
            run()
            assertRegister(cpu.A, 0x42)
        }
    }

    fun bmi() {
        with(computer(0xa9, 0x10, // lda #$10
                0xc9, 0x11,       // cmp #$11
                0x30, 1,          // bmi 7
                0x60,
                0xa9, 0x42,        // 0007: lda #$42
                0)) {
            assertRegister(cpu.A, 0)
            run()
            assertRegister(cpu.A, 0x42)
        }
    }

    fun bcc() {
        with(computer(0xa9, 0, 0x90, 1, 0x60, 0xa9, 1, 0x60)) {
            assertRegister(cpu.A, 0)
            run()
            assertRegister(cpu.A, 1)
        }
    }

    fun incZp() {
        with(computer(0xe6, 0x3, 0x60, 0x42)) {
            assertMemory(3, 0x42)
            run()
            assertMemory(3, 0x43)
        }
    }

    @DataProvider(name = "a")
    fun adcImmProvider() = arrayOf(
            // N V . .  D I Z C
//            arrayOf(7, -2, 5, 0, 0, 1)  // expected 0x31  0011 0001 (001)
            arrayOf(7, 2, 9, 0, 0, 0) // 0x30 (000)
            , arrayOf(7, 0x80, 0x87, 1, 0, 0) // 0xb0 (100)
    )

    @Test(dataProvider = "a")
    fun adcImm(a: Int, valueToAdd: Int, expected: Int, n: Int, v: Int, c: Int) {
        with(computer(0x69, valueToAdd)) {
            cpu.A = a
            cpu.nextInstruction(this).run()
            assertRegister(cpu.A, expected)
            assertFlag("N", cpu.P.N, n)
            assertFlag("V", cpu.P.V, v)
            assertFlag("C", cpu.P.C, c)
        }
    }

    private fun assertMemory(memory: Memory, start: Int, end: Int, expected: Int) {
        (start..end).forEach {
            assertThat(memory[it])
                    .withFailMessage("Expected index $it to be $expected but was ${memory[it]}")
                    .isEqualTo(expected)
        }
    }

    /**
     * Fill the memory 0x200-0x3ff with value 0x28.
     */
    fun fillingScreen() {
        with(computer(0x4c, 5, 0, 0xea, 0xea,
                0xa9, 0x00, // LDA #0
                0x85, 0x3,  // STA #03
                0xa9, 0x02, // LDA #2
                0x85, 0x4,  // STA #04
                0xa0, 0x0,  // LDY 0
                0xa9, 0x28, // LDA #$28
                0x91, 0x3,  // STA ($03),Y
                0xc8,       // INY
                0xd0, 0xf9, // BNE $5
                0xe6, 0x4,  // INC $04
                0xa5, 0x4,  // LDA $04
                0xc9, 0x04, // CMP #$03
                0x90, 0xf1, // BCC $5
                0x60)) {

            // Make sure the entire memory is 0 before the run
            assertMemory(memory, 0x30, 0x1ff, 0)
            run()
            // Make sure only 0x200-0x3ff contains the value
            assertMemory(memory, 0x30, 0x1ff, 0)
            assertMemory(memory, 0x200, 0x3ff, 0x28)
            assertMemory(memory, 0x400, 0x500, 0)
        }
    }

    fun jmpIndirect() {
        with(computer(0xa9, 0, 0x6c, 5, 0, 7, 0, 0xa9, 0x42, 0)) {
            assertRegister(cpu.A, 0)
            run()
            assertRegister(cpu.A, 0x42)
        }
    }

    private fun storeAbsolute(loadOp: Int, storeOp: Int) {
        with(computer(loadOp, 0x42, storeOp, 0x34, 0x12, 0)) {
            assertMemory(0x1234, 0)
            run()
            assertMemory(0x1234, 0x42)
        }
    }

    fun staAbsolute() = storeAbsolute(0xa9, 0x8d)  // LDA #$42, STA $1234

    fun stxAbsolute() = storeAbsolute(0xa2, 0x8e)  // LDX #$42, STX $1234

    fun styAbsolute() = storeAbsolute(0xa0, 0x8c) // LDY #$42, STY $1234

    fun ldaIndx() {
        with(computer(0x4c, 6, 0, // JMP $0006
                0x56, 0x34, 0x12,
                0xa2, 1,   // 0006: LDX #1
                0xbd, 4, 0, // LDA $0004,X
                0)) {
            assertRegister(cpu.A, 0)
            run()
            assertRegister(cpu.A, 0x12)
        }
    }

    fun staZpX() {
        with(computer(0xa9, 0x42, 0xa2, 0x1, 0x95, 0xf0)) {
            memory[0xf0] = 0x12
            memory[0xf1] = 0x34
            memory[0xf2] = 0x56
            assertMemory(0xf1, 0x34)
            run()
            assertMemory(0xf1, 0x42)
        }
    }

    fun tax() {
        with(computer(0xa9, 0x42, 0xaa)) {
            assertRegister(cpu.A, 0)
            assertRegister(cpu.X, 0)
            run()
            assertRegister(cpu.A, 0x42)
            assertRegister(cpu.X, 0x42)
        }
    }

    fun tay() {
        with(computer(0xa9, 0x42, 0xa8)) {
            assertRegister(cpu.A, 0)
            assertRegister(cpu.Y, 0)
            run()
            assertRegister(cpu.A, 0x42)
            assertRegister(cpu.Y, 0x42)
        }
    }

    fun txa() {
        with(computer(0xa2, 0x42, 0x8a)) {
            assertRegister(cpu.A, 0)
            assertRegister(cpu.X, 0)
            run()
            assertRegister(cpu.A, 0x42)
            assertRegister(cpu.X, 0x42)
        }
    }

    fun tya() {
        with(computer(0xa0, 0x42, 0x98)) {
            assertRegister(cpu.A, 0)
            assertRegister(cpu.Y, 0)
            run()
            assertRegister(cpu.A, 0x42)
            assertRegister(cpu.Y, 0x42)
        }
    }

    fun dex() {
        with(computer(0xa2, 0x42, 0xca)) {
            assertRegister(cpu.X, 0)
            run()
            assertRegister(cpu.X, 0x41)
        }
        with(computer(0xa2, 0x00, 0xca)) {
            assertRegister(cpu.X, 0)
            run()
            assertRegister(cpu.X.and(0xff), 0xff)
        }
    }

    fun dey() {
        with(computer(0xa0, 0x42, 0x88)) {
            assertRegister(cpu.Y, 0)
            run()
            assertRegister(cpu.Y, 0x41)
        }
        with(computer(0xa0, 0x00, 0x88)) {
            assertRegister(cpu.Y, 0)
            run()
            assertRegister(cpu.Y.and(0xff), 0xff)
        }
    }

    fun ldaAbsolute() {
        with(computer(0xad, 0x34, 0x12)) {
            memory[0x1234] = 0xa0
            assertRegister(cpu.A, 0)
            run()
            assertRegister(cpu.A, 0xa0)
        }
    }

    fun asl() {
        with(computer(0xa9, 2, 0xa)) {
            assertRegister(cpu.A, 0)
            run()
            assertRegister(cpu.A, 4)
            assertFlag("N", cpu.P.N, 0)
            assertFlag("Z", cpu.P.Z, 0)
            assertFlag("C", cpu.P.C, 0)
        }
        with(computer(0xa9, 0xff, 0xa)) { // LDA #$ff, ASL
            assertRegister(cpu.A, 0)
            run()
            assertRegister(cpu.A, 0xfe)
            assertFlag("N", cpu.P.N, 1)
            assertFlag("Z", cpu.P.Z, 0)
            assertFlag("C", cpu.P.C, 1)
        }
    }

    fun pha() {
        with(computer(0xa9, 0x42, 0x48)) {
            assertThat(cpu.SP.isEmpty())
            run()
            assertThat(cpu.SP.peekByte()).isEqualTo(0x42)
        }
    }

    fun pla() {
        with(computer(0xa9, 0x42, 0x48, 0xa9, 0, 0x68)) {
            assertThat(cpu.SP.isEmpty())
            run()
            assertRegister(cpu.A, 0x42)
        }
    }

    fun txs() {
        with(computer(0xa2, 0x42, 0x9a)) {
            assertThat(cpu.SP.isEmpty())
            run()
            assertThat(cpu.SP.S).isEqualTo(0x42)
        }
    }

    // Need tests for PHP and PLP
}

