package com.beust.sixty

class PulseResult(val runStatus: Computer.RunStatus = Computer.RunStatus.RUN)

class PulseManager {
    private val pulseListeners = arrayListOf<IPulse>()
    private var runStatus = Computer.RunStatus.RUN

    fun stop() {
        runStatus = Computer.RunStatus.STOP
    }

    fun addListener(listener: IPulse) {
        pulseListeners.add(listener)
    }

    fun removeListeners() {
        pulseListeners.clear()
    }

//    fun removeListener(listener: IPulse) {
//        pulseListeners.remove(listener)
//    }

    fun run(): Computer.RunStatus {
        runStatus = Computer.RunStatus.RUN
        while (runStatus == Computer.RunStatus.RUN) {
            var start = System.nanoTime()
            pulseListeners.forEach {
                val r = it.onPulse(this)
                runStatus = r.runStatus
            }
//            while (System.nanoTime() - start < 800);
        }
        return runStatus
    }
}

interface IPulse {
    fun onPulse(manager: PulseManager): PulseResult

    fun stop()
}
