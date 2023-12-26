package com.beust.app

import com.beust.sixty.*
import java.util.concurrent.TimeUnit

class DiskController(val slot: Int = 6): MemoryListener() {
    private val slot16 = slot * 16
    private var latch: Int = 0
    private var drive1 = true
    private var motor = Motor { -> drive1 }
    private var q6 = false
    private var q7 = false
    private val lss = Lss()

    enum class MotorState {
        ON, OFF, SPINNING_DOWN;
    }

    class Motor(private val drive1: () -> Boolean) {
        private fun updateUi(state: MotorState) {
            UiState.diskStates[if (drive1()) 0 else 1].motor.value = state
        }

        private var status: MotorState = MotorState.OFF
            set(f) {
                if (f == MotorState.ON) {
                    updateUi(MotorState.ON)
                    field = MotorState.ON
                } else if (f == MotorState.OFF) {
                    if (field == MotorState.ON) {
                        logDisk("Scheduling motor off")
                        // Turn off the motor after a second, unless it was turned on in the meantime
                        Cycles.motorOff.add(CycleAction(4_000_000) {
                            // Make sure we're still spinning down and not back on
                            if (field == MotorState.SPINNING_DOWN) {
                                logDisk("Turning motor off after a second")
                                updateUi(MotorState.OFF)
                                field = MotorState.OFF
                            } // Motor was turned on while spinning down: not turning it off
                        })
                        logDisk("Motor spinning down")
                        field = MotorState.SPINNING_DOWN
                        updateUi(MotorState.SPINNING_DOWN)
                    } // we're already OFF or SPINNING_DOWN, nothing to do
                }
            }

        fun turn(on: Boolean) {
            status = if (on) {
//                log("Motor back on, canceling OFF tasks")
                Cycles.motorOff.clear()
                MotorState.ON
            } else {
                MotorState.OFF
            }
        }
        val isOn: Boolean get() = status == MotorState.ON || status == MotorState.SPINNING_DOWN
    }

    private fun c(address: Int) = address + slot16
    override fun isInRange(address: Int) = address in (c(0xc080)..c(0xc08f))

    // 55 works for paddles
    // 75 for kamungas
//    private val HOLD = 36
    private var hold = 0

    private var headWindow = 0
    private val FAKE_BIT_STREAM = FakeBitStream(-1, -1)

    private fun nextBitWithWindow(disk: IDisk): Int {
        val bit = disk.nextBit()
        headWindow = headWindow shl 1
        headWindow = headWindow or bit
        return if (headWindow and 0x0f !== 0x00) {
            headWindow and 0x02 shr 1
        } else {
            FAKE_BIT_STREAM.nextBit()
        }
    }

    private var clock = 0
    private var nextQa = 0

    fun step(): Computer.RunStatus {
        // Use the LSS
        when(NIBBLE_STRATEGY) {
            NibbleStrategy.LSS -> {
                disk()?.let { disk ->
                    lss.onPulse(q6, q7, { -> motor.isOn }, disk)
                    latch = lss.latch
                }
            }
            NibbleStrategy.BYTES -> {
                // Faster way for unprotected disks
                disk()?.let {
                    if (motor.isOn) {
                        if (latch.and(0x80) == 0) {
                            //                latch = latch.shl(1).or(it.nextBit())
                            latch = it.nextByte()
                            println("Loaded new byte, holding for ${NIBBLE_STRATEGY.hold}")
                            hold = NIBBLE_STRATEGY.hold
                        } else if (hold > 0) {
                            hold--
                        } else {
                            println("Missed nibble: " + latch.h())
                            latch = 0
                        }
                    }
                }
            }
            NibbleStrategy.BITS -> {
                disk()?.let {
                    if (motor.isOn) {
                        if (clock++ % 8 == 0) {
                            val newBit = nextBitWithWindow(it)
                            if (latch.and(0x80) > 0) { // qa is set
                                if (newBit == 0 && nextQa == 0) {
                                    // do nothing, this is how we sync
                                } else if (newBit == 1 && nextQa == 0) {
                                    nextQa = 1
                                } else if (nextQa == 1) {
                                    latch = 2.or(newBit)
                                    nextQa = 0
                                }
                            } else {// qa not set
                                latch = latch.shl(1).or(newBit)
                            }
//                            logAsm("Newbit: $newBit, nextQa: $nextQa, latch: " + latch.h())
                            ""
                        }
                        if (TRACE_ON) {
                            val l = String.format("%1$02X", latch)
                            logAsmTrace("@@ clock=$clock bitPosition=${disk()!!.bitPosition} latch=$l")
                        }
                    }
                }
            }
        }

        return Computer.RunStatus.RUN
    }

