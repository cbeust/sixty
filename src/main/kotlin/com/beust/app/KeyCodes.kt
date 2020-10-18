package com.beust.app

val KEY_MAP = mapOf(
        0x27 to listOf(0x27, 0x22), // ' "
        0x2c to listOf(0x2c, 0x3c), // , <
        0x2e to listOf(0x2e, 0x3e), // . >
        0x2f to listOf(0x2f, 0x3f), // / ?
        0x31 to listOf('1'.toInt(), '!'.toInt()),
        0x32 to listOf('2'.toInt(), '@'.toInt()),
        0x33 to listOf('3'.toInt(), '#'.toInt()),
        0x34 to listOf('4'.toInt(), '$'.toInt()),
        0x35 to listOf('5'.toInt(), '%'.toInt()),
        0x36 to listOf('6'.toInt(), '^'.toInt()),
        0x37 to listOf('7'.toInt(), '&'.toInt()),
        0x38 to listOf('8'.toInt(), '*'.toInt()),
        0x39 to listOf('9'.toInt(), '('.toInt()),
        0x30 to listOf('0'.toInt(), ')'.toInt()),
        0x2d to listOf(0x2d, 0x5f), // - _
        0x3b to listOf(0x3b, 0x3a), // ; :
        0x3d to listOf('='.toInt(), '+'.toInt()),
        0x5b to listOf('['.toInt(), '{'.toInt()),
        0x5c to listOf('\\'.toInt(), '|'.toInt()),
        0x5d to listOf(']'.toInt(), '}'.toInt())
)
