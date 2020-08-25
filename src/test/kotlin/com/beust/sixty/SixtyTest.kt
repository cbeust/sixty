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
        computer(0xd0, 0, 0x90, 1, 0x60, 0xa9, 1, 0x60).let { computer ->
            assertThat(computer.cpu.A).isEqualTo(0)
            computer.run()
            assertThat(computer.cpu.A).isEqualTo(1)
        }
    }

    fun bcc() {
        computer(0xa9, 0, 0x90, 1, 0x60, 0xa9, 1, 0x60).let { computer ->
            assertThat(computer.cpu.A).isEqualTo(0)
            computer.run()
            assertThat(computer.cpu.A).isEqualTo(1)
        }
    }

    fun incZp() {
        computer(0xe6, 0x3, 0x60, 0x42).let { computer ->
            assertThat(computer.memory.byte(3)).isEqualTo(0x42)
            computer.run()
            assertThat(computer.memory.byte(3)).isEqualTo(0x43)
        }
    }

    fun fillingScreen() {
        val c = computer(0x4c, 5, 0, 0xea, 0xea, 0xa9, 0x00, 0x85, 0x3, 0xa9, 0x02, 0x85, 0x4,
                0xa0, 0xff, 0xa9, 0x28, 0x91, 0x3, 0xc8,
                0xc0, 0xff, // CPY #$ff
                0xd0, 0xf9, 0xe6, 0x4,
                0xa5, 0x3,
                0xc9, 0x03,
                0x90, 0xf1,
                0x60)
//        c.disassemble()
        c.run()
    }
/*

Address  Hexdump   Dissassembly
-------------------------------
$0600    4c 05 06  JMP $0605
$0603    ea        NOP
$0604    ea        NOP
$0605    a9 00     LDA #$00
$0607    8d 01 06  STA $0601
$060a    a9 02     LDA #$02
$060c    8d 02 06  STA $0602
$060f    a0 ff     LDY #$ff
$0611    a9 02     LDA #$02
$0613    91 03     STA ($03),Y
$0615    c8        INY
$0616    c0 ff     CPY #$ff
$0618    d0 f9     BNE $0613
$061a    ee 02 06  INC $0602
$061d    a5 03     LDA $03
$061f    c9 06     CMP #$06
$0621    90 f0     BCC $0613


jmp start
nop
nop
start:
lda #$00
sta $601
lda #$02
sta $602
ldy #$ff
lda #$2
loop:
sta ($3),y
iny
cpy #$ff
bne loop
inc $602
lda $3
cmp #6
bcc loop

     */
}