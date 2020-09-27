package com.beust.app

import com.beust.app.app.TextPanel
import com.beust.sixty.IMemory
import com.beust.sixty.MemoryListener
import com.beust.swt.HiResWindow

class Apple2MemoryListener(private val textPanel: TextPanel, private val hiresScreen: HiResWindow):
        MemoryListener() {
    override fun isInRange(address: Int): Boolean {
        return address in 0x400..0x800 || address in 0x2000..0x6000
    }

    override fun onWrite(memory: IMemory, location: Int, value: Int) {
        if (location in 0x400..0x7ff) {
            textPanel.drawMemoryLocation(location, value)
//            logLines.add("Writing on the text screen: $" + location.hh() + "=$" + value.h())
        } else if (location in 0x2000..0x4000) {
            hiresScreen.drawMemoryLocation(memory, location)
//            logLines.add("Writing in graphics")
        }
    }
}
