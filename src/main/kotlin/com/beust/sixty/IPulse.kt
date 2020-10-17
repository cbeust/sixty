package com.beust.sixty

import com.beust.app.Apple2Computer
import com.beust.app.GraphicContext
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class PulseResult(val runStatus: Computer.RunStatus = Computer.RunStatus.RUN)

class Runner(val gc: GraphicContext? = null) {
    private var runStatus = Computer.RunStatus.RUN
    private var sliceStart = System.currentTimeMillis()

    fun runSlice(computer: IComputer): Computer.RunStatus {
        runStatus = Computer.RunStatus.RUN
        var targetCycles = (System.currentTimeMillis() - sliceStart) * 1000
        while (runStatus == Computer.RunStatus.RUN && targetCycles-- > 0) {
            runStatus = computer.step().runStatus
        }
        sliceStart = System.currentTimeMillis()
        return runStatus
    }

    private val blocked = Object()

    fun runPeriodically(computer: IComputer, maxTimeSeconds: Int = 0, blocking: Boolean = false,
            onStop: () -> Unit = {}): Computer.RunStatus {
        var c = computer
        val command = object: Runnable {
            var runStart = System.currentTimeMillis()
            var stop = false
            override fun run() {
                if (! stop) {
                    val status = runSlice(c)
                    if (status == Computer.RunStatus.STOP) {
                        stop = true
                    } else if (status == Computer.RunStatus.REBOOT) {
                        val c2 = Apple2Computer(gc)
                        c = c2
                        gc?.reset(c2)
                    } // else STOP
                    if (maxTimeSeconds > 0 && (System.currentTimeMillis() - runStart) / 1000 >= maxTimeSeconds) {
                        stop = true
                        onStop()
                        synchronized(blocked) {
                            blocked.notify()
                        }
                    }
                }
            }
        }

        val tp = Executors.newScheduledThreadPool(1)
        val task = tp.scheduleWithFixedDelay(command, 0, 100, TimeUnit.MILLISECONDS)
        try {
            task.get(5, TimeUnit.SECONDS)
        } catch(ex: TimeoutException) {
            ""
            // do nothing
        } catch(ex: Exception) {
            throw ex
        }
        if (blocking) synchronized(blocked) {
            blocked.wait()
            task.cancel(true)
        }

        return runStatus
    }
}

class PulseManager {
    private var runStatus = Computer.RunStatus.RUN

    fun stop() {
        runStatus = Computer.RunStatus.STOP
    }

    private var start = System.currentTimeMillis()

    fun run(listener: IPulse): Computer.RunStatus {
        while (runStatus == Computer.RunStatus.RUN) {
            runStatus = listener.onPulse(this@PulseManager).runStatus
        }
        return runStatus
    }

    fun runSlice(listener: IPulse): Computer.RunStatus {
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
