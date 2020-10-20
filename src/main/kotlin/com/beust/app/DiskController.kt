package com.beust.app

import com.beust.sixty.*
import java.util.*
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
        private fun updateUi(b: Boolean) {
            UiState.diskStates[if (drive1()) 0 else 1].motor.value = b
        }

        private var status: MotorState = MotorState.OFF
            set(f) {
                if (f == MotorState.ON) {
                    updateUi(true)
                    field = MotorState.ON
                } else if (f == MotorState.OFF) {
                    if (field == MotorState.ON) {
                        // Turn off the motor after a second, unless it was turned on in the meantime
                        val task = Runnable {
                            if (field == MotorState.SPINNING_DOWN) {
                                logDisk("Turning motor OFF after a second")
                                updateUi(false)
                                field = MotorState.OFF
                            } // Motor was turned on while spinning down: not turning it off
                        }
                        Threads.scheduledThreadPool.schedule(task, 7500,TimeUnit.MILLISECONDS)
                        logDisk("Motor spinning down")
                        field = MotorState.SPINNING_DOWN
                    } // we're already OFF or SPINNING_DOWN, nothing to do
                }
            }

        fun turn(on: Boolean) {
            status = if (on) MotorState.ON else MotorState.OFF
        }
        val isOn: Boolean get() = status == MotorState.ON || status == MotorState.SPINNING_DOWN
    }

    private fun c(address: Int) = address + slot16
    override fun isInRange(address:Int) = address in (c(0xc080)..c(0xc08f))

    private val useLss = true

    fun step(): Computer.RunStatus {
        // Use the LSS
        if (useLss) {
            disk()?.let { disk ->
                lss.onPulse(q6, q7, { -> motor.isOn }, disk)
                latch = lss.latch
            }
        } else {

            // Faster way for unprotected disks
            disk()?.let {
                if (motor.isOn && latch.and(0x80) == 0) {
                    //                latch = latch.shl(1).or(it.nextBit())
                    latch = it.nextByte()
                }
            }
        }

//        if (motorOn && disk != null) {
////     More formal way: bit by bit on every pulse
//            if (latch and 0x80 == 0) {
//                repeat(8) {
//                    latch = latch.shl(1).or(disk!!.nextBit()).and(0xff)
//                }
//                while (latch and 0x80 == 0) {
//                    latch = latch.shl(1).or(disk!!.nextBit()).and(0xff)
//                }
//            }
//        }
////        println("@@@   latch: ${latch.h()}")
        return Computer.RunStatus.RUN
    }

    private var disk1: IDisk? = null
    private var disk2: IDisk? = null

    private fun disk() = if (drive1) disk1 else disk2
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
        /*9*/ listOf( 0,-1,-1,-2,-3,-3, 0,+3,+3,+2,+1,+1, 0, 0, 0, 0),
        /*8*/ listOf(+1, 0, 0,-1,-2,-2,-3, 0, 0,+3,+2,+2, 0, 0, 0, 0),
        /*D*/ listOf(+1, 0, 0,-1,-2,-2,-3, 0, 0,+3,+2,+2, 0, 0, 0, 0),
        /*C*/ listOf(+2,+1,+1, 0,-1,-1,-2,-3,-3, 0,+3,+3, 0, 0, 0, 0),
        /*4*/ listOf(+3,+2,+2,+1, 0, 0,-1,-2,-2,-3, 0, 0, 0, 0, 0, 0),
        /*E*/ listOf(+3,+2,+2,+1, 0, 0,-1,-2,-2,-3, 0, 0, 0, 0, 0, 0),
        /*6*/ listOf( 0,+3,+3,+2,+1,+1, 0,-1,-1,-2,-3,-3, 0, 0, 0, 0),
        /*2*/ listOf(-3, 0, 0,+3,+2,+2,+1, 0, 0,-1,-2,-2, 0, 0, 0, 0),
        /*7*/ listOf(-3, 0, 0,+3,+2,+2,+1, 0, 0,-1,-2,-2, 0, 0, 0, 0),
        /*3*/ listOf(-2,-3,-3, 0,+3,+3,+2,+1,+1, 0,-1,-1, 0, 0, 0, 0),
        /*1*/ listOf(-1,-2,-2,-3, 0, 0,+3,+2,+2,+1, 0, 0, 0, 0, 0, 0),
        /*B*/ listOf(-1,-2,-2,-3, 0, 0,+3,+2,+2,+1, 0, 0, 0, 0, 0, 0)
    )

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
//                    magnet2(disk()!!, phase, state)
//                    val oldPhase = phaseBits.int()
//                    phaseBits[phase] = if (state) 1 else 0
//                    val newPhase = phaseBits.int()
//                    val delta = transitions[oldPhase][newPhase]
//                    println("@@@ Transitioning from $oldPhase to $newPhase, delta: $delta")
                    disk()?.let { magnet(it, phase, state) }
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
                if (! useLss) {
                    latch = disk()!!.nextByte()
                }
                q6 = false
                val result = latch
