package com.beust.sixty

/**
 * Size, in bytes, required for each instruction. This table
 * includes sizes for all instructions for NMOS 6502, CMOS 65C02,
 * and CMOS 65C816
 */
val SIZES = intArrayOf(
        1, 2, 2, 1, 2, 2, 2, 2, 1, 2, 1, 1, 3, 3, 3, 3,  // 0x00-0x0f
        2, 2, 2, 1, 2, 2, 2, 2, 1, 3, 1, 1, 3, 3, 3, 3,  // 0x10-0x1f
        3, 2, 2, 1, 2, 2, 2, 2, 1, 2, 1, 1, 3, 3, 3, 3,  // 0x20-0x2f
        2, 2, 2, 1, 2, 2, 2, 2, 1, 3, 1, 1, 3, 3, 3, 3,  // 0x30-0x3f
        1, 2, 2, 1, 2, 2, 2, 2, 1, 2, 1, 1, 3, 3, 3, 3,  // 0x40-0x4f
        2, 2, 2, 1, 2, 2, 2, 2, 1, 3, 1, 1, 3, 3, 3, 3,  // 0x50-0x5f
        1, 2, 2, 1, 2, 2, 2, 2, 1, 2, 1, 1, 3, 3, 3, 3,  // 0x60-0x6f
        2, 2, 2, 1, 2, 2, 2, 2, 1, 3, 1, 1, 3, 3, 3, 3,  // 0x70-0x7f
        2, 2, 2, 1, 2, 2, 2, 2, 1, 2, 1, 1, 3, 3, 3, 3,  // 0x80-0x8f
        2, 2, 2, 1, 2, 2, 2, 2, 1, 3, 1, 1, 3, 3, 3, 3,  // 0x90-0x9f
        2, 2, 2, 1, 2, 2, 2, 2, 1, 2, 1, 1, 3, 3, 3, 3,  // 0xa0-0xaf
        2, 2, 2, 1, 2, 2, 2, 2, 1, 3, 1, 1, 3, 3, 3, 3,  // 0xb0-0xbf
        2, 2, 2, 1, 2, 2, 2, 2, 1, 2, 1, 1, 3, 3, 3, 3,  // 0xc0-0xcf
        2, 2, 2, 1, 2, 2, 2, 2, 1, 3, 1, 1, 3, 3, 3, 3,  // 0xd0-0xdf
        2, 2, 2, 1, 2, 2, 2, 2, 1, 2, 1, 1, 3, 3, 3, 3,  // 0xe0-0xef
        2, 2, 2, 1, 2, 2, 2, 2, 1, 3, 1, 1, 3, 3, 3, 3 // 0xf0-0xff
)

/**
 * Number of clock cycles required for each instruction when
 * in NMOS mode.
 */
val instructionClocksNmos = intArrayOf(
        7, 6, 1, 8, 3, 3, 5, 5, 3, 2, 2, 2, 4, 4, 6, 6,  // 0x00-0x0f
        2, 5, 1, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,  // 0x10-0x1f
        6, 6, 1, 8, 3, 3, 5, 5, 4, 2, 2, 2, 4, 4, 6, 6,  // 0x20-0x2f
        2, 5, 1, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,  // 0x30-0x3f
        6, 6, 1, 8, 3, 3, 5, 5, 3, 2, 2, 2, 3, 4, 6, 6,  // 0x40-0x4f
        2, 5, 1, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,  // 0x50-0x5f
        6, 6, 1, 8, 3, 3, 5, 5, 4, 2, 2, 2, 5, 4, 6, 6,  // 0x60-0x6f
        2, 5, 1, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,  // 0x70-0x7f
        2, 6, 2, 6, 3, 3, 3, 3, 2, 2, 2, 2, 4, 4, 4, 4,  // 0x80-0x8f
        2, 6, 1, 6, 4, 4, 4, 4, 2, 5, 2, 5, 5, 5, 5, 5,  // 0x90-0x9f
        2, 6, 2, 6, 3, 3, 3, 3, 2, 2, 2, 2, 4, 4, 4, 4,  // 0xa0-0xaf
        2, 5, 1, 5, 4, 4, 4, 4, 2, 4, 2, 4, 4, 4, 4, 4,  // 0xb0-0xbf
        2, 6, 2, 8, 3, 3, 5, 5, 2, 2, 2, 2, 4, 4, 6, 6,  // 0xc0-0xcf
        2, 5, 1, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,  // 0xd0-0xdf
        2, 6, 2, 8, 3, 3, 5, 5, 2, 2, 2, 2, 4, 4, 6, 6,  // 0xe0-0xef
        2, 5, 1, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7 // 0xf0-0xff
)

