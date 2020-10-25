package com.beust.app

import com.beust.sixty.logDisk

//fun main() {
//    val disk = WozDisk("", DISKS[1].inputStream())
//    NibbleTrack(disk.bitStream, disk.phaseSizeInBits(0), createMarkFinders())
//}

class TimedByte(val byte: Int, val timingBitCount: Int = 0)

interface IMarkFinders {
    fun isAddressPrologue(bytes: List<Int>): Boolean = bytes == listOf(0xd5, 0xaa, 0x96)
    fun isAddressEpilogue(bytes: List<Int>): Boolean = bytes == listOf(0xde, 0xaa)
    fun isDataPrologue(bytes: List<Int>): Boolean = bytes == listOf(0xd5, 0xaa, 0xad)
    fun isDataEpilogue(bytes: List<Int>): Boolean = bytes == listOf(0xde, 0xaa)
}

class DefaultMarkFinders: IMarkFinders

class BouncingMarkFinders: IMarkFinders {
    override fun isAddressPrologue(bytes: List<Int>)
        = (bytes[0] == 0xd4 || bytes[0] == 0xd5) && bytes[1] == 0xaa && bytes[2] == 0x96
    override fun isAddressEpilogue(bytes: List<Int>) = bytes == listOf(0xda, 0xaa)
    override fun isDataEpilogue(bytes: List<Int>) = bytes == listOf(0xda, 0xaa)
}

class NibbleTrack(bitStream: IBitStream, val sizeInBits: Int, private val markFinders: IMarkFinders) {
    val bytes = arrayListOf<TimedByte>()
    var analyzedTrack: AnalyzedTrack? = null

    private val tmpBytes = arrayListOf<TimedByte>()

    init {
        fun peekZeros(): Int {
            var result = 0
            bitStream.save()
            while (bitStream.nextBit() == 0) {
                result++
            }
            bitStream.restore()
            return result
        }

        var done = false
        var bitsRead = 0
        var readCounts = 0
        var start = -1
        var end = -1
        while (!done) {
            var byte = 0
            while (byte and 0x80 == 0) {
                val pair = bitStream.nextBit()
                bitsRead++
                if (bitsRead % sizeInBits == 0) {
                    readCounts++
                    if (readCounts == 1) {
                        start = tmpBytes.size
                    } else if (readCounts == 2) {
                        end = tmpBytes.size
                        done = true
                    }
                }
                byte = byte.shl(1).or(pair)
            }
            val zeros = peekZeros()
            tmpBytes.add(TimedByte(byte, zeros))
        }
        val slice = tmpBytes.slice(start..end)
        bytes.addAll(tmpBytes)
//        bytes.addAll(tmpBytes.slice(start..end))
        tmpBytes.clear()
        analyzeTrack()
    }

    fun analyzeTrack() {
        analyzedTrack = analyze(markFinders)
    }

    data class SectorInfo(val volume: Int, val track: Int, val sector: Int, val checksum: Int)

    fun sectorInfoForIndex(index: Int): SectorInfo? {
        val result =
            if (analyzedTrack == null) null
            else analyzedTrack?.let { at ->
                val tr = at.trackRanges.filter { it.isAddress }.reversed().firstOrNull { it.range.first < index }
                if (tr != null) {
                    val ind = tr.range.start
                    val volume = SixAndTwo.pair4And4(bytes[ind].byte, bytes[ind + 1].byte)
                    val track = SixAndTwo.pair4And4(bytes[ind + 2].byte, bytes[ind + 3].byte)
                    val sector  = SixAndTwo.pair4And4(bytes[ind + 4].byte, bytes[ind + 5].byte)
                    val checksum  = SixAndTwo.pair4And4(bytes[ind + 6].byte, bytes[ind + 7].byte)
                    SectorInfo(volume, track, sector, checksum)
                } else {
                    null
                }
            }
        return result
    }

    private fun peek(pos: Int, count: Int): List<Int> {
        val result = arrayListOf<Int>()
        var i = pos % bytes.size
        repeat(count) {
            result.add(bytes[i].byte)
            i = (i + 1) % bytes.size

        }
        return result
    }

    private fun findNextChunk(position: Int,
            isPrologue: (List<Int>) -> Boolean, isEpilogue: (List<Int>) -> Boolean): IntRange?
    {
        var i = position
        var tries = 0
        while (tries < bytes.size && ! isPrologue(peek(i, 3))) {
            i = (i + 1) % bytes.size
            tries++
        }
        return if (tries < bytes.size) {
            tries = 0
            val start = i + 3
            while (tries < bytes.size && !isEpilogue(peek(i, 2))) {
                i = (i + 1) % bytes.size
                tries++
            }
            if (tries < bytes.size) {
                IntRange(start, i - 1)
            } else {
                null
            }
        } else {
            null
        }
    }

    data class TrackRange(val isAddress: Boolean, val range: IntRange)
    class AnalyzedTrack(val trackRanges: List<TrackRange>, val sizeInBits: Int) {
        enum class Type { ADDRESS, DATA, NONE }
        fun typeFor(index: Int): Type {
            val tr = trackRanges.firstOrNull { index in it.range }
            return if (tr == null) Type.NONE
            else if (tr.isAddress) Type.ADDRESS
            else Type.DATA
        }
    }

    private fun analyze(markFinders: IMarkFinders = DefaultMarkFinders()): AnalyzedTrack {
        val trackRanges = arrayListOf<TrackRange>()
        var i = 0
        var sectors = 0
        while (sectors < 16) {
            val addressRange = findNextChunk(i, markFinders::isAddressPrologue, markFinders::isAddressEpilogue)
            if (addressRange != null) {
                i = addressRange.endInclusive
                trackRanges.add(TrackRange(true, addressRange))
            }

            val dataRange = findNextChunk(i, markFinders::isDataPrologue, markFinders::isDataEpilogue)
            if (dataRange != null) {
                i = dataRange.endInclusive
                trackRanges.add(TrackRange(false, dataRange))

                // Verify checksum
                var chesksum = 0
                val start = dataRange.start
                val end = dataRange.endInclusive
                for (j in start..end) {
                    chesksum = chesksum.xor(READ_TABLE[bytes[j].byte]!!)
                }
                logDisk("Sector $sectors, address: $addressRange, data: $dataRange checksum: $chesksum")
            }
            sectors++
        }

        return AnalyzedTrack(trackRanges, sizeInBits)
    }

    private var byteIndex = 0
    fun nextByte(): TimedByte {
        val result = bytes[byteIndex]
        byteIndex = (byteIndex + 1) % bytes.size
        return result
    }
}