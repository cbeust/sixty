package com.beust.sixty

class PulseResult(val stop: Boolean = false)

interface IPulse {
    fun onPulse(): PulseResult
}
