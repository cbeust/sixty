package com.beust.app

import com.beust.sixty.Computer
import com.beust.sixty.PcListener
import com.beust.sixty.h

class Apple2PcListener: PcListener {
    var computer: Computer? = null

    override fun onPcChanged(c: Computer) {
        val memory = c.memory
        val newValue = c.cpu.PC
        when(newValue) {
        }
    }
}