    private var disk1: IDisk? = null
    private var disk2: IDisk? = null

    fun disk() = if (drive1) disk1 else disk2
    /** @param drive 0 for drive 1, 1 for drive 2 */
    fun loadDisk(disk: IDisk?, drive: Int = 0) {
        logDisk("Loading disk $disk in drive " + (drive + 1))
        when(drive) {
            0 -> disk1 = disk
            1 -> disk2 = disk
            else -> ERROR("INCORRECT DRIVE")
        }
    }

    override fun onWrite(location: Int, value: Int) {
        handle(location, value)
    }

    override fun onRead(location: Int, value: Int): Int? {
        return handle(location, value)
    }

    /** 4 bits, one for each phase */
    private val phaseBits = arrayListOf(0, 0, 0, 0)

    private val transitions = listOf(
            //fr\to       9  8  D  C  4  E  6  2  7  3  1  B  0  5  A  F
            /*9*/ listOf(0, -1, -1, -2, -3, -3, 0, +3, +3, +2, +1, +1, 0, 0, 0, 0),
            /*8*/ listOf(+1, 0, 0, -1, -2, -2, -3, 0, 0, +3, +2, +2, 0, 0, 0, 0),
            /*D*/ listOf(+1, 0, 0, -1, -2, -2, -3, 0, 0, +3, +2, +2, 0, 0, 0, 0),
            /*C*/ listOf(+2, +1, +1, 0, -1, -1, -2, -3, -3, 0, +3, +3, 0, 0, 0, 0),
            /*4*/ listOf(+3, +2, +2, +1, 0, 0, -1, -2, -2, -3, 0, 0, 0, 0, 0, 0),
            /*E*/ listOf(+3, +2, +2, +1, 0, 0, -1, -2, -2, -3, 0, 0, 0, 0, 0, 0),
            /*6*/ listOf(0, +3, +3, +2, +1, +1, 0, -1, -1, -2, -3, -3, 0, 0, 0, 0),
            /*2*/ listOf(-3, 0, 0, +3, +2, +2, +1, 0, 0, -1, -2, -2, 0, 0, 0, 0),
            /*7*/ listOf(-3, 0, 0, +3, +2, +2, +1, 0, 0, -1, -2, -2, 0, 0, 0, 0),
            /*3*/ listOf(-2, -3, -3, 0, +3, +3, +2, +1, +1, 0, -1, -1, 0, 0, 0, 0),
            /*1*/ listOf(-1, -2, -2, -3, 0, 0, +3, +2, +2, +1, 0, 0, 0, 0, 0, 0),
            /*B*/ listOf(-1, -2, -2, -3, 0, 0, +3, +2, +2, +1, 0, 0, 0, 0, 0, 0)
    )

    private val addressState = AddressState()

    /**
     * Magnet states, just 4 bits.
     */
    private var magnetStates = 0
    private var drivePhase = 0
    private var drivePhasePrecise = 0.0f

    private fun toBinaryString(n: Int): String {
        val sb = arrayListOf<String>()
        var c = n
        repeat(4) {
            sb.add(0, if (c % 2 == 0) "0" else "1")
            c /= 2
        }

        return sb.joinToString("")
    }

//    fun BitSet.toBitString(): String {
//        val result = arrayListOf<Int>()
//        repeat(cardinality()) {
//            result.add(if (this[it]) 1 else 0)
//        }
//        val r = result.joinToString("")
//        return r
//    }

