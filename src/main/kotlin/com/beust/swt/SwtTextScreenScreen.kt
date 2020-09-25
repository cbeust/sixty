package com.beust.swt

import com.beust.app.swing.ITextScreen

class SwtTextScreenScreen: ITextScreen {
    override fun drawCharacter(x: Int, y: Int, value: Int) {
        println("Drawing character $x,$y: " + (value - 0x80).toChar())
    }

}