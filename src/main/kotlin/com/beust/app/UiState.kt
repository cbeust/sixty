package com.beust.app

import java.io.File
import kotlin.reflect.KProperty

typealias ObsListener<T> = (T, T) -> Unit

class Obs<T>(val def: T) {
    private val listeners = mutableListOf<ObsListener<T>>()

    fun addListener(l: (T, T) -> Unit) { listeners.add(l) }

    private var _value = def
    var value: T
        get() = _value
        set(f) {
            println("New value: $f")
            _value = f
            listeners.forEach { it.invoke(_value, f) }
        }

    override fun toString() = _value.toString()
}

object UiState {
    var currentDiskFile: Obs<File?> = Obs(null)
    var currentTrack: Obs<Int> = Obs(0)
//    var currentDisk: Obs<IDisk?> = Obs(null)
}