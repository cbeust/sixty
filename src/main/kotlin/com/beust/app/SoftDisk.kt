package com.beust.app

import com.beust.sixty.MemoryInterceptor

/**
 * Soft switches that manipulate the disk drive.
 */
//object SoftDisk {
//    val RANGE = 0xc0e8..0xc0eb
//
//    fun onRead(location: Int, value: Int, disk: WozDisk): Int {
//        val byte = when(location) {
//            0xc0e8 -> {
//                println("Turning motor off")
//                value
//            }
//            0xc0e9 -> {
//                println("Turning motor on")
//                value
//            }
//            0xc0ea -> {
//                println("Turning on drive 1")
//                value
//            }
//            0xc0eb -> {
//                println("Turning on drive 2")
//                value
//            }
//            else -> {
//                TODO("Should never happen")
//            }
//        }
//
//        return byte
//    }
//
//}