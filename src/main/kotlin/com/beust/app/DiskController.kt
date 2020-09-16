package com.beust.app

import com.beust.sixty.*

class DiskController(val slot: Int = 6, val disk: IDisk): IPulse, MemoryListener() {
    private val slot16 = slot * 16
    private var latch: Int = 0

    private var motorOn = false
    private var drive1 = true
    private var drive2 = false

    private fun c(address: Int) = address + slot16
    override fun isInRange(address:Int) = address in (c(0xc080)..c(0xc08c))

    override fun onPulse(): PulseResult {
        // Faster way for unprotected disks
//        latch = disk.nextByte()

        if (motorOn) {
//     More formal way: bit by bit on every pulse
            if (latch and 0x80 == 0) {
                repeat(8) {
                    latch = latch.shl(1).or(disk.nextBit()).and(0xff)
                }
                while (latch and 0x80 == 0) {
                    latch = latch.shl(1).or(disk.nextBit()).and(0xff)
                }
            }
        }
//        println("@@@   latch: ${latch.h()}")
        return PulseResult()
    }


    override fun onRead(i: Int, value: Int): Int? {
        val a = i - slot16
        val result = when(a) {
            in (0xc080..0xc087) -> {
                // Seek address: $b9a0
                val (phase, state) = when (a) {
                   0xc080 -> 0 to false
                   0xc081 -> 0 to true
                   0xc082 -> 1 to false
                   0xc083 -> 1 to true
                   0xc084 -> 2 to false
                   0xc085 -> 2 to true
                   0xc086 -> 3 to false
                   0xc087 -> 3 to true
                    else -> ERROR("SHOULD NEVER HAPPEN")
                }
                magnet(disk, phase, state)
                value
            }
            0xc088 -> {
                println("Turning motor off")
                motorOn = false
                value
            }
            0xc089 -> {
                println("Turning motor on")
                motorOn = true
                value
            }
            0xc08a -> {
                println("Turning on drive 1")
                drive1 = true
                value
            }
            0xc08b -> {
                println("Turning on drive 2")
                drive2 = true
                value
            }
            0xc08c -> {
//                latch = disk.nextByte()
                val result = latch
                if (latch.and(0x80) != 0) latch = latch.and(0x7f) // clear bit 7
                result
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