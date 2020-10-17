package com.beust.swt

import com.beust.app.UiState
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite

class DebuggerWindow(parent: Composite): Composite(parent, SWT.NONE) {
    init {
        layout = GridLayout(1, false)
        Button(this, SWT.CHECK).apply {
            text = "Debug"
            addSelectionListener(object: SelectionAdapter() {
                override fun widgetSelected(e: SelectionEvent?) {
                    UiState.debug.value = selection
                }
            })
        }
//        val speedMhz = label(this, "<speed in Mhz>", SWT.BORDER).apply {
//            font = font(shell, "Arial", 15, SWT.BOLD)
//        }
//        UiState.speedMegahertz.addAfterListener { _, new ->
//            display.asyncExec {
//                speedMhz.text = String.format("%2.2f Mhz", new)
//            }
//        }
    }
}