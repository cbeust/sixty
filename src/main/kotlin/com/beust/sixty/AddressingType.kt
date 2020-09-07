package com.beust.sixty

enum class AddressingType {
    IMMEDIATE, ZP, ZP_X, ZP_Y, ABSOLUTE, ABSOLUTE_X, ABSOLUTE_Y, INDIRECT_X, INDIRECT_Y, REGISTER_A, INDIRECT,
        RELATIVE, ZPI, AIX, NONE;

    fun toString(pc: Int, byte: Int, word: Int): String {
        return when(this) {
            IMMEDIATE -> " #$${byte.h()}"
            ZP -> " $${byte.h()}"
            ZP_X -> " $${byte.h()},X"
            ZP_Y -> " $${byte.h()},Y"
            ABSOLUTE -> " $${word.hh()}"
            ABSOLUTE_X -> " $${word.hh()},X"
            ABSOLUTE_Y -> " $${word.hh()},Y"
            INDIRECT_X -> " ($${byte.h()},X)"
            INDIRECT_Y -> " ($${byte.h()},Y)"
            REGISTER_A -> ""
            INDIRECT -> " ($${word.hh()})"
            RELATIVE -> " $${(pc + byte.toByte() + 2).hh()}"
            ZPI, AIX, NONE -> ""
        }
    }

}