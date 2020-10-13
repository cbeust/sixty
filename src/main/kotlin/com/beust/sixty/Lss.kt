package com.beust.sixty

import com.beust.app.IDisk

class Lss {
    private val P6 = listOf(
            //                Q7 L (Read)                                         Q7 H (Write)
            //       Q6 L                     Q6 H                   Q6 L (Shift)               Q6 H (Load)
            //  QA L        QA H         QA L        QA H           QA L        QA H         QA L        QA H
            //1     0     1     0      1     0     1     0        1     0     1     0      1     0     1     0
            0x18, 0x18, 0x18, 0x18, 0x0A, 0x0A, 0x0A, 0x0A, 0x18, 0x18, 0x18, 0x18, 0x18, 0x18, 0x18, 0x18, // 0
            0x2D, 0x2D, 0x38, 0x38, 0x0A, 0x0A, 0x0A, 0x0A, 0x28, 0x28, 0x28, 0x28, 0x28, 0x28, 0x28, 0x28, // 1
            0xD8, 0x38, 0x08, 0x28, 0x0A, 0x0A, 0x0A, 0x0A, 0x39, 0x39, 0x39, 0x39, 0x3B, 0x3B, 0x3B, 0x3B, // 2
            0xD8, 0x48, 0x48, 0x48, 0x0A, 0x0A, 0x0A, 0x0A, 0x48, 0x48, 0x48, 0x48, 0x48, 0x48, 0x48, 0x48, // 3
            0xD8, 0x58, 0xD8, 0x58, 0x0A, 0x0A, 0x0A, 0x0A, 0x58, 0x58, 0x58, 0x58, 0x58, 0x58, 0x58, 0x58, // 4
            0xD8, 0x68, 0xD8, 0x68, 0x0A, 0x0A, 0x0A, 0x0A, 0x68, 0x68, 0x68, 0x68, 0x68, 0x68, 0x68, 0x68, // 5
            0xD8, 0x78, 0xD8, 0x78, 0x0A, 0x0A, 0x0A, 0x0A, 0x78, 0x78, 0x78, 0x78, 0x78, 0x78, 0x78, 0x78, // 6
            0xD8, 0x88, 0xD8, 0x88, 0x0A, 0x0A, 0x0A, 0x0A, 0x08, 0x08, 0x88, 0x88, 0x08, 0x08, 0x88, 0x88, // 7
            0xD8, 0x98, 0xD8, 0x98, 0x0A, 0x0A, 0x0A, 0x0A, 0x98, 0x98, 0x98, 0x98, 0x98, 0x98, 0x98, 0x98, // 8
            0xD8, 0x29, 0xD8, 0xA8, 0x0A, 0x0A, 0x0A, 0x0A, 0xA8, 0xA8, 0xA8, 0xA8, 0xA8, 0xA8, 0xA8, 0xA8, // 9
            0xCD, 0xBD, 0xD8, 0xB8, 0x0A, 0x0A, 0x0A, 0x0A, 0xB9, 0xB9, 0xB9, 0xB9, 0xBB, 0xBB, 0xBB, 0xBB, // A
            0xD9, 0x59, 0xD8, 0xC8, 0x0A, 0x0A, 0x0A, 0x0A, 0xC8, 0xC8, 0xC8, 0xC8, 0xC8, 0xC8, 0xC8, 0xC8, // B
            0xD9, 0xD9, 0xD8, 0xA0, 0x0A, 0x0A, 0x0A, 0x0A, 0xD8, 0xD8, 0xD8, 0xD8, 0xD8, 0xD8, 0xD8, 0xD8, // C
            0xD8, 0x08, 0xE8, 0xE8, 0x0A, 0x0A, 0x0A, 0x0A, 0xE8, 0xE8, 0xE8, 0xE8, 0xE8, 0xE8, 0xE8, 0xE8, // D
            0xFD, 0xFD, 0xF8, 0xF8, 0x0A, 0x0A, 0x0A, 0x0A, 0xF8, 0xF8, 0xF8, 0xF8, 0xF8, 0xF8, 0xF8, 0xF8, // E
            0xDD, 0x4D, 0xE0, 0xE0, 0x0A, 0x0A, 0x0A, 0x0A, 0x88, 0x88, 0x08, 0x08, 0x88, 0x88, 0x08, 0x08  // F
    )

    private var clock = 0
//    var _lastCycles = io.cycles()
    private var state = 0
    private var zeros = 0
    var latch = 0

    fun onPulse(q6: Boolean, q7: Boolean, motorOn: () -> Boolean, disk: IDisk) {
        var pulse = 0
        if (clock == 4) {

            pulse = disk.nextBit().second
            if (pulse == 0) {
                if (++zeros > 2) {
                    pulse = if (Math.random() > 0.3) 0 else 1
                }
            } else {
                zeros = 0
            }
        }

        var idx = 0
        idx = idx.or(if (pulse == 1) 0x00 else 0x01)
        idx = idx.or(if (latch.and(0x80) > 0) 0x02 else 0x00)
        idx = idx.or(if (q6) 0x04 else 0x00)
        idx = idx.or(if (q7) 0x08 else 0x00)
        idx = idx.or(state.shl(4))

        val command = P6[idx]
        when (command.and(0xf)) {
            0 -> { // CLR
                latch = 0
            }
            8 -> {
            } // NOP
            9 -> { // SL0
                latch = latch.shl(1).and(0xff)
            }
            0xa -> { // SR
                latch = latch.shr(1)
//                if (_cur.readOnly) {
//                    _latch = _latch or 0x80
//                }

            }
            0xb -> { // LD
                TODO("LD command for LSS")
                // latch = bus
            }
            0xd -> { // SL1
                latch = latch.shl(1).or(1).and(0xff)
            }
        }

        state = command.shr(4)

        if (clock == 4) {
            if (motorOn()) {
                if (q7) {
                    TODO("WRITE MODE NOT SUPPORTED)")
                    // track[_cur.head] = _state & 0x8 ? 0x01 : 0x00;
                }

            }
        }
        if (++clock > 7) clock = 0
    }
}