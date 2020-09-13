package com.beust.app

import com.beust.sixty.MemoryInterceptor
import com.beust.sixty.h

object StepperMotor {
    val RANGE = 0xc0e0 .. 0xc0e7

    fun onRead(location: Int, value: Int, disk: IDisk): Int {
        val byte = when(location) {
            in RANGE -> {
                // Seek address: $b9a0
                val (phase, state) = when (location) {
                    0xc0e0 -> 0 to false
                    0xc0e1 -> 0 to true
                    0xc0e2 -> 1 to false
                    0xc0e3 -> 1 to true
                    0xc0e4 -> 2 to false
                    0xc0e5 -> 2 to true
                    0xc0e6 -> 3 to false
                    0xc0e7 -> 3 to true
                    else -> Pair(-1, false)
                }
                magnet(disk, phase, state)
                value
            }
            0xc0e8 -> {
                println("Turning motor off")
                value
            }
            0xc0e9 -> {
                println("Turning motor on")
                value
            }
            0xc0ea -> {
                println("Turning on drive 1")
                value
            }
            0xc0eb -> {
                println("Turning on drive 2")
                value
            }
            else -> value
        }
        return byte
    }

    private var magnets = BooleanArray(4) { _ -> false }
    private var phase = 0

    private fun magnet(disk: IDisk, index: Int, state: Boolean) {
        if (state) {
            when(phase) {
                0 -> {
                    if (index == 1) {
                        phase = 1
                        disk.incTrack()
                    } else if (index == 3) {
                        phase = 3
                        disk.decTrack()
                    }
                }
                1 -> {
                    if (index == 2) {
                        phase = 2
                        disk.incTrack()
                    } else if (index == 0) {
                        phase = 0
                        disk.decTrack()
                    }
                }
                2 -> {
                    if (index == 3) {
                        phase = 3
                        disk.incTrack()
                    } else if (index == 1) {
                        phase = 1
                        disk.decTrack()
                    }
                }
                3 -> {
                    if (index == 0) {
                        phase = 0
                        disk.incTrack()
                    } else if (index == 2) {
                        phase = 2
                        disk.decTrack()
                    }
                }
            }
        }

//        println("=== Track: "+ disk.track + " magnet $index=$state")
        magnets[index] = state
    }
}