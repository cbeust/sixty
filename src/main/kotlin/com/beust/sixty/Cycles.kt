package com.beust.sixty

/**
 * Actions that need to take place at a certain number of cycles.
 */
class CycleAction(var wait: Long, val run: () -> Unit)

object Cycles {
    /** Stepper actions */
    val stepper = arrayListOf<CycleAction>()

    // TODO: motor off action should go in here too instead of a separate thread

    fun reset(traceCycles: Long) { cycles = traceCycles }
    var cycles = 0L
        set(v) {
            val difference = v - field

            // Go through our actions and find out if one is due. If it is, run it, then remove it
            stepper.firstOrNull()?.let { ca ->
                ca.wait -= difference
                if (ca.wait <= 0) {
                    ca.run()
                    stepper.removeAt(0)
                }
            }

            field = v
        }
}