package com.beust.swt

import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.FormLayout
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.layout.RowLayout
import org.eclipse.swt.widgets.*
import java.awt.FlowLayout

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
        val child = this // Composite(this, SWT.NONE)
        child.layout = GridLayout(17, true)

        val rowSize = 16
        repeat(80) { row ->
            label(child, "\$" + String.format("%04X", row * 16))
            repeat(rowSize) {
                label(child, "FF")
            }
        }
        setSize(300, 300)
//        setMinSize(500, 200)
        pack()
    }

}
