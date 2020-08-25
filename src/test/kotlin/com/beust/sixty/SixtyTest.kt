package com.beust.sixty

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class SixtyTest {
    fun computer(vararg bytes: Int) = Computer(memory = Memory(*bytes))

    fun inxy() {
        // inx * 2, iny * 2
        computer(0xe8, 0xe8, 0xc8, 0xc8, 0).let { computer ->
            assertThat(computer.cpu.X).isEqualTo(0)
            assertThat(computer.cpu.Y).isEqualTo(0)
            computer.run()
            assertThat(computer.cpu.X).isEqualTo(2)
            assertThat(computer.cpu.Y).isEqualTo(2)
        }
    }

    fun rts() {
        computer(
            0x20, 0x8, 0,   // jsr $8
            0xa9, 0x12,     // lda #$12
            0x60,
            0xea, 0xea,  // nop
            0x60   // rts
        ).let { computer ->
            computer.cpu.let { cpu ->
                assertThat(cpu.A).isNotEqualTo(0x12)
                computer.run()
                assertThat(cpu.A).isEqualTo(0x12)
            }
        }
    }

    fun jsr() {
        // jsr $1234
        computer(0x20, 0x34, 0x12, 0xEA, 0xEA, 0xEA).let { computer ->
            computer.cpu.let { cpu ->
                assertThat(cpu.SP.isEmpty())
                cpu.nextInstruction(computer).let { inst ->
                    inst.runDebug()
                    assertThat(cpu.PC).isEqualTo(0x1234 - inst.size)
                }
            }
        }
    }

    fun lda() {
        // lda #$23
        val expected = 0x23
        computer(0xa9, expected).let { computer ->
            assertThat(computer.cpu.A).isNotEqualTo(expected.toByte())
            computer.cpu.nextInstruction(computer).runDebug()
            assertThat(computer.cpu.A).isEqualTo(expected.toByte())
        }
    }

    fun staZp() {
        // lda #$23
        val expected = 0x23
        computer(0xa9, expected, 0x85, 0x8, 0).let { computer ->
            assertThat(computer.memory.byte(0x8)).isNotEqualTo(expected.toByte())
            computer.run()
            assertThat(computer.memory.byte(0x8)).isEqualTo(expected.toByte())
        }
    }

    fun StaIndY() {
        // memory(4) points to address 8, then we add Y(2) to it to produce 10. Store $42 in memory(10)
        // STA ($4), Y
        computer(0x91, 4, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0).let { computer ->
            assertThat(computer.memory.byte(10)).isNotEqualTo(0x42)
            computer.cpu.A = 0x42
            computer.cpu.Y = 2
            computer.cpu.nextInstruction(computer).runDebug()
            assertThat(computer.memory.byte(10)).isEqualTo(0x42)
        }
    }

    private fun cmpFlags(compared: Int, y: Byte, z: Int, c: Int, n: Int) {
        computer(0x44, compared).let { computer ->
            computer.cpu.Y = y
            computer.cpu.P.let {
                assertThat(it.Z).isEqualTo(0)
                assertThat(it.C).isEqualTo(0)
                assertThat(it.N).isEqualTo(0)
            }
            CpyImm(computer).runDebug()
            computer.cpu.P.let {
                assertThat(it.Z).isEqualTo(z)
                assertThat(it.C).isEqualTo(c)
                assertThat(it.N).isEqualTo(n)
            }
        }
    }

    fun cpyImm() {
        // CPY #$80 with #$7F
        cmpFlags(0x80, 0x7f, 0, 0, 1)

        // CPY #$FF with 1
        cmpFlags(0xff, 1, 0, 0, 0)
    }

    fun bne() {
        computer(0xea, 0xd0, -3).let { computer ->
            computer.run()
        }
    }
/*
$0600    a9 00     LDA #$00
$0602    85 10     STA $10
$0604    a9 02     LDA #$02
$0606    85 11     STA $11
$0608    a0 ff     LDY #$ff
$060a    a9 28     LDA #$28
$060c    91 10     STA ($10),Y
$060e    c8        INY
$060f    c0 ff     CPY #$ff
$0611    d0 f9     BNE $060c
$0613    e6 11     INC $11
$0615    a5 11     LDA $11
$0617    c9 06     CMP #$06
$0619    90 f1     BCC $060c

     */
}