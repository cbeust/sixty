package com.beust.swt

import org.eclipse.swt.SWT
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.TabItem

class ByteBufferTab(parent: Composite) : Composite(parent, SWT.NONE) {
    init {
        layout = FillLayout()
        Label(this, SWT.NONE).apply {
            text = "This is the byte buffer tab"
        }
        pack()
    }

}
