package com.beust.app.swing

import com.beust.app.LineCalculator

interface IGraphicTextPanel {
    fun drawCharacter(x: Int, y: Int, value: Int)
}

class TextPanel(private val graphicPanel: IGraphicTextPanel) {
    private val calculator = LineCalculator()

    fun drawMemoryLocation(location: Int, value: Int) {
        calculator.coordinatesFor(location)?.let { (x, y) ->
            graphicPanel.drawCharacter(x, y, value)
        }
    }

}
