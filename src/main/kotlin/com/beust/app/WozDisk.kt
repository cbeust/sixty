package com.beust.app

import com.beust.sixty.ERROR
import com.beust.sixty.bit
import com.beust.sixty.logWoz
import java.io.InputStream

fun main() {
    val d = IDisk.create(DISKS[10])!!
    SixAndTwo.dump(d, closingAddress = listOf(0xda, 0xaa, 0xeb), closingData = listOf(0xda, 0xaa, 0xeb))
//    SixAndTwo.dump(d)
    ""
}

class WozDisk(override val name: String, ins: InputStream): BaseDisk() {
    private val MAX_TRACK = 160

    /** 0..159 */
    var phase = 0
    private val bytes: ByteArray = ins.readAllBytes()
    private val woz = Woz(bytes)

    private var saved: Int = 0
    override fun save() { bitStream.save() }
    override fun restore() { bitStream.restore() }

    fun createBits(bytes: ByteArray): List<Int> {
        val result = arrayListOf<Int>()
        bytes.forEach {
            for (i in 0..8) {
                result.add(it.bit(7 - i))
            }
        }
        return result
    }

    override fun phaseSizeInBits(phase: Int): Int {
        return woz.bitStreamForPhase(phase).sizeInBits
    }

    private var changedTrack = false

    private fun updatePosition(oldPhase: Int, newPhase: Int) {
        println("UPDATE POSITION: $oldPhase -> $newPhase")
        val newTrack = woz.tmap.offsetFor(newPhase)
//        if (oldTrack != newTrack) {
            val oldBitStream = woz.bitStreamForPhase(oldPhase)
            val newBitStream = woz.bitStreamForPhase(newPhase)

            val oldTrackLength = oldBitStream.sizeInBits
            val newTrackLength = newBitStream.sizeInBits
            val position = oldBitStream.bitPosition
            val newPosition = position.toLong() * newTrackLength / oldTrackLength
            if (newPosition < 0) {
                ERROR("Negative new position, should never happen")
            }
        if (newTrack != -1) logWoz("New position for new phase $newTrack: " + newPosition)
        newBitStream.bitPosition = newPosition.toInt()
        changedTrack = true
    }

    val bitStream: IBitStream
        get() {
            val result = woz.bitStreamForPhase(phase)
            if (changedTrack) {
                println("Changed phase, returning bitstream for phase $phase bitPosition: " + result.bitPosition)
                changedTrack = false
            }
            return result
        }

    private fun movePhase(block: () -> Unit) {
        val t = phase
        block()
        if (t != phase) {
            updatePosition(t, phase)
//            println("New track: $" + track.h() + " (" + track / 4.0 + ")")
        }
    }

    override fun incPhase() {
        movePhase {
            phase++
            if (phase >= MAX_TRACK) phase = MAX_TRACK - 1
        }
    }

    override fun decPhase() = movePhase {
        if (phase > 0) phase--
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

    override val sizeInBits: Int
        get() = phaseSizeInBits(phase)

    override var bitPosition = bitStream.bitPosition

    private var headWindow = 0

    override fun peekZeroBitCount(): Int {
        bitStream.save()
        var result = 0
        while (bitStream.nextBit() == 0) result++
        bitStream.restore()
        return result
    }

    override fun nextBit(): Int = bitStream.nextBit()
//        bitStream.nextBit(position).let { result ->
//            bitPosition = result.component1()
//            return result
//            headWindow = headWindow shl 1
//            headWindow = headWindow or bit
//            return if (headWindow and 0x0f !== 0x00) {
//                headWindow and 0x02 shr 1
//            } else {
//                FAKE_BIT_STREAM.next()
//            }
//        }

    override fun nextByte() = nextByte(false)

    private var zeroCount = 0
    fun nextByte(peek: Boolean = false): Int {
        var bits = arrayListOf(0)
        var result = 0
        if (peek) {
            save()
        }
        var iterated = 0
        while (result and 0x80 == 0) {
            val nb = bitStream.nextBit()
            iterated++
            bits.add(nb)
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