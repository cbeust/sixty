package com.beust.app

import com.beust.sixty.logUiStatus
import java.io.File

typealias ObsListener<T> = (T, T) -> Unit

class Obs<T>(val name: String, val def: T) {
    private val listeners = mutableListOf<ObsListener<T>>()
    private val afterListeners = mutableListOf<ObsListener<T>>()

    fun addListener(l: (T, T) -> Unit) { listeners.add(l) }
    fun addAfterListener(l: (T, T) -> Unit) { afterListeners.add(l) }

    private var _value = def
    var value: T
        get() = _value
        set(f) {
            logUiStatus("$name=$f")
            listeners.forEach { it.invoke(_value, f) }
            _value = f
            afterListeners.forEach { it.invoke(_value, f) }
        }

    override fun toString() = _value.toString()
}

enum class ByteAlgorithm { RAW, SHIFTED, DISK_CONTENT }

object UiState {
    //
    // DISK tab
    //
    val currentDisk1File: Obs<File?> = Obs("Disk 1", DISK)
    val motor1: Obs<Boolean> = Obs("Drive 1", false)
    val currentDisk2File: Obs<File?> = Obs("Disk 2", null)
    val motor2: Obs<Boolean> = Obs("Drive 2", false)
    val currentTrack = Obs("Current track", 0)
    val byteAlgorithn = Obs("Byte algorithm", ByteAlgorithm.SHIFTED)
    val currentBytes: Obs<List<Int>> = Obs("Current bytes", emptyList())

    //
    // DEBUGGER tab
    //
    val debug: Obs<Boolean> = Obs("Debug", false)

    //
    // Text/hires pages
    //
    val mainScreenText = Obs("text", true)
    val mainScreenMixed = Obs("mixed", false)
    val mainScreenHires = Obs("hires", false)
    val mainScreenPage2 = Obs("page2", false)
}