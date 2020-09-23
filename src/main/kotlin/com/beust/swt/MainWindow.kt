package com.beust.swt

import org.eclipse.jface.resource.FontDescriptor
import org.eclipse.jface.resource.JFaceResources
import org.eclipse.jface.resource.LocalResourceManager
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label

class MainWindow(parent: Composite): Composite(parent, SWT.NONE) {
    private val resManager = LocalResourceManager(JFaceResources.getResources(), shell)
    private val textFont: Font = resManager.createFont(FontDescriptor.createFrom("Arial", 10, SWT.BOLD))

    init {
        layout = GridLayout(40, true)
        background = display.getSystemColor(SWT.COLOR_BLACK)
        repeat(40) {
            repeat(24) {
                Label(this, SWT.NONE).apply {
                    font = textFont
                    background = display.getSystemColor(SWT.COLOR_BLACK)
                    foreground = display.getSystemColor(SWT.COLOR_GREEN)
                    text = "@"
                }
            }
        }
        pack()
    }
}