package com.beust.app

import com.beust.sixty.bit
import com.beust.sixty.h
import java.io.File
import java.io.InputStream

fun main() {
    val d = IDisk.create(disks[10])!!
    SixAndTwo.dump(d, closingAddress = listOf(0xda, 0xaa, 0xeb), closingData = listOf(0xda, 0xaa, 0xeb))
//    SixAndTwo.dump(d)
    ""
}

class WozDisk(override val name: String, ins: InputStream): BaseDisk(), IByteStream {

    constructor(file: File): this(file.absolutePath, file.inputStream())

    private val MAX_TRACK = 160

    override var position: Int = 0
    private var saved: Int = 0

    var track = 0
//    var bitPosition = 0
    private val bytes: ByteArray = ins.readAllBytes()
    private val woz = Woz(bytes)

    private fun save() { saved = position }
    private fun restore() { position = saved }

    fun createBits(bytes: ByteArray): List<Int> {
        val result = arrayListOf<Int>()
        bytes.forEach {
            for (i in 0..8) {
                result.add(it.bit(7 - i))
            }
        }
        return result
    }

    override fun phaseSizeInBytes(phase: Int): Int {
        return woz.bitStreamForTrack(phase).sizeInBytes
    }


    private fun updatePosition(oldTrack: Int, newTrack: Int) {
//        val oldTrackLength = woz.trks.trks[oldTrack].bitCount / 8
//        val newTrackLength = woz.trks.trks[newTrack].bitCount / 8
//        if (oldTrackLength != 0 && newTrack != 0) {
//            position = position * newTrackLength / oldTrackLength
////            println("Update position oldTrack: $oldTrack -> $newTrack, new position: $position")
//        }
    }

    private fun moveTrack(block: () -> Unit) {
        val t = track
        block()
        if (t != track) {
            updatePosition(t, track)
//            println("New track: $" + track.h() + " (" + track / 4.0 + ")")
        }
    }

    override fun incTrack() {
        moveTrack {
            track++
            if (track >= MAX_TRACK) track = MAX_TRACK - 1
        }
    }

    override fun decTrack() = moveTrack {
        if (track > 0) track--
    }

    override fun peekZeroBitCount(): Int {
        var result = 0
        save()
        while (nextBit() == 0) result++
        restore()
        return result
    }

    override fun peekBytes(count: Int): ArrayList<Int> {
        val result = arrayListOf<Int>()
        save()
        repeat(count) {
            result.add(nextByte())
        }
        restore()
        return result
    }

    fun peekByte() = nextByte(true)

    override fun nextBytes(count: Int) : List<Int> {
        val result = arrayListOf<Int>()
        repeat(count) {
            result.add(nextByte())
        }
        return result
    }

    val bitStream: IBitStream
        get() = woz.bitStreamForTrack(track)

    private var headWindow = 0

    override fun nextBit(): Int {
        bitStream.next(position).let { (newPosition, bit) ->
            position = newPosition
            return bit
//            headWindow = headWindow shl 1
//            headWindow = headWindow or bit
//            return if (headWindow and 0x0f !== 0x00) {
//                headWindow and 0x02 shr 1
//            } else {
//                FAKE_BIT_STREAM.next()
//            }
        }
    }

    override fun nextByte() = nextByte(false)

    private var zeroCount = 0
    fun nextByte(peek: Boolean = false): Int {
        var bits = arrayListOf(0)
        var result = 0
        if (peek) {
            save()
        }
        while (result and 0x80 == 0) {
            val (newPosition, nb) = bitStream.next(position)
            bits.add(nb)
            position = newPosition
//            val nb = nextBit(peek, ahead * 8)
            result = result.shl(1).or(nb)
            if (nb == 0) {
                if (zeroCount >= 2) {
                    result = 0
                }
                zeroCount++
            } else {
                zeroCount = 0
            }
        }
        if (peek) {
            restore()
        }
        if (result == 0x94) {
            println("PROBLEM")
        }
        return result
    }

    /**
     * @return the next byte and the stream size in bits
     */
//    fun calculateCurrentByte(currentBitPosition: Int): Pair<Int, Int> {
//        val tmapOffset = woz.tmap.offsetFor(track)
//        val trk = woz.trks.trks[tmapOffset]
//        val streamSizeInBytes = (trk.bitCount / 8)
//        val relativeOffset = currentBitPosition / 8
//        val byteOffset = (trk.startingBlock * 512 + relativeOffset) % streamSizeInBytes
//        val byte = bytes[byteOffset].toInt().and(0xff)
//        return byte to trk.bitCount
//    }
}