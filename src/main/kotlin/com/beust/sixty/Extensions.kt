package com.beust.sixty

import java.awt.Color

fun Byte.h(): String = String.format("%02x", this).toUpperCase()
fun Int.h(): String = String.format("%02x", this).toUpperCase()
fun Int.hh(): String = String.format("%04x", this).toUpperCase()

fun Byte.bit(n: Int) = this.toInt().bit(n)
fun Int.bit(n: Int) = this.and(1.shl(n)).shr(n)

//fun Int.b() = Integer.toBinaryString(this)

/** Int to binary */
fun Int.b(): String {
    val result = arrayListOf<String>()
    var n = this.and(0xff)
    var i = 0
    while (n > 0) {
        result.add(0, if (n % 2 == 0) "0" else "1")
        n /= 2
        if ((++i) % 4 == 0 && n > 0) {
            result.add(0, "_")
        }
    }
    return result.joinToString("")
}

fun Byte.b() = toInt().b()

fun Color.s() = when(this) {
    Color.BLACK -> "black"
    Color.WHITE -> "white"
    Color.GREEN -> "green"
    Color.MAGENTA -> "magenta"
    Color.ORANGE -> "orange"
    Color.BLUE -> "blue"
    else -> this.toString()
}

fun Boolean.int(): Int = if (this) 1 else 0
fun Int.toBoolean(): Boolean = if (this == 0) false else if (this == 1) true
    else TODO("Illegal boolean: $this")

fun List<Int>.b(): String {
    val result = StringBuffer()
    var i = 0
    this.forEach {
        if (i != 0 && i % 8 == 0) result.append(" ")
        else if (i != 0 && i % 4 == 0) result.append("_")
        result.append(it)
        i++
    }
    return result.toString()
}

fun ERROR(reason: String): Nothing = throw Error(reason)
