package com.beust.app.swing

import com.beust.app.LineCalculator

interface ITextScreen {
    fun drawCharacter(x: Int, y: Int, value: Int)
}

class TextPanel(private val screen: ITextScreen) {
    private val calculator = LineCalculator()

    fun drawMemoryLocation(location: Int, value: Int) {
        calculator.coordinatesFor(location)?.let { (x, y) ->
            screen.drawCharacter(x, y, value)
        }
    }

}
