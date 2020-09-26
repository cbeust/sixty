package com.beust.swt

import com.beust.app.UiState
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.layout.*
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label

class RightWindow(parent: Composite, parentHeight: Int): Composite(parent, SWT.NONE) {
    private val diskLabel: Label
    val scrolledComposite: ScrolledComposite

    init {
        layout = GridLayout(3, false)

        val header = Composite(this, SWT.NONE).apply {
            layout = GridLayout(2, false)
            background = grey(display)
            layoutData = GridData().apply {
//                background = grey(display)
                horizontalSpan = 2
                horizontalAlignment = GridData.FILL
                grabExcessHorizontalSpace = true
            }
        }

        diskLabel = label(header, UiState.currentDiskFile.value?.name ?: "<none>").apply {
            layoutData = GridData(SWT.BEGINNING, SWT.CENTER, false, false)
////                background = grey(display)
////                horizontalSpan = 2
//                horizontalAlignment = GridData.FILL
//                grabExcessHorizontalSpace = true
//            }
        }
        button(header, "...").apply {
            layoutData = GridData(SWT.BEGINNING, SWT.CENTER, false, false)
        }

        scrolledComposite = createScrollableByteBuffer(this).apply {
            layoutData = GridData(GridData.FILL, GridData.FILL, true, true).apply {
//                horizontalAlignment = GridData.FILL
//                verticalAlignment = GridData.FILL
                horizontalSpan = 3
                grabExcessVerticalSpace = true
                grabExcessHorizontalSpace = true
            }
        }

        UiState.currentDiskFile.addListener { _, new -> diskLabel.text = new?.name }
    }
}