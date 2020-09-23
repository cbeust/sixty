package com.beust.app

import kotlin.reflect.KProperty

typealias ObsListener<T> = (T, T) -> Unit

class Obs<T>(val def: T) {
    private var _value = def
    private val listeners = mutableListOf<ObsListener<T>>()

    fun addListener(l: (T, T) -> Unit) { listeners.add(l) }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return _value
    }

    fun setValue(value: T) {
        listeners.forEach { it.invoke(_value, value) }
        _value = value
    }

    override fun toString() = _value.toString() 
}

object UiState {
    var currentDiskName = Obs("<none>")
}