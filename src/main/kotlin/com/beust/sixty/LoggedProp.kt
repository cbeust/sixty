package com.beust.sixty

import kotlin.reflect.KProperty

class LoggedProp(private var value: Boolean) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: Boolean) {
//        println(property.name + "=" + newValue)
//        if (newValue) {
//            println("BREAK")
//        }
        value = newValue
    }
    override fun toString() = value.toString()
}

