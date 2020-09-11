package com.beust.app

import com.beust.sixty.MemoryInterceptor

/**
 * Soft switches that manipulate the disk drive.
 */
object SoftDisk {
    val RANGE = 0xc0e8..0xc0ec

    fun onRead(location: Int, value: Int, disk: WozDisk): Int {
        val byte = when(location) {
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
            0xc0ec -> {
//                    val v = if (value and 0x80 != 0) 0 else value
//                    val result = v.shl(1).or(disk.nextBit()).and(0xff)
//                    if (result == 0xd5 || result == 0x96 || result == 0xad) {
//                        val rh = result.h()
//                        println("MAGIC: $result")
//                    }
                val result = disk.nextByte()
                result
            }
            else -> {
                TODO("Should never happen")
            }
        }

        return byte
    }

}