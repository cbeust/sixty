package com.beust.sixty

class PulseResult(val stop: Boolean = false)

interface IPulse {
    fun onPulse(): PulseResult

    fun stop()

    fun run() {
        var r = onPulse()
        while (! r.stop) r = onPulse()
    }
}
