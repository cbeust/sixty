package com.beust.sixty

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class SixtyTest {
    fun computer(vararg bytes: Int) = Computer(memory = Memory(*bytes))

    fun inxy() {
        // inx * 2, iny * 2
        computer(0xe8, 0xe8, 0xc8, 0xc8, 0).let { computer ->
            assertRegister(computer.cpu.X, 0)
            assertRegister(computer.cpu.Y, 0)
            computer.run()
            assertRegister(computer.cpu.X, 2)
            assertRegister(computer.cpu.Y, 2)
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
                assertNotRegister(cpu.A, 0x12)
                computer.run()
                assertRegister(cpu.A, 0x12)
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
            assertThat(computer.cpu.A).isNotEqualTo(expected.toUByte())
            computer.cpu.nextInstruction(computer).runDebug()
            assertThat(computer.cpu.A).isEqualTo(expected.toUByte())
        }
    }

    fun staZp() {
        // lda #$23
        val expected = 0x23
        computer(0xa9, expected, 0x85, 0x8, 0).let { computer ->
            assertThat(computer.memory.byte(0x8)).isNotEqualTo(expected.toUByte())
            computer.run()
            assertThat(computer.memory.byte(0x8)).isEqualTo(expected.toUByte())
        }
    }

    @Test(enabled = false)
    fun Computer.assertMemory(index: Int, value: Int) {
        assertThat(memory.byte(index).toInt()).isEqualTo(value)
    }

    fun StaIndY() {
        // memory(4) points to address 8, then we add Y(2) to it to produce 10. Store $42 in memory(10)
        // STA ($4), Y
        computer(0x91, 4, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0).let { computer ->
            assertThat(computer.memory.byte(10)).isNotEqualTo(0x42)
            computer.cpu.A = 0x42u
            computer.cpu.Y = 2u
            computer.cpu.nextInstruction(computer).runDebug()
            computer.assertMemory(10, 0x42)
        }
    }

    private fun cmpFlags(compared: Int, y: UByte, z: Int, c: Int, n: Int) {
        computer(0x44, compared).let { computer ->
            computer.cpu.Y = y
            computer.cpu.P.let {
                assertFlag(it.Z, 0)
                assertFlag(it.C, 0)
                assertFlag(it.N, 0)
            }
            CpyImm(computer).runDebug()
            computer.cpu.P.let {
                assertFlag(it.Z, z)
                assertFlag(it.C, c)
                assertFlag(it.N, n)
            }
        }
    }

    fun cpyImm() {
        // CPY #$80 with #$7F
        cmpFlags(0x80, 0x7fu, 0, 0, 1)

        // CPY #$FF with 1
        cmpFlags(0xff, 1u, 0, 0, 0)
    }

    fun bne() {
        computer(0xd0, 0, 0x90, 1, 0x60, 0xa9, 1, 0x60).let { computer ->
            assertRegister(computer.cpu.A, 0)
            computer.run()
            assertRegister(computer.cpu.A, 1)
        }
    }

    @Test(enabled = false)
    fun assertFlag(flag: UInt, expected: Int) {
        assertThat(flag.toInt()).isEqualTo(expected)
    }

    @Test(enabled = false)
    fun assertRegister(register: UByte, expected: Int) {
        assertThat(register.toInt()).isEqualTo(expected)
    }

    @Test(enabled = false)
    fun assertNotRegister(register: UByte, expected: Int) {
        assertThat(register.toInt()).isNotEqualTo(expected)
    }

    fun bcc() {
        computer(0xa9, 0, 0x90, 1, 0x60, 0xa9, 1, 0x60).let { computer ->
            assertRegister(computer.cpu.A, 0)
            computer.run()
            assertRegister(computer.cpu.A, 1)
        }
    }

    fun incZp() {
        computer(0xe6, 0x3, 0x60, 0x42).let { computer ->
            computer.assertMemory(3, 0x42)
            computer.run()
            computer.assertMemory(3, 0x43)
        }
    }

    @Test(enabled = true)
    fun fillingScreen() {
        val c = computer(0x4c, 5, 0, 0xea, 0xea, 0xa9, 0x00, 0x85, 0x3, 0xa9, 0x02, 0x85, 0x4,
                0xa0, 0xff, 0xa9, 0x28, 0x91, 0x3, 0xc8,
                0xc0, 0xff, // CPY #$ff
                0xd0, 0xf9, // BNE $11
                0xe6, 0x4,
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