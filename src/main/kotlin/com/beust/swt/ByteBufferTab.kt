package com.beust.swt

import com.beust.app.IDisk
import com.beust.app.UiState
import com.beust.app.WozDisk
import com.beust.sixty.h
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label
import java.io.File

class ByteBufferTab(parent: Composite) : Composite(parent, SWT.NONE) {
    private var disk: IDisk? = UiState.currentDisk.value
    private var currentTrack: Int = 0
    private val rowSize = 16

    init {
//        background = display.getSystemColor(SWT.COLOR_BLUE)
        // Create a child composite to hold the controls

        layout = GridLayout(rowSize + 1, true)
        UiState.currentDisk.addListener { _, new ->
            disk = new
            updateBuffer()
        }
        updateBuffer()
    }

    private fun updateBuffer() {
        disk?.let { d ->
            repeat(40) { row ->
                label(this, "\$" + String.format("%04X", row * 16))
                repeat(rowSize) {
                    label(this, d.nextByte().h())
                }
            }
        }
    }
}