    private fun updateStepper(address: Int) {
        // update phases (magnet states)
        val phase = address.shr(1).and(3)
        val phaseBit = 1.shl(phase);

        // update the magnet states
        if (address.and(1) != 0) {
            magnetStates = magnetStates.or(phaseBit)
            if (TRACE_ON)
                logAsmTrace("@@ magnetStates=$magnetStates")
        } else {
            magnetStates = magnetStates.and(phaseBit.inv())
            if (TRACE_ON)
                logAsmTrace("@@ magnetStates=$magnetStates")
        }

        // check for any stepping effect from a magnet
        // - move only when the magnet opposite the cog is off
        // - move in the direction of an adjacent magnet if one is on
        // - do not move if both adjacent magnets are on (ie. quarter track)
        // momentum and timing are not accounted for ... maybe one day!
        var direction = 0
        if (magnetStates and (1.shl((drivePhase + 1).and(3))) != 0) direction += 1
        if (magnetStates and (1.shl((drivePhase + 3).and(3))) != 0) direction -= 1
        currentPhase += direction

        if (currentPhase < 0) currentPhase = 0
        if (currentPhase >= IDisk.PHASE_MAX) currentPhase = IDisk.PHASE_MAX - 1
//        println("   head direction: " + direction)

        var quarterDirection = 0
        if (magnetStates === 0xC || // 1100
                magnetStates === 0x6 || // 0110
                magnetStates === 0x3 || // 0011
                magnetStates === 0x9) // 1001
        {
            quarterDirection = direction
            direction = 0
        }

        drivePhase = Math.max(0, Math.min(79, drivePhase + direction));

        var newPhasePrecise = drivePhase.toFloat() + (quarterDirection * 0.5f)
        if (newPhasePrecise < 0) newPhasePrecise = 0f

        if (newPhasePrecise != drivePhasePrecise) {
            drivePhasePrecise = newPhasePrecise
        }

        val track = currentTrackString()
        logDisk("Track \$$track magnet: " + toBinaryString(magnetStates)
                + " direction: " + direction
                + " drivePhase: " + drivePhase
                + " phase: " + (address.shr(1).and(3))
                + " " + (if (address.and(1) == 1) "on " else "off")
                + " address: " + address.hh())

        //
        // Move the head, but we need to insert a small delay  for the first move if it wasn't in motion already.
        // For subsequent ones, the head can move right away (hence delay of 1 cycle)
        //
        if (Cycles.stepper.isEmpty()) {
            if (disk()?.phase != drivePhase) {
                logAsmTrace("@@ updatePhase=${disk()?.phase}->${drivePhase}")
            }

            disk()?.phase = drivePhase
            if (direction != 0) {
                logDisk("     delta: $direction newTrack: $currentPhase")
                UiState.diskStates[if (drive1) 0 else 1].currentPhase.value = currentPhase / 4
            }
        } else {
            disk()?.phase = drivePhase
        }

        if (TRACE_ON)
            logAsmTrace("@@ direction=$direction phase=${drivePhase} magnetStates=$magnetStates")
    }

    fun currentTrackString() : String {
        val trackInt = (drivePhasePrecise / 2).toInt()
        val trackFrac = ((drivePhasePrecise / 2 - trackInt) * 100).toInt()
        return String.format("%02x.%02d", trackInt, trackFrac)
    }

