package com.beust.swt

import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.TabFolder
import org.eclipse.swt.widgets.TabItem

class RightWindow(parent: Composite): Composite(parent, SWT.NONE) {
    init {
        val tabFolder = TabFolder(parent, SWT.NONE)
        val wozItem = TabItem(tabFolder, SWT.NONE).apply {
            text = "WOZ"
            control = Label(tabFolder, SWT.NONE).apply {
                text = "Content of the tab"
            }
        }
//        pack()
    }
}