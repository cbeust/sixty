package com.beust.sixty

class PulseResult(val stop: Boolean = false)

class PulseManager {
    private val pulseListeners = arrayListOf<IPulse>()
    private var stop = false

    fun stop() {
        stop = true
    }

    fun addListener(listener: IPulse) {
        pulseListeners.add(listener)
    }

    fun run() {
        while (! stop) {
            pulseListeners.forEach {
                var r = it.onPulse(this)
                if (r.stop) stop = true
            }
        }
    }
}

interface IPulse {
    fun onPulse(manager: PulseManager): PulseResult

    fun stop()
}
