package com.beust.app

import java.io.File

typealias ObsListener<T> = (T, T) -> Unit

class Obs<T>(val name: String, val def: T) {
    private val listeners = mutableListOf<ObsListener<T>>()

    fun addListener(l: (T, T) -> Unit) { listeners.add(l) }

    private var _value = def
    var value: T
        get() = _value
        set(f) {
            println("$name=$f")
            _value = f
            listeners.forEach { it.invoke(_value, f) }
        }

    override fun toString() = _value.toString()
}

enum class ByteAlgorithm { RAW, SHIFTED, DISK_CONTENT }

object UiState {
    var currentDiskFile: Obs<File?> = Obs("Current disk", null)
    var currentTrack = Obs("Current track", 0)

    var byteAlgorithn = Obs("Byte algorithm", ByteAlgorithm.SHIFTED)
    val currentBytes: Obs<List<Int>> = Obs("Current bytes", emptyList())

    var mainScreenText = Obs("text", true)
    var mainScreenMixed = Obs("mixed", false)
    var mainScreenHires = Obs("hires", false)
    var mainScreenPage2 = Obs("page2", false)
}