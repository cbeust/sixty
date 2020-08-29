package com.beust.sixty

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import kotlin.math.exp

fun computer(vararg bytes: Int) = Computer(memory = Memory(4096, *bytes))

fun assertFlag(n: String, flag: Boolean, expected: Int) {
    assertThat(flag.int()).isEqualTo(expected).withFailMessage("Flag $n")
}

fun assertRegister(register: Int, expected: Int) {
    assertThat(register).isEqualTo(expected)
}

fun assertNotRegister(register: Int, expected: Int) {
    assertThat(register).isNotEqualTo(expected)
}

@Test
class SixtyTest {
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
        assertThat(memory[index].toInt()).isEqualTo(value)
    }

    fun StaIndY() {
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
        val c = computer(0x4c, 5, 0, 0xea, 0xea,
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
                0x60)

        // Make sure the entire memory is 0 before the run
        assertMemory(c.memory, 0x30, 0x1ff, 0)
        c.run()
        // Make sure only 0x200-0x3ff contains the value
        assertMemory(c.memory, 0x30, 0x1ff, 0)
        assertMemory(c.memory, 0x200, 0x3ff, 0x28)
        assertMemory(c.memory, 0x400, 0x500, 0)
    }

    fun jmpIndirect() {
        with(computer(0xa9, 0, 0x6c, 5, 0, 7, 0, 0xa9, 0x42, 0)) {
            assertRegister(cpu.A, 0)
//            disassemble()
            run()
            assertRegister(cpu.A, 0x42)
        }
    }
}
