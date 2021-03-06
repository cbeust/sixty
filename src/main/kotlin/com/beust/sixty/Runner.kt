package com.beust.sixty

import com.beust.app.*
import com.beust.swt.PERIOD_MILLISECONDS
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class Runner(val gc: GraphicContext? = null) {
    private var stop = false
    private val threadPool = Executors.newScheduledThreadPool(1)
    private var runStatus = Computer.RunStatus.RUN
    private var sliceStart = System.currentTimeMillis()
    private var future: ScheduledFuture<*>? = null
    private var throwable: Throwable? = null

    fun stop() {
        stop = true
        future?.cancel(true)
        threadPool.shutdown()
        throwable?.let {
            throw(it)
        }
    }

    fun runCycles(computer: IComputer, cycles: Int) {
        repeat(cycles) {
            computer.step()
        }
    }

    fun runTimedSlice(computer: IComputer): Pair<Computer.RunStatus, Long> {
        runStatus = Computer.RunStatus.RUN
        var targetCycles = SPEED_FACTOR * (System.currentTimeMillis() - sliceStart) * 1300
        val speed = UiState.speedMegahertz.value
        val adjustedCycles = if (speed > 0.25f) (targetCycles / speed).toLong() else targetCycles
        val cycles = adjustedCycles
        while (runStatus == Computer.RunStatus.RUN && targetCycles-- > 0) {
            runStatus = computer.step()
        }
        sliceStart = System.currentTimeMillis()
        return Pair(runStatus, cycles)
    }

    private val blocked = Object()

    data class RunInfo(val start: Long, val cycles: Long)
    private val maxSize = 10
    private val runInfos = arrayListOf<RunInfo>()

    private fun addRunInfo(start: Long, cycles: Long) {
        if (runInfos.size >=maxSize) {
            runInfos.removeAt(0)
        }
        runInfos.add(RunInfo(start, cycles))
    }

    @Suppress("ConvertLambdaToReference")
    private fun updateCpuSpeed() {
        val timeStart = runInfos[0].start
        val timeEnd = runInfos[runInfos.size - 1].start
        val durationMicroSeconds = (timeEnd - timeStart) * 1000
        val cycles = runInfos.map { it.cycles }.sum()
        UiState.speedMegahertz.value = cycles.toFloat() / durationMicroSeconds
    }

    fun runPeriodically(computer: IComputer, maxTimeSeconds: Int = 0, blocking: Boolean = false,
            onStop: () -> Throwable? = { null }) {
        var c = computer
        val command = object: Runnable {
            var runStart = System.currentTimeMillis()
            override fun run() {
                try {
                    if (! stop) {
                        val cycleStart = System.currentTimeMillis()
                        val (status, cycles) = runTimedSlice(c)
                        when (status) {
                            Computer.RunStatus.STOP -> {
                                stop = true
                            }
                            Computer.RunStatus.REBOOT -> {
                                val c2 = Apple2Computer(gc)
                                c = c2
                                gc?.reset(c2)
                            }
                            else -> {
                                addRunInfo(cycleStart, cycles)
                                updateCpuSpeed()
                            }
                        }

                        if (maxTimeSeconds > 0 && (System.currentTimeMillis() - runStart) / 1000 >= maxTimeSeconds) {
                            stop = true
                            throwable = onStop()
                            synchronized(blocked) {
                                @Suppress("ConvertLambdaToReference")
                                blocked.notify()
                            }
                        }
                    } else {
                        stop()
                    }
                } catch(ex: Throwable) {
                    ex.printStackTrace()
                    throw ex
                }
            }
        }


        Threads.scheduledThreadPool.scheduleWithFixedDelay(command, 0, PERIOD_MILLISECONDS, TimeUnit.MILLISECONDS)
        if (blocking) synchronized(blocked) {
            blocked.wait()
            stop()
        }
    }
}