    private fun handle(i: Int, value: Int): Int? {
        val a = i - slot16
        val result = when(a) {
            in (0xc080..0xc087) -> {
                if (motor.isOn) {
                    // Seek address: $b9a0
                    val (phase, state) = when (a) {
                        0xc080 -> 0 to false
                        0xc081 -> 0 to true
                        0xc082 -> 1 to false
                        0xc083 -> 1 to true
                        0xc084 -> 2 to false
                        0xc085 -> 2 to true
                        0xc086 -> 3 to false
                        0xc087 -> 3 to true
                        else -> ERROR("SHOULD NEVER HAPPEN")
                    }

                    updateStepper(a)
//                    disk()?.let { magnet(it, phase, state) }

//                    magnet2(disk()!!, phase, state)
//                    val oldPhase = phaseBits.int()
//                    phaseBits[phase] = if (state) 1 else 0
//                    val newPhase = phaseBits.int()
//                    val delta = transitions[oldPhase][newPhase]
//                    println("@@@ Transitioning from $oldPhase to $newPhase, delta: $delta")
                }
                value
            }
            0xc088 -> {
                logTraceDisk("Turning motor off")
                motor.turn(false)
                value
            }
            0xc089 -> {
                logTraceDisk("Turning motor on")
                motor.turn(true)
                value
            }
            0xc08a -> {
                logTraceDisk("Turning on drive 1")
                drive1 = true
                value
            }
            0xc08b -> {
                logTraceDisk("Turning on drive 2")
                drive1 = false
                value
            }
            0xc08c -> {
//                if (!useLss) {
//                    disk()?.let { disk ->
//                        latch = disk.nextByte()
//                    }
//                }
                q6 = false
                val result = latch
                if (result.and(0x80) > 0) {
//                    println("Nibble: " + result.h())
                    addressState.readByte(result, if (drive1) 0 else 1)
                    if (NIBBLE_STRATEGY != NibbleStrategy.BITS) {
                        latch = 0 // latch.and(0x7f)
                    }
                }
//                if (latch.and(0x80) != 0) latch = 0//latch.and(0x7f) // clear bit 7
                result
            }
            0xc08d -> {
                // Reset the LSS and clear the latch for the E7 protection to work.
//                latch = 0
//                nextQa = 0
//                lss.reset()
                q6 = true
                value
            }
            0xc08e -> {
                q7 = false
                value
            }
            0xc08f -> {
                q7 = true
                value
            }
            else -> value
        }

        return result
    }

    private var magnets = BooleanArray(4) { false }
//    var phase = 0

    private var stepperMotorPhase = 0
    var currentPhase = 0
    private val phaseDeltas = listOf(
            listOf(0, 1, 2, -1),
            listOf(-1, 0, 1, 2),
            listOf(-2, -1, 0, 1),
            listOf(1, -2, -1, 0)
    )

//    private fun magnet(disk: IDisk, phase: Int, on: Boolean) {
//        logDisk("*** phase($phase, $on)")
//        if (on) {
//            val delta = phaseDeltas[stepperMotorPhase][phase]
//            val oldTrack = currentPhase
//            currentPhase += delta
//            stepperMotorPhase = phase
//            if (currentPhase < 0) currentPhase = 0
//            if (currentPhase >= IDisk.PHASE_MAX) currentPhase = IDisk.PHASE_MAX - 1
//            disk.phase = currentPhase
//            if (oldTrack != currentPhase) {
//                logDisk("     delta: $delta newTrack: $currentPhase")
//                UiState.diskStates[if (drive1) 0 else 1].currentPhase.value = currentPhase / 2
//            }
//        }
//    }


//    private fun magnet2(disk: IDisk, index: Int, state: Boolean) {
//        fun logInc(p1: Int, p2: Int) { logTraceDisk("Phase $p1 -> $p2: Incrementing track")}
//        fun logDec(p1: Int, p2: Int) { logTraceDisk("Phase $p1 -> $p2: Decrementing track")}
//        if (state) {
//            when(phase) {
//                0 -> {
//                    if (index == 1) {
//                        phase = 1
//                        logInc(0, 1)
//                        disk.incPhase()
//                    } else if (index == 3) {
//                        phase = 3
//                        logDec(0, 3)
//                        disk.decPhase()
//                    }
//                }
//                1 -> {
//                    if (index == 2) {
//                        phase = 2
//                        logInc(1, 2)
//                        disk.incPhase()
//                    } else if (index == 0) {
//                        phase = 0
//                        logDec(1, 0)
//                        disk.decPhase()
//                    }
//                }
//                2 -> {
//                    if (index == 3) {
//                        phase = 3
//                        logInc(2, 3)
//                        disk.incPhase()
//                    } else if (index == 1) {
//                        phase = 1
//                        logDec(2, 1)
//                        disk.decPhase()
//                    }
//                }
//                3 -> {
//                    if (index == 0) {
//                        phase = 0
//                        logInc(3, 4)
//                        disk.incPhase()
//                    } else if (index == 2) {
//                        phase = 2
//                        logDec(3, 2)
//                        disk.decPhase()
//                    }
//                }
//            }
//        }
//
//        magnets[index] = state
//    }
}

