package com.beust.app

import com.beust.sixty.ERROR
import com.beust.sixty.h

object SixAndTwo {
    fun pair4And4(b1: Int, b2: Int) = b1.shl(1).or(1).and(b2).and(0xff)

    fun dump(disk: IDisk) {
        val sectors = hashMapOf<Int, Sector>()
        val result = arrayListOf<IntArray>()
        fun pair4And4() = pair4And4(disk.nextByte(), disk.nextByte())
        repeat(35) { expectedTrack ->
            repeat(16) { expectedSector ->
                while (disk.peekBytes(3) != listOf(0xd5, 0xaa, 0x96)) {
                    disk.nextByte()
                }
                disk.nextBytes(3)
                val volume = pair4And4()
                val track = pair4And4()
                if (track != expectedTrack) {
                    ERROR("Expected track $expectedTrack, was $track")
                }
                val sector = pair4And4()
                if (sector != expectedSector) {
//                    ERROR("Expected sector $expectedSector, was $sector")
                }
                val checksumAddress = pair4And4()
                if (volume.xor(track).xor(sector) != checksumAddress) {
                    ERROR("Checksum doesn't match")
                }
                println("   Volume: $volume Track: $track Sector: $sector checksum: $checksumAddress")
                if (disk.nextBytes(3) != listOf(0xde, 0xaa, 0xeb)) {
                    ERROR("Didn't find closing for address")
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
        repeat(40) { disk.decTrack() }
    }
}