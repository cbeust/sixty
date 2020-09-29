package com.beust.app.app

import com.beust.app.LineCalculator

interface ITextScreen {
    companion object {
        const val WIDTH = 40
        const val HEIGHT = 24
    }

    fun drawCharacter(x: Int, y: Int, value: Int)
}

class TextPanel(private val start: Int, private val screen: ITextScreen) {
    private val calculator = LineCalculator(start)

    fun drawMemoryLocation(location: Int, value: Int) {
        calculator.coordinatesFor(location)?.let { (x, y) ->
            screen.drawCharacter(x, y, value)
        }
    }

}
