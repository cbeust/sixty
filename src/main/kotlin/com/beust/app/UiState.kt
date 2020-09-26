package com.beust.app

import kotlin.reflect.KProperty

typealias ObsListener<T> = (T, T) -> Unit

class Obs<T>(val def: T) {
    private val listeners = mutableListOf<ObsListener<T>>()

    fun addListener(l: (T, T) -> Unit) { listeners.add(l) }

    private var _value = def
    var value: T
        get() = _value
        set(f) {
            listeners.forEach { it.invoke(_value, f) }
            _value = f
        }

    override fun toString() = _value.toString()
}

object UiState {
    var currentDisk: Obs<IDisk?> = Obs(null)
}