/**
 * Number of clock cycles required for each instruction when
 * in CMOS mode
 */
val TIMINGS = intArrayOf(
        7, 6, 2, 1, 5, 3, 5, 5, 3, 2, 2, 1, 6, 4, 6, 5,  // 0x00-0x0f
        2, 5, 5, 1, 5, 4, 6, 5, 2, 4, 2, 1, 6, 4, 6, 5,  // 0x10-0x1f
        6, 6, 2, 1, 3, 3, 5, 5, 4, 2, 2, 1, 4, 4, 6, 5,  // 0x20-0x2f
        2, 5, 5, 1, 4, 4, 6, 5, 2, 4, 2, 1, 4, 4, 6, 5,  // 0x30-0x3f
        6, 6, 2, 1, 2, 3, 5, 3, 3, 2, 2, 1, 3, 4, 6, 5,  // 0x40-0x4f
        2, 5, 5, 1, 4, 4, 6, 5, 2, 4, 3, 1, 8, 4, 6, 5,  // 0x50-0x5f
        6, 6, 2, 1, 3, 3, 5, 5, 4, 2, 2, 1, 6, 4, 6, 5,  // 0x60-0x6f
        2, 5, 5, 1, 4, 4, 6, 5, 2, 4, 4, 3, 6, 4, 6, 5,  // 0x70-0x7f
        3, 6, 2, 1, 3, 3, 3, 5, 2, 2, 2, 1, 4, 4, 4, 5,  // 0x80-0x8f
        2, 6, 5, 1, 4, 4, 4, 5, 2, 5, 2, 1, 4, 5, 5, 5,  // 0x90-0x9f
        2, 6, 2, 1, 3, 3, 3, 5, 2, 2, 2, 1, 4, 4, 4, 5,  // 0xa0-0xaf
        2, 5, 5, 1, 4, 4, 4, 5, 2, 4, 2, 1, 4, 4, 4, 5,  // 0xb0-0xbf
        2, 6, 2, 1, 3, 3, 5, 5, 2, 2, 2, 3, 4, 4, 6, 5,  // 0xc0-0xcf
        2, 5, 5, 1, 4, 4, 6, 5, 2, 4, 3, 3, 4, 4, 7, 5,  // 0xd0-0xdf
        2, 6, 2, 1, 3, 3, 5, 5, 2, 2, 2, 1, 4, 4, 6, 5,  // 0xe0-0xef
        2, 5, 5, 1, 4, 4, 6, 5, 2, 4, 4, 1, 4, 4, 7, 5 // 0xf0-0xff
)

// 6502 opcodes.  No 65C02 opcodes implemented.
/**
 * Instruction opcode names. This lists all opcodes for
 * NMOS 6502, CMOS 65C02, and CMOS 65C816
 */
