package com.beust.app

import com.beust.sixty.ERROR
import com.beust.sixty.MemoryListener

class DiskController(val slot: Int = 6, val disk: IDisk): IPulse, MemoryListener() {
    private val slot16 = slot * 16
    private var latch: Int = 0

    override fun isInRange(address:Int) = address in (c(0xc080)..c(0xc08c))

    override fun onPulse() {
        // Faster way for unprotected disks
//        latch disk.nextByte()

//     More formal way: bit by bit on every pulse
        if (latch and 0x80 != 0) latch = 0
        latch = latch.shl(1).or(disk.nextBit()).and(0xff)
    }

    private fun c(address: Int) = address + slot16

    override fun onRead(i: Int, value: Int): Int? {
        val result = when(i) {
            in (c(0xc080)..c(0xc087)) -> {
                // Seek address: $b9a0
                val (phase, state) = when (i) {
                    c(0xc080) -> 0 to false
                    c(0xc081) -> 0 to true
                    c(0xc082) -> 1 to false
                    c(0xc083) -> 1 to true
                    c(0xc084) -> 2 to false
                    c(0xc085) -> 2 to true
                    c(0xc086) -> 3 to false
                    c(0xc087) -> 3 to true
                    else -> ERROR("SHOULD NEVER HAPPEN")
                }
                magnet(disk, phase, state)
                value
            }
            c(0xc088) -> {
                println("Turning motor off")
                value
            }
            c(0xc089) -> {
                println("Turning motor on")
                value
            }
            c(0xc08a) -> {
                println("Turning on drive 1")
                value
            }
            c(0xc08b) -> {
                println("Turning on drive 2")
                value
            }
            c(0xc08c) -> {
                latch = disk.nextByte()
                latch
            }
            else -> value
        }

        return result
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
        if (index == -1) {
            println("PROBLEM")
        }
        magnets[index] = state
    }
}