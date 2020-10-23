package com.beust.app

import com.beust.sixty.h

/**
 * State machine to parse the sector info following D5 AA 96.
 */
class AddressState {
    enum class State {
        START, D5, D5AA, D5AA96, VOLUME0, VOLUME1, TRACK0, TRACK1, SECTOR0, SECTOR1, CHECKSUM0, CHECKSUM1
    }
    private var state = State.START
    private var b0 = 0
    private var volume = 0
    private var track = 0
    private var sector = 0
    private var checksum = 0

    fun readByte(byte: Int) {

        fun reset() {
            state = State.START
            b0 = 0
            volume = 0
            track = 0
            sector = 0
            checksum = 0
        }

        if (byte == 0xd5) state = State.D5
        else if (byte == 0xaa && state == State.D5) state = State.D5AA
        else if (byte == 0x96 && state == State.D5AA) state = State.D5AA96
        else {
            when(state) {
                State.D5AA96 -> {
                    b0 = byte
                    state = State.VOLUME0
                }
                State.VOLUME0 -> {
                    volume = SixAndTwo.pair4And4(b0, byte)
                    state = State.VOLUME1
                }
                State.VOLUME1 -> {
                    b0 = byte
                    state = State.TRACK0
                }
                State.TRACK0 -> {
                    track = SixAndTwo.pair4And4(b0, byte)
                    state = State.TRACK1
//                    println("Read track $track")
                }
                State.TRACK1 -> {
                    b0 = byte
                    state = State.SECTOR0
                }
                State.SECTOR0 -> {
                    sector = SixAndTwo.pair4And4(b0, byte)
//                    println("Read sector $sector")
                    state = State.SECTOR1
                }
                State.SECTOR1 -> {
                    b0 = byte
                    state = State.CHECKSUM0
                }
                State.CHECKSUM0 -> {
                    checksum = SixAndTwo.pair4And4(b0, byte)
                    UiState.currentSectorInfo.value = NibbleTrack.SectorInfo(volume, track, sector, checksum)
                    reset()
                }
                else -> {
                    reset()
                }
            }
        }
    }
}