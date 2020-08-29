package com.beust.sixty

import javafx.scene.paint.Color

fun Byte.h(): String = String.format("%02x", this.toInt()).toUpperCase()
fun Int.h(): String = String.format("%02x", this).toUpperCase()

//fun Int.b() = Integer.toBinaryString(this)

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

fun Color.s() = when(this) {
    Color.BLACK -> "black"
    Color.WHITE -> "white"
    Color.GREEN -> "green"
    Color.VIOLET -> "violet"
    Color.MAGENTA -> "magenta"
    Color.ORANGE -> "orange"
    Color.BLUE -> "blue"
    else -> this.toString()
}
