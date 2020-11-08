package com.beust.sixty
import com.beust.sixty.AddressingType.*

const val BRK = 0x00

const val ADC_IMM = 0x69
const val ADC_ZP = 0x65
const val ADC_ZP_X = 0x75
const val ADC_ABS = 0x6d
const val ADC_ABS_X = 0x7d
const val ADC_ABS_Y = 0x79
const val ADC_IND_X = 0x61
const val ADC_IND_Y = 0x71

const val AND_IMM = 0x29
const val AND_ZP = 0x25
const val AND_ZP_X = 0x35
const val AND_ABS = 0x2d
const val AND_ABS_X = 0x3d
const val AND_ABS_Y = 0x39
const val AND_IND_X = 0x21
const val AND_IND_Y = 0x31

const val ASL = 0x0a
const val ASL_ZP = 0x06
const val ASL_ZP_X = 0x16
const val ASL_ABS = 0x0e
const val ASL_ABS_X = 0x1e

const val BIT_ZP = 0x24
const val BIT_ABS = 0x2c

const val BPL = 0x10
const val BMI = 0x30
const val BVC = 0x50
const val BVS = 0x70
const val BCC = 0x90
const val BCS = 0xb0
const val BNE = 0xd0
const val BEQ = 0xf0

const val CPX_IMM = 0xe0
const val CPX_ZP = 0xe4
const val CPX_ABS = 0xec

const val CLC = 0x18
const val SEC = 0x38
const val CLI = 0x58
const val SEI = 0x78
const val CLV = 0xb8
const val CLD = 0xd8
const val SED = 0xf8

const val CMP_IMM = 0xc9
const val CMP_ZP = 0xc5
const val CMP_ZP_X = 0xd5
const val CMP_ABS = 0xcd
const val CMP_ABS_X = 0xdd
const val CMP_ABS_Y = 0xd9
const val CMP_IND_X = 0xc1
const val CMP_IND_Y = 0xd1

const val CPY_IMM = 0xc0
const val CPY_ZP = 0xc4
const val CPY_ABS = 0xcc

const val DEC_ZP = 0xc6
const val DEC_ZP_X = 0xd6
const val DEC_ABS = 0xce
const val DEC_ABS_X = 0xde

const val EOR_IMM = 0x49
const val EOR_ZP = 0x45
const val EOR_ZP_X = 0x55
const val EOR_ABS = 0x4d
const val EOR_ABS_X = 0x5d
const val EOR_ABS_Y = 0x59
const val EOR_IND_X = 0x41
const val EOR_IND_Y = 0x51

const val INC_ZP = 0xe6
const val INC_ZP_X = 0xf6
const val INC_ABS = 0xee
const val INC_ABS_X = 0xfe

const val JMP = 0x4c
const val JMP_IND = 0x6c
const val JSR = 0x20

const val LDA_IMM = 0xa9
const val LDA_ZP = 0xa5
const val LDA_ZP_X = 0xb5
const val LDA_ABS = 0xad
const val LDA_ABS_X = 0xbd
const val LDA_ABS_Y = 0xb9
const val LDA_IND_X = 0xa1
const val LDA_IND_Y = 0xb1

const val LDX_IMM = 0xa2
const val LDX_ZP = 0xa6
const val LDX_ZP_Y = 0xb6
const val LDX_ABS = 0xae
const val LDX_ABS_Y = 0xbe

const val LDY_IMM = 0xa0
const val LDY_ZP = 0xa4
const val LDY_ZP_X = 0xb4
const val LDY_ABS = 0xac
const val LDY_ABS_X = 0xbc

const val LSR = 0x4a
const val LSR_ZP = 0x46
const val LSR_ZP_X = 0x56
const val LSR_ABS = 0x4e
const val LSR_ABS_X = 0x5e

const val ORA_IMM = 0x09
const val ORA_ZP = 0x05
const val ORA_ZP_X = 0x15
const val ORA_ABS = 0x0d
const val ORA_ABS_X = 0x1d
const val ORA_ABS_Y = 0x19
const val ORA_IND_X = 0x01
const val ORA_IND_Y = 0x11

const val NOP = 0xea

const val ROL = 0x2a
const val ROL_ZP = 0x26
const val ROL_ZP_X = 0x36
const val ROL_ABS = 0x2e
const val ROL_ABS_X = 0x3e

