package com.beust.swt

import com.beust.app.UiState
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.TabFolder
import org.eclipse.swt.widgets.TabItem

class RightWindow(parent: Composite, parentHeight: Int): Composite(parent, SWT.NONE) {
    private val diskLabel: Label

    init {
        layout = GridLayout(3, false)

        label(this, "File:").apply {
        }
        diskLabel = label(this, UiState.currentDiskFile?.value?.name ?: "<none>").apply {
            layoutData = GridData().apply {
                horizontalSpan = 2
                horizontalAlignment = GridData.FILL
                grabExcessHorizontalSpace = true
            }
        }
        createScrollableByteBuffer(this, parentHeight).apply {
            layoutData = GridData().apply {
                horizontalAlignment = GridData.FILL
                verticalAlignment = GridData.FILL
                horizontalSpan = 3
                grabExcessVerticalSpace = true
            }
        }

        UiState.currentDiskFile.addListener { _, new -> diskLabel.text = new?.name }
    }
}