val NAMES = arrayOf(
        "BRK", "ORA", "NOP", "NOP", "TSB", "ORA", "ASL", "RMB0",  // 0x00-0x07
        "PHP", "ORA", "ASL", "NOP", "TSB", "ORA", "ASL", "BBR0",  // 0x08-0x0f
        "BPL", "ORA", "ORA", "NOP", "TRB", "ORA", "ASL", "RMB1",  // 0x10-0x17
        "CLC", "ORA", "INC", "NOP", "TRB", "ORA", "ASL", "BBR1",  // 0x18-0x1f
        "JSR", "AND", "NOP", "NOP", "BIT", "AND", "ROL", "RMB2",  // 0x20-0x27
        "PLP", "AND", "ROL", "NOP", "BIT", "AND", "ROL", "BBR2",  // 0x28-0x2f
        "BMI", "AND", "AND", "NOP", "BIT", "AND", "ROL", "RMB3",  // 0x30-0x37
        "SEC", "AND", "DEC", "NOP", "BIT", "AND", "ROL", "BBR3",  // 0x38-0x3f
        "RTI", "EOR", "NOP", "NOP", "NOP", "EOR", "LSR", "RMB4",  // 0x40-0x47
        "PHA", "EOR", "LSR", "NOP", "JMP", "EOR", "LSR", "BBR4",  // 0x48-0x4f
        "BVC", "EOR", "EOR", "NOP", "NOP", "EOR", "LSR", "RMB5",  // 0x50-0x57
        "CLI", "EOR", "PHY", "NOP", "NOP", "EOR", "LSR", "BBR5",  // 0x58-0x5f
        "RTS", "ADC", "NOP", "NOP", "STZ", "ADC", "ROR", "RMB6",  // 0x60-0x67
        "PLA", "ADC", "ROR", "NOP", "JMP", "ADC", "ROR", "BBR6",  // 0x68-0x6f
        "BVS", "ADC", "ADC", "NOP", "STZ", "ADC", "ROR", "RMB7",  // 0x70-0x77
        "SEI", "ADC", "PLY", "NOP", "JMP", "ADC", "ROR", "BBR7",  // 0x78-0x7f
        "BRA", "STA", "NOP", "NOP", "STY", "STA", "STX", "SMB0",  // 0x80-0x87
        "DEY", "BIT", "TXA", "NOP", "STY", "STA", "STX", "BBS0",  // 0x88-0x8f
        "BCC", "STA", "STA", "NOP", "STY", "STA", "STX", "SMB1",  // 0x90-0x97
        "TYA", "STA", "TXS", "NOP", "STZ", "STA", "STZ", "BBS1",  // 0x98-0x9f
        "LDY", "LDA", "LDX", "NOP", "LDY", "LDA", "LDX", "SMB2",  // 0xa0-0xa7
        "TAY", "LDA", "TAX", "NOP", "LDY", "LDA", "LDX", "BBS2",  // 0xa8-0xaf
        "BCS", "LDA", "LDA", "NOP", "LDY", "LDA", "LDX", "SMB3",  // 0xb0-0xb7
        "CLV", "LDA", "TSX", "NOP", "LDY", "LDA", "LDX", "BBS3",  // 0xb8-0xbf
        "CPY", "CMP", "NOP", "NOP", "CPY", "CMP", "DEC", "SMB4",  // 0xc0-0xc7
        "INY", "CMP", "DEX", "NOP", "CPY", "CMP", "DEC", "BBS4",  // 0xc8-0xcf
        "BNE", "CMP", "CMP", "NOP", "NOP", "CMP", "DEC", "SMB5",  // 0xd0-0xd7
        "CLD", "CMP", "PHX", "NOP", "NOP", "CMP", "DEC", "BBS5",  // 0xd8-0xdf
        "CPX", "SBC", "NOP", "NOP", "CPX", "SBC", "INC", "SMB6",  // 0xe0-0xe7
        "INX", "SBC", "NOP", "NOP", "CPX", "SBC", "INC", "BBS6",  // 0xe8-0xef
        "BEQ", "SBC", "SBC", "NOP", "NOP", "SBC", "INC", "SMB7",  // 0xf0-0xf7
        "SED", "SBC", "PLX", "NOP", "NOP", "SBC", "INC", "BBS7" // 0xf8-0xff
)

/**
 * Instruction addressing modes. This table includes sizes
 * for all instructions for NMOS 6502, CMOS 65C02,
 * and CMOS 65C816
 */
