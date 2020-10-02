package com.beust.swt

import com.beust.app.UiState
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite

class DebuggerWindow(parent: Composite): Composite(parent, SWT.NONE) {
    private val debugCheckBox: Button

    init {
        layout = GridLayout()
        debugCheckBox = Button(this, SWT.CHECK)
        debugCheckBox.apply {
            text = "Debug"
            addSelectionListener(object: SelectionAdapter() {
                override fun widgetSelected(e: SelectionEvent?) {
                    UiState.debug.value = debugCheckBox.selection
                }
            })
        }
    }
}