package com.beust.app

import com.beust.sixty.Computer
import com.beust.sixty.MemoryInterceptor

class Apple2MemoryInterceptor: MemoryInterceptor {
    lateinit override var computer: Computer
    val disk = WozDisk(Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz").openStream())

    override fun onRead(location: Int, value: Int): MemoryInterceptor.Response {
        val result = when (location) {
            in StepperMotor.RANGE -> StepperMotor.onRead(location, value, disk)
            in SoftDisk.RANGE -> SoftDisk.onRead(location, value, disk)
            in SoftSwitches.RANGE -> SoftSwitches.onRead(computer, location, value)
            else -> value
        }
        return MemoryInterceptor.Response(true, result)
    }

    override fun onWrite(location: Int, value: Int): MemoryInterceptor.Response {
        var result = MemoryInterceptor.Response(true, value)

        if (location in SoftSwitches.RANGE) {
            result = SoftSwitches.onWrite(location, value)
        }
        return result
    }
}

