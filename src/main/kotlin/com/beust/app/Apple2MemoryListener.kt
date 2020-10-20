package com.beust.app

import com.beust.sixty.Apple2Memory
import com.beust.sixty.MemoryListener
import com.beust.swt.HiResWindow
import com.beust.swt.TextWindow

class Apple2MemoryListener(private val memory: () -> Apple2Memory,
        private val textPanel: TextWindow, private val hiresScreen: HiResWindow): MemoryListener() {

    override fun isInRange(address: Int): Boolean = address in 0x400..0x800 || address in 0x2000..0x6000

    override fun onWrite(location: Int, value: Int) {
        when (location) {
            in 0x400..0x7ff -> {
                textPanel.drawMemoryLocation(location, value)
            }
            in 0x2000..0x4000 -> {
                hiresScreen.drawMemoryLocation(memory(), location, 0)
            }
            in 0x4000..0x6000 -> {
                hiresScreen.drawMemoryLocation(memory(), location, 1)
            }
        }
    }
}