var instructionModes: Array<Addressing> = arrayOf(
        Addressing.NONE, Addressing.INDIRECT_X, Addressing.NONE, Addressing.NONE,  // 0x00-0x03
        Addressing.ZP, Addressing.ZP, Addressing.ZP, Addressing.ZP,  // 0x04-0x07
        Addressing.NONE, Addressing.IMMEDIATE, Addressing.REGISTER_A, Addressing.NONE,  // 0x08-0x0b
        Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.RELATIVE,  // 0x0c-0x0f
        Addressing.RELATIVE, Addressing.INDIRECT_Y, Addressing.ZPI, Addressing.NONE,  // 0x10-0x13
        Addressing.ZP, Addressing.ZP_X, Addressing.ZP_X, Addressing.ZP,  // 0x14-0x17
        Addressing.NONE, Addressing.ABSOLUTE_Y, Addressing.NONE, Addressing.NONE,  // 0x18-0x1b
        Addressing.ABSOLUTE, Addressing.ABSOLUTE_X, Addressing.ABSOLUTE_X, Addressing.RELATIVE,  // 0x1c-0x1f
        Addressing.ABSOLUTE, Addressing.INDIRECT_X, Addressing.NONE, Addressing.NONE,  // 0x20-0x23
        Addressing.ZP, Addressing.ZP, Addressing.ZP, Addressing.ZP,  // 0x24-0x27
        Addressing.NONE, Addressing.IMMEDIATE, Addressing.REGISTER_A, Addressing.NONE,  // 0x28-0x2b
        Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.RELATIVE,  // 0x2c-0x2f
        Addressing.RELATIVE, Addressing.INDIRECT_Y, Addressing.ZPI, Addressing.NONE,  // 0x30-0x33
        Addressing.ZP_X, Addressing.ZP_X, Addressing.ZP_X, Addressing.ZP,  // 0x34-0x37
        Addressing.NONE, Addressing.ABSOLUTE_Y, Addressing.NONE, Addressing.NONE,  // 0x38-0x3b
        Addressing.NONE, Addressing.ABSOLUTE_X, Addressing.ABSOLUTE_X, Addressing.RELATIVE,  // 0x3c-0x3f
        Addressing.NONE, Addressing.INDIRECT_X, Addressing.NONE, Addressing.NONE,  // 0x40-0x43
        Addressing.NONE, Addressing.ZP, Addressing.ZP, Addressing.ZP,  // 0x44-0x47
        Addressing.NONE, Addressing.IMMEDIATE, Addressing.REGISTER_A, Addressing.NONE,  // 0x48-0x4b
        Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.RELATIVE,  // 0x4c-0x4f
        Addressing.RELATIVE, Addressing.INDIRECT_Y, Addressing.ZPI, Addressing.NONE,  // 0x50-0x53
        Addressing.NONE, Addressing.ZP_X, Addressing.ZP_X, Addressing.ZP,  // 0x54-0x57
        Addressing.NONE, Addressing.ABSOLUTE_Y, Addressing.NONE, Addressing.NONE,  // 0x58-0x5b
        Addressing.NONE, Addressing.ABSOLUTE_X, Addressing.ABSOLUTE_X, Addressing.RELATIVE,  // 0x5c-0x5f
        Addressing.NONE, Addressing.INDIRECT_X, Addressing.NONE, Addressing.NONE,  // 0x60-0x63
        Addressing.ZP, Addressing.ZP, Addressing.ZP, Addressing.ZP,  // 0x64-0x67
        Addressing.NONE, Addressing.IMMEDIATE, Addressing.REGISTER_A, Addressing.NONE,  // 0x68-0x6b
        Addressing.INDIRECT, Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.RELATIVE,  // 0x6c-0x6f
        Addressing.RELATIVE, Addressing.INDIRECT_Y, Addressing.ZPI, Addressing.NONE,  // 0x70-0x73
        Addressing.ZP_X, Addressing.ZP_X, Addressing.ZP_X, Addressing.ZP,  // 0x74-0x77
        Addressing.NONE, Addressing.ABSOLUTE_Y, Addressing.NONE, Addressing.NONE,  // 0x78-0x7b
        Addressing.AIX, Addressing.ABSOLUTE_X, Addressing.ABSOLUTE_X, Addressing.RELATIVE,  // 0x7c-0x7f
        Addressing.RELATIVE, Addressing.INDIRECT_X, Addressing.NONE, Addressing.NONE,  // 0x80-0x83
        Addressing.ZP, Addressing.ZP, Addressing.ZP, Addressing.ZP,  // 0x84-0x87
        Addressing.NONE, Addressing.NONE, Addressing.NONE, Addressing.NONE,  // 0x88-0x8b
        Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.RELATIVE,  // 0x8c-0x8f
        Addressing.RELATIVE, Addressing.INDIRECT_Y, Addressing.ZPI, Addressing.NONE,  // 0x90-0x93
        Addressing.ZP_X, Addressing.ZP_X, Addressing.ZP_Y, Addressing.ZP,  // 0x94-0x97
        Addressing.NONE, Addressing.ABSOLUTE_Y, Addressing.NONE, Addressing.NONE,  // 0x98-0x9b
        Addressing.ABSOLUTE, Addressing.ABSOLUTE_X, Addressing.ABSOLUTE_X, Addressing.RELATIVE,  // 0x9c-0x9f
        Addressing.IMMEDIATE, Addressing.INDIRECT_X, Addressing.IMMEDIATE, Addressing.NONE,  // 0xa0-0xa3
        Addressing.ZP, Addressing.ZP, Addressing.ZP, Addressing.ZP,  // 0xa4-0xa7
        Addressing.NONE, Addressing.IMMEDIATE, Addressing.NONE, Addressing.NONE,  // 0xa8-0xab
        Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.RELATIVE,  // 0xac-0xaf
        Addressing.RELATIVE, Addressing.INDIRECT_Y, Addressing.ZPI, Addressing.NONE,  // 0xb0-0xb3
        Addressing.ZP_X, Addressing.ZP_X, Addressing.ZP_Y, Addressing.ZP,  // 0xb4-0xb7
        Addressing.NONE, Addressing.ABSOLUTE_Y, Addressing.NONE, Addressing.NONE,  // 0xb8-0xbb
        Addressing.ABSOLUTE_X, Addressing.ABSOLUTE_X, Addressing.ABSOLUTE_Y, Addressing.RELATIVE,  // 0xbc-0xbf
        Addressing.IMMEDIATE, Addressing.INDIRECT_X, Addressing.NONE, Addressing.NONE,  // 0xc0-0xc3
        Addressing.ZP, Addressing.ZP, Addressing.ZP, Addressing.ZP,  // 0xc4-0xc7
        Addressing.NONE, Addressing.IMMEDIATE, Addressing.NONE, Addressing.NONE,  // 0xc8-0xcb
        Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.RELATIVE,  // 0xcc-0xcf
        Addressing.RELATIVE, Addressing.INDIRECT_Y, Addressing.ZPI, Addressing.NONE,  // 0xd0-0xd3
        Addressing.NONE, Addressing.ZP_X, Addressing.ZP_X, Addressing.ZP,  // 0xd4-0xd7
        Addressing.NONE, Addressing.ABSOLUTE_Y, Addressing.NONE, Addressing.NONE,  // 0xd8-0xdb
        Addressing.NONE, Addressing.ABSOLUTE_X, Addressing.ABSOLUTE_X, Addressing.RELATIVE,  // 0xdc-0xdf
        Addressing.IMMEDIATE, Addressing.INDIRECT_X, Addressing.NONE, Addressing.NONE,  // 0xe0-0xe3
        Addressing.ZP, Addressing.ZP, Addressing.ZP, Addressing.ZP,  // 0xe4-0xe7
        Addressing.NONE, Addressing.IMMEDIATE, Addressing.NONE, Addressing.NONE,  // 0xe8-0xeb
        Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.ABSOLUTE, Addressing.RELATIVE,  // 0xec-0xef
        Addressing.RELATIVE, Addressing.INDIRECT_Y, Addressing.ZPI, Addressing.NONE,  // 0xf0-0xf3
        Addressing.NONE, Addressing.ZP_X, Addressing.ZP_X, Addressing.ZP,  // 0xf4-0xf7
        Addressing.NONE, Addressing.ABSOLUTE_Y, Addressing.NONE, Addressing.NONE,  // 0xf8-0xfb
        Addressing.NONE, Addressing.ABSOLUTE_X, Addressing.ABSOLUTE_X, Addressing.RELATIVE // 0xfc-0xff
)
