package com.beust.swt

import com.beust.app.GraphicContext
import com.beust.app.LineCalculator
import com.beust.app.app.ITextScreen
import com.beust.sixty.h
import com.beust.sixty.logText
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label

class TextWindow(parent: Composite, start: Int): Composite(parent, SWT.NONE), ITextScreen {

    private val labels = arrayListOf<Label>()
    private val calculator = LineCalculator(start)

    fun drawMemoryLocation(location: Int, value: Int) {
        calculator.coordinatesFor(location)?.let { (x, y) ->
            drawCharacter(x, y, value)
        }
    }

    init {
        layout = GridLayout(40, true).apply {
            horizontalSpacing = 0
            verticalSpacing = 0
        }
        clear()
    }

    fun clear() {
        display.syncExec {
            if (labels.isEmpty()) {
                background = black(display)
                repeat(ITextScreen.HEIGHT) { y ->
                    repeat(ITextScreen.WIDTH) { x ->
                        labels.add(Label(this, SWT.NONE).apply {
                            font = GraphicContext.textFont
                            background = display.getSystemColor(SWT.COLOR_BLACK)
                            foreground = display.getSystemColor(SWT.COLOR_GREEN)
                        })
                    }
                }
            }
            labels.forEach {
                it.text = 0xa0.toChar().toString() // (x % 10).toString()
            }
        }
    }

    /**
     * Reference:  https://en.wikipedia.org/wiki/Apple_II_character_set
     */
    override fun drawCharacter(x: Int, y: Int, value: Int) {
        if (display != null) {
            display.asyncExec {
//                GC(this).let { gc ->
//                    gc.background = red(display)
//                    gc.drawString("FOO", x * WIDTH_FACTOR, y)
//                }
                labels[y * ITextScreen.WIDTH + x].let { label ->
                    if (!label.isDisposed) {
                        when(value) {
                            in 0x01..0x3f -> { // inverse
                                label.background = green(display)
                                label.foreground = black(display)
                            }
//                            in 0x40..0x7f -> { // flashing
//
//                            }
                            else -> {
                                label.background = black(display)
                                label.foreground = green(display)
                            }
                        }

                        val c = when(value) {
                            in 0x01..0x1a -> {
                                value + 0x40
                            }
                            else -> value.and(0x7f)
                        }
                        if (value != 0xff && value != 0xa0) {
                            logText("Drawing ${value.h()} ${c.h()} " + c.toChar().toString())
                        }
                        label.text = c.toChar().toString()
                    }
                }
            }
        }
    }
}