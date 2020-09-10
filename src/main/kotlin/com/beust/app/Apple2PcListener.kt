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
            0xc28b -> {
                println("Waiting for key")
            }
//                0xc67d -> {
//                    println("Decoding data")
//                }
//                0xc699 -> {
//                    println("Testing carry")
//                }
//                0xc6a6 -> {
//                    println("Decoding data sector $" + memory[0x3d].h() + " into " + c.word(address = 0x26).hh())
//                    ""
//                }
//                0x822 -> {
//                    println("BMI for X: " + c.cpu.X)
//                }
//                0x829 -> {
//                    println("Decrementing $8ff: " + memory[0x8ff].hh())
//                }
//                0x836 -> {
//                    println("Reading next sector: "+ memory[0x3d].h())
//                    ""
//                }
//                0x839 -> {
//                    println("Next part of stage 2")
//                }
//                0xc6ed -> {
//                    println("Incrementing $3d: " + memory[0x3d].h())
//                }
            0xaca3 -> {
                println("Storing potentially in $36 [$42]=" + memory[0x42].h() + memory[0x43].h())
                ""
            }
            0x9fc5 -> {
                // TODO
                memory[0x36] = 0xf0
                memory[0x37] = 0xfd
            }
            0x9eba -> {
                // TODO
                memory[0x38] = 0x1b
                memory[0x39] = 0xfd

            }
        }
    }
}