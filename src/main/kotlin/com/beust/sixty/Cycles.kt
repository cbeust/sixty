package com.beust.sixty

/**
 * Actions that need to take place at a certain number of cycles.
 */
class CycleAction(var wait: Long, val run: () -> Unit)

object Cycles {
    /** Stepper actions */
    val stepper = arrayListOf<CycleAction>()

    /** Turning the motor off */
    val motorOff = arrayListOf<CycleAction>()

    fun reset(traceCycles: Long) { cycles = traceCycles }
    var cycles = 0L
        set(v) {
            val difference = v - field

            // Go through our actions and find out if one is due. If it is, run it, then remove it
            listOf(stepper, motorOff).forEach { list ->
                list.firstOrNull()?.let { ca ->
                    ca.wait -= difference
                    if (ca.wait <= 0) {
                        ca.run()
                        list.removeAt(0)
                    }
                }
            }

            field = v
        }
}