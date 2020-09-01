package com.beust.sixty

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
const val AND_ABS = 0x24
const val AND_ABS_X = 0x3d
const val AND_ABS_Y = 0x39
const val AND_IND_X = 0x21
const val AND_IND_Y = 0x31

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

const val EOR_IMM = 0x49
const val EOR_ZP = 0x45
const val EOR_ZP_X = 0x55
const val EOR_ABS = 0x4d
const val EOR_ABS_X = 0x5d
const val EOR_ABS_Y = 0x59
const val EOR_IND_X = 0x41
const val EOR_IND_Y = 0x51

const val JSR = 0x20

const val LDA_IMM = 0xa9
const val LDA_ZP = 0xa5
const val LDA_ZP_X = 0xb5
const val LDA_ZP_Y = 0xb6
const val LDA_ABS = 0xad
const val LDA_ABS_X = 0xbd
const val LDA_ABS_Y = 0xb9
const val LDA_IND_X = 0xa1
const val LDA_IND_Y = 0xb1


const val LDY_IMM = 0xa0
const val LDY_ZP = 0xa4
const val LDY_ZP_X = 0xb4
const val LDY_ABS = 0xac
const val LDY_ABS_X = 0xbc

const val STY_ZP = 0x84
const val STY_ZP_X = 0x94
const val STY_ABS = 0x8c


const val LSR_A = 0x4a
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

const val ROL_A = 0x2a
const val ROL_ZP = 0x26
const val ROL_XP_Z = 0x36
const val ROL_ABS = 0x2e
const val ROL_ABS_X = 0x3e

const val ROR_A = 0x6a
const val ROR_ZP = 0x66
const val ROR_XP_Z = 0x76
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

const val STX_ZP = 0x86

const val TXS = 0x9a
const val TSX = 0xba
const val PHA = 0x48
const val PLA = 0x68
const val PHP = 0x08
const val PLP = 0x28

const val LDX_IMM = 0xa2
const val LDX_ZP = 0xa6
const val LDX_ZP_Y = 0xb6
const val LDX_ABS = 0xae
const val LDX_ABS_Y = 0xbe

const val SBC_IMM = 0xe9
const val SBC_ZP = 0xe5
const val SBC_ZP_X = 0xf5
const val SBC_ABS = 0xed
const val SBC_ABX_X = 0xfd
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


