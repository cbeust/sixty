package com.beust.sixty

class PulseResult(val runStatus: Computer.RunStatus = Computer.RunStatus.RUN)

class PulseManager {
    private var runStatus = Computer.RunStatus.RUN

    fun stop() {
        runStatus = Computer.RunStatus.STOP
    }

    private var start = System.currentTimeMillis()

    fun run(listener: IPulse): Computer.RunStatus {
        runStatus = Computer.RunStatus.RUN
        var targetCycles = (System.currentTimeMillis() - start) * 1000
        while (runStatus == Computer.RunStatus.RUN && targetCycles-- > 0) {
            runStatus = listener.onPulse(this@PulseManager).runStatus
            start = System.currentTimeMillis()
        }
        return runStatus
    }
}

interface IPulse {
    fun onPulse(manager: PulseManager): PulseResult

    fun stop()
}
