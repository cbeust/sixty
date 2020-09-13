package com.beust.app

import com.beust.sixty.h
import com.beust.sixty.hh

object SixAndTwo {
    fun dump(disk: IDisk) {
        val sectors = hashMapOf<Int, Sector>()
        val result = arrayListOf<IntArray>()
        fun pair() = disk.nextByte().shl(1).or(1).and(disk.nextByte()).and(0xff)
        repeat(35) {
            repeat(16) {
                while (disk.peekBytes(3) != listOf(0xd5, 0xaa, 0x96)) {
                    disk.nextByte()
                }
                println("Found d5 aa 96")
                val s = disk.nextBytes(3)
                val volume = pair()
                val track = pair()
                val sector = pair()
                val checksumAddress = pair()
                if (volume.xor(track).xor(sector) != checksumAddress) {
                    TODO("Checksum doesn't match")
                }
                println("   Volume: $volume Track: $track Sector: $sector checksum: $checksumAddress")
                if (disk.nextBytes(3) != listOf(0xde, 0xaa, 0xeb)) {
                    TODO("Didn't find closing for address")
                }

                while (disk.peekBytes(3) != listOf(0xd5, 0xaa, 0xad)) {
                    disk.nextByte()
                }
                disk.nextBytes(3)

                val buffer = IntArray(342)
                var checksum = 0
                for (i in buffer.indices) {
                    val b = disk.nextByte()
                    if (READ_TABLE[b] == null) {
                        println("INVALID NIBBLE")
                    }
                    checksum = checksum xor READ_TABLE[b]!!
                    if (i < 86) {
                        buffer[buffer.size - i - 1] = checksum
                    } else {
                        buffer[i - 86] = checksum
                    }
                }
                val bh = disk.peekBytes(1).first().h()
                checksum = checksum xor READ_TABLE[disk.nextByte()]!!
                if (checksum != 0) {
                    TODO("BAD CHECKSUM")
                }

                val sectorData = IntArray(256)
                for (i in sectorData.indices) {
                    val b1: Int = buffer[i]
                    val lowerBits: Int = buffer.size - i % 86 - 1
                    val b2: Int = buffer[lowerBits]
                    val shiftPairs = i / 86 * 2
                    // shift b1 up by 2 bytes (contains bits 7-2)
                    // align 2 bits in b2 appropriately, mask off anything but
                    // bits 0 and 1 and then REVERSE THEM...
                    val reverseValues = intArrayOf(0x0, 0x2, 0x1, 0x3)
                    val b = b1 shl 2 or reverseValues[b2 shr shiftPairs and 0x03]
                    sectorData[i] = b
                }

                if (disk.nextBytes(3) != listOf(0xde, 0xaa, 0xeb)) {
                    TODO("Didn't find closing for data")
                }
                val ls = DskDisk.LOGICAL_SECTORS[sector]
                sectors[ls] = Sector(ls, sectorData)
//                println("  Successfully read sector $sector (logical: $ls)")
            }
            disk.incTrack()
        }
    }
}