package com.beust.app

import org.eclipse.jface.window.ApplicationWindow
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label

fun main() {
    SwtWindow()
}

class SwtWindow : ApplicationWindow(null) {
    override fun createContents(parent: Composite?): Control {
        // Create a Hello, World label
        val label = Label(parent, SWT.CENTER)
        label.setText("www.java2s.com")
        return label
    }

    init {
        // Don't return from open() until window closes
        setBlockOnOpen(true)

        // Open the main window
        open()

        // Dispose the display
        Display.getCurrent().dispose()
    }
}

