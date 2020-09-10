package com.beust.app

import com.beust.sixty.MemoryInterceptor
import com.beust.sixty.hh

object SoftSwitches {
    val RANGE = 0xc000..0xc0ff

    fun onRead(location: Int, value: Int): MemoryInterceptor.Response {
        var result = 0xd
        when(location) {
            0xC000 -> {
                DEBUG = true
                result = 0xc1
            } // KBD/CLR80STORE
            0xC001 -> {} // SET80STORE
            0xC006 -> {} // SETSLOTCXROM
            0xc007 -> {} // SETINTCXROM
            0xC00C -> {} // CLR80COL
            0xC00E -> {} // CLRALTCHAR
            0xC010 -> {} // KBDSTRB
            0xC015 -> {} // RDCXROM
            0xC018 -> { result = 0x8d } // RD80STORE
            0xC01C -> {} // RDPAGE2
            0xC054 -> {} // LOWSCR
            0xC051 -> {} // TXTSET
            0xC055 -> {} // HISCR
            0xC056 -> {} // LORES
            else -> {
                println("Unknown soft switch: " + location.hh())
            }
        }
        return MemoryInterceptor.Response(true, result)
    }

    fun onWrite(location: Int, value: Int): MemoryInterceptor.Response {
        when(location) {
            0xC000 -> {} // KBD/CLR80STORE
            0xC001 -> {} // SET80STORE
            0xC006 -> {} // SETSLOTCXROM
            0xc007 -> {} // SETINTCXROM
            0xC00C -> {} // CLR80COL
            0xC00E -> {} // CLRALTCHAR
            0xC010 -> {} // KBDSTRB
            0xC054 -> {} // LOWSCR
            0xC055 -> {} // HISCR
            else -> {
                TODO("Unknown soft switch: " + location.hh())
            }
        }
        return MemoryInterceptor.Response(false, value)
    }
}