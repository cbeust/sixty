package com.beust.swt

import com.beust.sixty.h
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label

fun label(parent: Composite, t: String) = Label(parent, SWT.NONE).apply { text = t }
fun button(parent: Composite, t: String) = Button(parent, SWT.NONE).apply { text = t }
fun red(d: Display) = d.getSystemColor(SWT.COLOR_RED)
fun white(d: Display) = d.getSystemColor(SWT.COLOR_WHITE)
fun black(d: Display) = d.getSystemColor(SWT.COLOR_BLACK)
fun green(d: Display) = d.getSystemColor(SWT.COLOR_GREEN)
fun blue(d: Display) = d.getSystemColor(SWT.COLOR_BLUE)
fun yellow(d: Display) = d.getSystemColor(SWT.COLOR_YELLOW)

class ByteBufferTab(parent: Composite) : Composite(parent, SWT.NONE) {
    init {
//        background = display.getSystemColor(SWT.COLOR_BLUE)
        // Create a child composite to hold the controls
        val rowSize = 16

        layout = GridLayout(rowSize + 1, true)

        repeat(40) { row ->
            label(this, "\$" + String.format("%04X", row * 16))
            repeat(rowSize) {
                label(this, it.h())
            }
        }
    }

}
