package com.beust.app

import com.beust.sixty.ERROR
import com.beust.sixty.h

fun word(b1: Int, b2: Int): Int = b1.or(b2.shl(8))

object SixAndTwo {
    fun pair4And4(b1: Int, b2: Int) = b1.shl(1).or(1).and(b2).and(0xff)

    val detrans62 = listOf(
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
        0x00, 0x00, 0x02, 0x03, 0x00, 0x04, 0x05, 0x06,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x07, 0x08,
        0x00, 0x00, 0x00, 0x09, 0x0A, 0x0B, 0x0C, 0x0D,
        0x00, 0x00, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13,
        0x00, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x1B, 0x00, 0x1C, 0x1D, 0x1E,
        0x00, 0x00, 0x00, 0x1F, 0x00, 0x00, 0x20, 0x21,
        0x00, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x29, 0x2A, 0x2B,
        0x00, 0x2C, 0x2D, 0x2E, 0x2F, 0x30, 0x31, 0x32,
        0x00, 0x00, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38,
        0x00, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F
    )

    fun decodeData(disk: IDisk, track: Int, sector: Int): IntArray {
        val data = IntArray(0x100)
        val data2 = IntArray(0x56)
        var last = 0
        var jdx = 0x55
        while (jdx >= 0) {
            val value = detrans62[disk.nextByte() - 0x80].xor(last)
            data2[jdx] = value
            last = value
            jdx--
        }
        jdx = 0
        while (jdx < 0x100) {
            val value = detrans62[disk.nextByte() - 0x80].xor(last)
            data[jdx] = value
            last = value
            jdx++
        }
        var checksum = detrans62[disk.nextByte() - 0x80].xor(last)
        if (checksum != 0) {
            ERROR("BAD CHECKSUM: $checksum at track $track sector $sector")
        }
        println("   checksum: " + last.h())
        var kdx = 0
        jdx = 0x55
        while (kdx < 0x100) {
            data[kdx] = data[kdx].shl(1)
            if (data2[jdx].and(1) == 0) {
                data[kdx] = data[kdx].or(1)
            }
            data2[jdx] = data2[jdx].shr(1)
            if (--jdx < 0) jdx = 0x55
            kdx++
        }

        return data
    }

    private fun decodeData2(disk: IDisk, track: Int, sector: Int): IntArray {
        val buffer = IntArray(342)
        var checksum = 0
        for (i in buffer.indices) {
            val b = disk.nextByte()
            val rt = READ_TABLE[b] ?: 0
            checksum = checksum xor rt
            if (i < 86) {
                buffer[buffer.size - i - 1] = checksum
            } else {
                buffer[i - 86] = checksum
            }
        }
        val bh = disk.peekBytes(1).first().h()
        val readChecksum = READ_TABLE[disk.nextByte()]!!
        checksum = checksum xor readChecksum
        if (checksum != 0) {
            ERROR("BAD CHECKSUM: $checksum at track $track sector $sector")
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

        return sectorData
    }

    fun dump(disk: IDisk,
            closingAddress: List<Int> = listOf(0xde, 0xaa, 0xeb),
            closingData: List<Int> = listOf(0xde, 0xaa, 0xeb)) {
        val sectors = hashMapOf<Int, Sector>()
        val result = arrayListOf<IntArray>()
        fun pair4And4() = pair4And4(disk.nextByte(), disk.nextByte())
        repeat(35) { expectedTrack ->
            repeat(16) { expectedSector ->
                while (disk.peekBytes(3) != listOf(0xd5, 0xaa, 0x96) && disk.peekBytes(3) != listOf(0xd4, 0xaa, 0x96)) {
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
                if (disk.nextBytes(3) != closingAddress) {
                    ERROR("Didn't find closing for address")
                }

                while (disk.peekBytes(3) != listOf(0xd5, 0xaa, 0xad)) {
                    disk.nextByte()
                }
                disk.nextBytes(3)

                val sectorData = decodeData2(disk, track, sector)
//                val sectorData = decodeData(disk, track, sector)
                println("Successfully decoded track $track sector \$" + sector.h())

                val nb = disk.nextBytes(3)
                if (nb != closingData) {
                    TODO("Didn't find closing for data")
                }
                val ls = DskDisk.LOGICAL_SECTORS[sector]
                sectors[ls] = Sector(ls, sectorData)
//                println("  Successfully read sector $sector (logical: $ls)")
            }
            disk.incPhase()
            disk.incPhase()
        }
        repeat(40) { disk.decPhase() }
    }
}

data class Sector(val number: Int, val content: IntArray)
data class Track(val number: Int, val sectors: Map<Int, Sector>)
data class DiskContent(val tracks: List<Track>)