//                if (latch.and(0x80) != 0) latch = 0//latch.and(0x7f) // clear bit 7
                result
            }
            0xc08d -> {
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
    private var phase = 0

    private var stepperMotorPhase = 0
    private var currentPhase = 0
    private val phaseDeltas = listOf(
            listOf(0, 1, 2, -1),
            listOf(-1, 0, 1, 2),
            listOf(-2, -1, 0, 1),
            listOf(1, -2, -1, 0)
    )

    private fun magnet(disk: IDisk, phase: Int, on: Boolean) {
        if (on) {
            val delta = phaseDeltas[stepperMotorPhase][phase]
            val oldTrack = currentPhase
            currentPhase += delta
            stepperMotorPhase = phase
            if (delta > 0) {
                repeat(delta) { disk.incPhase() }
            } else if (delta < 0) {
                repeat(-delta) { disk.decPhase() }
            }
            if (currentPhase < 0) currentPhase = 0
            if (currentPhase >= IDisk.PHASE_MAX) currentPhase = IDisk.PHASE_MAX - 1
            if (oldTrack != currentPhase) {
                logDisk("*** phase($phase, $on) delta: $delta newTrack: $currentPhase")
                UiState.diskStates[if (drive1) 0 else 1].currentPhase.value = currentPhase / 2
            }
        }
    }


    private fun magnet2(disk: IDisk, index: Int, state: Boolean) {
        fun logInc(p1: Int, p2: Int) { logTraceDisk("Phase $p1 -> $p2: Incrementing track")}
        fun logDec(p1: Int, p2: Int) { logTraceDisk("Phase $p1 -> $p2: Decrementing track")}
        if (state) {
            when(phase) {
                0 -> {
                    if (index == 1) {
                        phase = 1
                        logInc(0, 1)
                        disk.incPhase()
                    } else if (index == 3) {
                        phase = 3
                        logDec(0, 3)
                        disk.decPhase()
                    }
                }
                1 -> {
                    if (index == 2) {
                        phase = 2
                        logInc(1, 2)
                        disk.incPhase()
                    } else if (index == 0) {
                        phase = 0
                        logDec(1, 0)
                        disk.decPhase()
                    }
                }
                2 -> {
                    if (index == 3) {
                        phase = 3
                        logInc(2, 3)
                        disk.incPhase()
                    } else if (index == 1) {
                        phase = 1
                        logDec(2, 1)
                        disk.decPhase()
                    }
                }
                3 -> {
                    if (index == 0) {
                        phase = 0
                        logInc(3, 4)
                        disk.incPhase()
                    } else if (index == 2) {
                        phase = 2
                        logDec(3, 2)
                        disk.decPhase()
                    }
                }
            }
        }

        magnets[index] = state
    }
}