const val ROR = 0x6a
const val ROR_ZP = 0x66
const val ROR_ZP_X = 0x76
const val ROR_ABS = 0x6e
const val ROR_ABS_X = 0x7e

const val RTI = 0x40

const val STA_ZP = 0x85
const val STA_ZP_X = 0x95
const val STA_ABS = 0x8d
const val STA_ABS_X = 0x9d
const val STA_ABS_Y = 0x99
const val STA_IND_X = 0x81
const val STA_IND_Y = 0x91

const val STY_ZP = 0x84
const val STY_ZP_X = 0x94
const val STY_ABS = 0x8c

const val STX_ZP = 0x86
const val STX_ZP_Y = 0x96
const val STX_ABS = 0x8e

const val TXS = 0x9a
const val TSX = 0xba
const val PHA = 0x48
const val PLA = 0x68
const val PHP = 0x08
const val PLP = 0x28

const val SBC_IMM = 0xe9
const val SBC_ZP = 0xe5
const val SBC_ZP_X = 0xf5
const val SBC_ABS = 0xed
const val SBC_ABS_X = 0xfd
const val SBC_ABS_Y = 0xf9
const val SBC_IND_X = 0xe1
const val SBC_IND_Y = 0xf1

const val TAX = 0xaa
const val TXA = 0x8a
const val DEX = 0xca
const val INX = 0xe8
const val TAY = 0xa8
const val TYA = 0x98
const val DEY = 0x88
const val INY = 0xc8

const val RTS = 0x60

// NMI vector
//        const val NMI_VECTOR_L = 0xfffa
//        const val NMI_VECTOR_H = 0xfffb
//
//        // Reset vector
//        const val RST_VECTOR_L = 0xfffc
//        const val RST_VECTOR_H = 0xfffd

// IRQ vector
const val IRQ_VECTOR_L = 0xfffe
const val IRQ_VECTOR_H = 0xffff

class Opcode(val opcode: Int, val name: String, val size: Int, val timing: Int, val addressingType: AddressingType)

