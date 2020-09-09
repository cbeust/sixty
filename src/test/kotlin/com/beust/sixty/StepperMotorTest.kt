package com.beust.sixty

import org.testng.annotations.Test

@Test
class StepperMotorTest {
    class Disk {
        var track: Int = 0
        fun incTrack() = track++
        fun decTrack() = track--
    }
    val disk = Disk()

    var magnets = BooleanArray(4) { _ -> false }
    var phase = 0

    private fun magnet(index: Int, state: Boolean) {
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

        println("Magnet $index=$state")
        magnets[index] = state
    }

    fun test() {
        magnet(1, true) //.5
        magnet(0, false)
        magnet(2, true) // 1
        magnet(1, false)
        magnet(3, true) // 1.5
        magnet(2, false)
        magnet(0, true) // 2.0
        magnet(3, false)
        magnet(0, false)

        println("Disk track: "+ disk.track)
    }
}