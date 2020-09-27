package com.beust.swt

import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.widgets.Composite


class HiResWindow(parent: Composite, style: Int = SWT.NONE): Composite(parent, style) {
    init {
        background = lightBlue(display)
//        layoutData = GridData(GridData.FILL, GridData.FILL).apply {
//            widthHint = 280
//            heightHint = 102
//        }
        addPaintListener { e ->
            val clientArea: Rectangle = shell.clientArea
            e.gc.drawLine(0, 0, clientArea.width, clientArea.height)
        }
    }
}