val OPCODES = listOf(
        Opcode(BRK, "BRK", 1, 7, NONE),
        Opcode(ORA_IND_X, "ORA", 2, 6, INDIRECT_X),
        Opcode(0x02, "NOP", 2, 2, NONE),
        Opcode(0x03, "NOP", 1, 1, NONE),
        Opcode(0x04, "TSB", 2, 5, ZP),
        Opcode(ORA_ZP, "ORA", 2, 3, ZP),
        Opcode(ASL_ZP, "ASL", 2, 5, ZP),
        Opcode(0x07, "RMB0", 2, 5, ZP),
        Opcode(PHP, "PHP", 1, 3, NONE),
        Opcode(ORA_IMM, "ORA", 2, 2, IMMEDIATE),
        Opcode(ASL, "ASL", 1, 2, REGISTER_A),
        Opcode(0x0b, "NOP", 1, 1, NONE),
        Opcode(0x0c, "TSB", 3, 6, ABSOLUTE),
        Opcode(ORA_ABS, "ORA", 3, 4, ABSOLUTE),
        Opcode(ASL_ABS, "ASL", 3, 6, ABSOLUTE),
        Opcode(0x0f, "BBR0", 3, 5, RELATIVE),
        Opcode(BPL, "BPL", 2, 2, RELATIVE),
        Opcode(ORA_IND_Y, "ORA", 2, 5, INDIRECT_Y),
        Opcode(0x12, "ORA", 2, 5, ZPI),
        Opcode(0x13, "NOP", 1, 1, NONE),
        Opcode(0x14, "TRB", 2, 5, ZP),
        Opcode(ORA_ZP_X, "ORA", 2, 4, ZP_X),
        Opcode(ASL_ZP_X, "ASL", 2, 6, ZP_X),
        Opcode(0x17, "RMB1", 2, 5, ZP),
        Opcode(CLC, "CLC", 1, 2, NONE),
        Opcode(ORA_ABS_Y, "ORA", 3, 4, ABSOLUTE_Y),
        Opcode(0x1a, "INC", 1, 2, NONE),
        Opcode(0x1b, "NOP", 1, 1, NONE),
        Opcode(0x1c, "TRB", 3, 6, ABSOLUTE),
        Opcode(ORA_ABS_X, "ORA", 3, 4, ABSOLUTE_X),
        Opcode(ASL_ABS_X, "ASL", 3, 7, ABSOLUTE_X),
        Opcode(0x1f, "BBR1", 3, 5, RELATIVE),
        Opcode(JSR, "JSR", 3, 6, ABSOLUTE),
        Opcode(AND_IND_X, "AND", 2, 6, INDIRECT_X),
        Opcode(0x22, "NOP", 2, 2, NONE),
        Opcode(0x23, "NOP", 1, 1, NONE),
        Opcode(BIT_ZP, "BIT", 2, 3, ZP),
        Opcode(AND_ZP, "AND", 2, 3, ZP),
        Opcode(ROL_ZP, "ROL", 2, 5, ZP),
        Opcode(0x27, "RMB2", 2, 5, ZP),
        Opcode(PLP, "PLP", 1, 4, NONE),
        Opcode(AND_IMM, "AND", 2, 2, IMMEDIATE),
        Opcode(ROL, "ROL", 1, 2, REGISTER_A),
        Opcode(0x2b, "NOP", 1, 1, NONE),
        Opcode(BIT_ABS, "BIT", 3, 4, ABSOLUTE),
        Opcode(AND_ABS, "AND", 3, 4, ABSOLUTE),
        Opcode(ROL_ABS, "ROL", 3, 6, ABSOLUTE),
        Opcode(0x2f, "BBR2", 3, 5, RELATIVE),
        Opcode(BMI, "BMI", 2, 2, RELATIVE),
        Opcode(AND_IND_Y, "AND", 2, 5, INDIRECT_Y),
        Opcode(0x32, "AND", 2, 5, ZPI),
        Opcode(0x33, "NOP", 1, 1, NONE),
        Opcode(0x34, "BIT", 2, 4, ZP_X),
        Opcode(AND_ZP_X, "AND", 2, 4, ZP_X),
        Opcode(ROL_ZP_X, "ROL", 2, 6, ZP_X),
        Opcode(0x37, "RMB3", 2, 5, ZP),
        Opcode(SEC, "SEC", 1, 2, NONE),
        Opcode(AND_ABS_Y, "AND", 3, 4, ABSOLUTE_Y),
        Opcode(0x3a, "DEC", 1, 2, NONE),
        Opcode(0x3b, "NOP", 1, 1, NONE),
        Opcode(0x3c, "BIT", 3, 4, NONE),
        Opcode(AND_ABS_X, "AND", 3, 4, ABSOLUTE_X),
        Opcode(ROL_ABS_X, "ROL", 3, 7, ABSOLUTE_X),
        Opcode(0x3f, "BBR3", 3, 5, RELATIVE),
        Opcode(RTI, "RTI", 1, 6, NONE),
        Opcode(EOR_IND_X, "EOR", 2, 6, INDIRECT_X),
        Opcode(0x42, "NOP", 2, 2, NONE),
        Opcode(0x43, "NOP", 1, 1, NONE),
        Opcode(0x44, "NOP", 2, 2, NONE),
        Opcode(EOR_ZP, "EOR", 2, 3, ZP),
        Opcode(LSR_ZP, "LSR", 2, 5, ZP),
        Opcode(0x47, "RMB4", 2, 3, ZP),
        Opcode(PHA, "PHA", 1, 3, NONE),
        Opcode(EOR_IMM, "EOR", 2, 2, IMMEDIATE),
        Opcode(LSR, "LSR", 1, 2, REGISTER_A),
        Opcode(0x4b, "NOP", 1, 1, NONE),
        Opcode(JMP, "JMP", 3, 3, ABSOLUTE),
        Opcode(EOR_ABS, "EOR", 3, 4, ABSOLUTE),
        Opcode(LSR_ABS, "LSR", 3, 6, ABSOLUTE),
        Opcode(0x4f, "BBR4", 3, 5, RELATIVE),
        Opcode(BVC, "BVC", 2, 2, RELATIVE),
        Opcode(EOR_IND_Y, "EOR", 2, 5, INDIRECT_Y),
        Opcode(0x52, "EOR", 2, 5, ZPI),
        Opcode(0x53, "NOP", 1, 1, NONE),
        Opcode(0x54, "NOP", 2, 4, NONE),
        Opcode(EOR_ZP_X, "EOR", 2, 4, ZP_X),
        Opcode(LSR_ZP_X, "LSR", 2, 6, ZP_X),
        Opcode(0x57, "RMB5", 2, 5, ZP),
        Opcode(CLI, "CLI", 1, 2, NONE),
        Opcode(EOR_ABS_Y, "EOR", 3, 4, ABSOLUTE_Y),
        Opcode(0x5a, "PHY", 1, 3, NONE),
        Opcode(0x5b, "NOP", 1, 1, NONE),
        Opcode(0x5c, "NOP", 3, 8, NONE),
        Opcode(EOR_ABS_X, "EOR", 3, 4, ABSOLUTE_X),
        Opcode(LSR_ABS_X, "LSR", 3, 7, ABSOLUTE_X),
        Opcode(0x5f, "BBR5", 3, 5, RELATIVE),
        Opcode(RTS, "RTS", 1, 6, NONE),
        Opcode(ADC_IND_X, "ADC", 2, 6, INDIRECT_X),
        Opcode(0x62, "NOP", 2, 2, NONE),
        Opcode(0x63, "NOP", 1, 1, NONE),
        Opcode(0x64, "STZ", 2, 3, ZP),
        Opcode(ADC_ZP, "ADC", 2, 3, ZP),
        Opcode(ROR_ZP, "ROR", 2, 5, ZP),
        Opcode(0x67, "RMB6", 2, 5, ZP),
        Opcode(PLA, "PLA", 1, 4, NONE),
        Opcode(ADC_IMM, "ADC", 2, 2, IMMEDIATE),
        Opcode(ROR, "ROR", 1, 2, REGISTER_A),
        Opcode(0x6b, "NOP", 1, 1, NONE),
        Opcode(JMP_IND, "JMP", 3, 5, INDIRECT),
        Opcode(ADC_ABS, "ADC", 3, 4, ABSOLUTE),
        Opcode(ROR_ABS, "ROR", 3, 6, ABSOLUTE),
        Opcode(0x6f, "BBR6", 3, 5, RELATIVE),
        Opcode(BVS, "BVS", 2, 2, RELATIVE),
        Opcode(ADC_IND_Y, "ADC", 2, 5, INDIRECT_Y),
        Opcode(0x72, "ADC", 2, 5, ZPI),
        Opcode(0x73, "NOP", 1, 1, NONE),
        Opcode(0x74, "STZ", 2, 4, ZP_X),
        Opcode(ADC_ZP_X, "ADC", 2, 4, ZP_X),
        Opcode(ROR_ZP_X, "ROR", 2, 6, ZP_X),
        Opcode(0x77, "RMB7", 2, 5, ZP),
        Opcode(SEI, "SEI", 1, 2, NONE),
        Opcode(ADC_ABS_Y, "ADC", 3, 4, ABSOLUTE_Y),
        Opcode(0x7a, "PLY", 1, 4, NONE),
        Opcode(0x7b, "NOP", 1, 3, NONE),
        Opcode(0x7c, "JMP", 3, 6, AIX),
        Opcode(ADC_ABS_X, "ADC", 3, 4, ABSOLUTE_X),
        Opcode(ROR_ABS_X, "ROR", 3, 7, ABSOLUTE_X),
        Opcode(0x7f, "BBR7", 3, 5, RELATIVE),
        Opcode(0x80, "BRA", 2, 3, RELATIVE),
        Opcode(STA_IND_X, "STA", 2, 6, INDIRECT_X),
        Opcode(0x82, "NOP", 2, 2, NONE),
        Opcode(0x83, "NOP", 1, 1, NONE),
        Opcode(STY_ZP, "STY", 2, 3, ZP),
        Opcode(STA_ZP, "STA", 2, 3, ZP),
        Opcode(STX_ZP, "STX", 2, 3, ZP),
        Opcode(0x87, "SMB0", 2, 5, ZP),
        Opcode(DEY, "DEY", 1, 2, NONE),
        Opcode(0x89, "BIT", 2, 2, NONE),
        Opcode(TXA, "TXA", 1, 2, NONE),
        Opcode(0x8b, "NOP", 1, 1, NONE),
        Opcode(STY_ABS, "STY", 3, 4, ABSOLUTE),
        Opcode(STA_ABS, "STA", 3, 4, ABSOLUTE),
        Opcode(STX_ABS, "STX", 3, 4, ABSOLUTE),
        Opcode(0x8f, "BBS0", 3, 5, RELATIVE),
        Opcode(BCC, "BCC", 2, 2, RELATIVE),
        Opcode(STA_IND_Y, "STA", 2, 6, INDIRECT_Y),
        Opcode(0x92, "STA", 2, 5, ZPI),
        Opcode(0x93, "NOP", 1, 1, NONE),
        Opcode(STY_ZP_X, "STY", 2, 4, ZP_X),
        Opcode(STA_ZP_X, "STA", 2, 4, ZP_X),
        Opcode(STX_ZP_Y, "STX", 2, 4, ZP_Y),
        Opcode(0x97, "SMB1", 2, 5, ZP),
        Opcode(TYA, "TYA", 1, 2, NONE),
        Opcode(STA_ABS_Y, "STA", 3, 5, ABSOLUTE_Y),
        Opcode(TXS, "TXS", 1, 2, NONE),
        Opcode(0x9b, "NOP", 1, 1, NONE),
        Opcode(0x9c, "STZ", 3, 4, ABSOLUTE),
        Opcode(STA_ABS_X, "STA", 3, 5, ABSOLUTE_X),
        Opcode(0x9e, "STZ", 3, 5, ABSOLUTE_X),
        Opcode(0x9f, "BBS1", 3, 5, RELATIVE),
        Opcode(LDY_IMM, "LDY", 2, 2, IMMEDIATE),
        Opcode(LDA_IND_X, "LDA", 2, 6, INDIRECT_X),
        Opcode(LDX_IMM, "LDX", 2, 2, IMMEDIATE),
        Opcode(0xa3, "NOP", 1, 1, NONE),
        Opcode(LDY_ZP, "LDY", 2, 3, ZP),
        Opcode(LDA_ZP, "LDA", 2, 3, ZP),
        Opcode(LDX_ZP, "LDX", 2, 3, ZP),
        Opcode(0xa7, "SMB2", 2, 5, ZP),
        Opcode(TAY, "TAY", 1, 2, NONE),
        Opcode(LDA_IMM, "LDA", 2, 2, IMMEDIATE),
        Opcode(TAX, "TAX", 1, 2, NONE),
        Opcode(0xab, "NOP", 1, 1, NONE),
        Opcode(0xac, "LDY", 3, 4, ABSOLUTE),
        Opcode(LDA_ABS, "LDA", 3, 4, ABSOLUTE),
        Opcode(LDX_ABS, "LDX", 3, 4, ABSOLUTE),
        Opcode(0xaf, "BBS2", 3, 5, RELATIVE),
        Opcode(BCS, "BCS", 2, 2, RELATIVE),
        Opcode(LDA_IND_Y, "LDA", 2, 5, INDIRECT_Y),
        Opcode(0xb2, "LDA", 2, 5, ZPI),
        Opcode(0xb3, "NOP", 1, 1, NONE),
        Opcode(LDY_ZP_X, "LDY", 2, 4, ZP_X),
        Opcode(LDA_ZP_X, "LDA", 2, 4, ZP_X),
        Opcode(LDX_ZP_Y, "LDX", 2, 4, ZP_Y),
        Opcode(0xb7, "SMB3", 2, 5, ZP),
        Opcode(CLV, "CLV", 1, 2, NONE),
        Opcode(LDA_ABS_Y, "LDA", 3, 4, ABSOLUTE_Y),
        Opcode(TSX, "TSX", 1, 2, NONE),
        Opcode(0xbb, "NOP", 1, 1, NONE),
        Opcode(LDY_ABS_X, "LDY", 3, 4, ABSOLUTE_X),
        Opcode(LDA_ABS_X, "LDA", 3, 4, ABSOLUTE_X),
        Opcode(0xbe, "LDX", 3, 4, ABSOLUTE_Y),
        Opcode(0xbf, "BBS3", 3, 5, RELATIVE),
        Opcode(CPY_IMM, "CPY", 2, 2, IMMEDIATE),
        Opcode(CMP_IND_X, "CMP", 2, 6, INDIRECT_X),
        Opcode(0xc2, "NOP", 2, 2, NONE),
        Opcode(0xc3, "NOP", 1, 1, NONE),
        Opcode(CPY_ZP, "CPY", 2, 3, ZP),
        Opcode(CMP_ZP, "CMP", 2, 3, ZP),
        Opcode(DEC_ZP, "DEC", 2, 5, ZP),
        Opcode(0xc7, "SMB4", 2, 5, ZP),
        Opcode(INY, "INY", 1, 2, NONE),
        Opcode(CMP_IMM, "CMP", 2, 2, IMMEDIATE),
        Opcode(DEX, "DEX", 1, 2, NONE),
        Opcode(0xcb, "NOP", 1, 3, NONE),
        Opcode(CPY_ABS, "CPY", 3, 4, ABSOLUTE),
        Opcode(CMP_ABS, "CMP", 3, 4, ABSOLUTE),
        Opcode(DEC_ABS, "DEC", 3, 6, ABSOLUTE),
        Opcode(0xcf, "BBS4", 3, 5, RELATIVE),
        Opcode(BNE, "BNE", 2, 2, RELATIVE),
        Opcode(CMP_IND_Y, "CMP", 2, 5, INDIRECT_Y),
        Opcode(0xd2, "CMP", 2, 5, ZPI),
        Opcode(0xd3, "NOP", 1, 1, NONE),
        Opcode(0xd4, "NOP", 2, 4, NONE),
        Opcode(CMP_ZP_X, "CMP", 2, 4, ZP_X),
        Opcode(DEC_ZP_X, "DEC", 2, 6, ZP_X),
        Opcode(0xd7, "SMB5", 2, 5, ZP),
        Opcode(CLD, "CLD", 1, 2, NONE),
        Opcode(CMP_ABS_Y, "CMP", 3, 4, ABSOLUTE_Y),
        Opcode(0xda, "PHX", 1, 3, NONE),
        Opcode(0xdb, "NOP", 1, 3, NONE),
        Opcode(0xdc, "NOP", 3, 4, NONE),
        Opcode(CMP_ABS_X, "CMP", 3, 4, ABSOLUTE_X),
        Opcode(DEC_ABS_X, "DEC", 3, 7, ABSOLUTE_X),
        Opcode(0xdf, "BBS5", 3, 5, RELATIVE),
        Opcode(CPX_IMM, "CPX", 2, 2, IMMEDIATE),
        Opcode(SBC_IND_X, "SBC", 2, 6, INDIRECT_X),
        Opcode(0xe2, "NOP", 2, 2, NONE),
        Opcode(0xe3, "NOP", 1, 1, NONE),
        Opcode(CPX_ZP, "CPX", 2, 3, ZP),
        Opcode(SBC_ZP, "SBC", 2, 3, ZP),
        Opcode(INC_ZP, "INC", 2, 5, ZP),
        Opcode(0xe7, "SMB6", 2, 5, ZP),
        Opcode(INX, "INX", 1, 2, NONE),
        Opcode(SBC_IMM, "SBC", 2, 2, IMMEDIATE),
        Opcode(NOP, "NOP", 1, 2, NONE),
        Opcode(0xeb, "NOP", 1, 1, NONE),
        Opcode(CPX_ABS, "CPX", 3, 4, ABSOLUTE),
        Opcode(SBC_ABS, "SBC", 3, 4, ABSOLUTE),
        Opcode(INC_ABS, "INC", 3, 6, ABSOLUTE),
        Opcode(0xef, "BBS6", 3, 5, RELATIVE),
        Opcode(BEQ, "BEQ", 2, 2, RELATIVE),
        Opcode(SBC_IND_Y, "SBC", 2, 5, INDIRECT_Y),
        Opcode(0xf2, "SBC", 2, 5, ZPI),
        Opcode(0xf3, "NOP", 1, 1, NONE),
        Opcode(0xf4, "NOP", 2, 4, NONE),
        Opcode(SBC_ZP_X, "SBC", 2, 4, ZP_X),
        Opcode(INC_ZP_X, "INC", 2, 6, ZP_X),
        Opcode(0xf7, "SMB7", 2, 5, ZP),
        Opcode(SED, "SED", 1, 2, NONE),
        Opcode(SBC_ABS_Y, "SBC", 3, 4, ABSOLUTE_Y),
        Opcode(0xfa, "PLX", 1, 4, NONE),
        Opcode(0xfb, "NOP", 1, 1, NONE),
        Opcode(0xfc, "NOP", 3, 4, NONE),
        Opcode(SBC_ABS_X, "SBC", 3, 4, ABSOLUTE_X),
        Opcode(INC_ABS_X, "INC", 3, 7, ABSOLUTE_X),
        Opcode(0xff, "BBS7", 3, 5, RELATIVE)
)