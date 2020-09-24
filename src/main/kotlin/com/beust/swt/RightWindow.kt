package com.beust.swt

import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.TabFolder
import org.eclipse.swt.widgets.TabItem

class RightWindow(parent: Composite): Composite(parent, SWT.NONE) {
    init {
        val tabFolder = TabFolder(parent, SWT.NONE)
        val scrolled = ScrolledComposite(tabFolder, SWT.V_SCROLL)
        val wozItem = TabItem(tabFolder, SWT.NONE).apply {
            text = "WOZ"
            control = scrolled
        }
        scrolled.content = ByteBufferTab(scrolled).apply {
            background = blue(display)
        }
//        pack()
    }
}