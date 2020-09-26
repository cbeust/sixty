package com.beust.swt

import com.beust.app.ByteAlgorithm
import com.beust.app.UiState
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.layout.*
import org.eclipse.swt.widgets.Combo
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label

class RightWindow(parent: Composite, parentHeight: Int): Composite(parent, SWT.NONE) {
    private val diskLabel: Label
    val scrolledComposite: ScrolledComposite

    init {
        layout = GridLayout(3, false)

        val header = Composite(this, SWT.NONE).apply {
            layout = GridLayout(6, false)
//            background = grey(display)
            layoutData = GridData().apply {
//                background = grey(display)
                horizontalSpan = 2
                horizontalAlignment = GridData.FILL
                grabExcessHorizontalSpace = true
            }
        }

        //
        // Name of the disk
        //
        diskLabel = label(header, UiState.currentDiskFile.value?.name ?: "<none>").apply {
            layoutData = GridData(SWT.BEGINNING, SWT.CENTER, false, false)
////                background = grey(display)
////                horizontalSpan = 2
//                horizontalAlignment = GridData.FILL
//                grabExcessHorizontalSpace = true
//            }
        }

        //
        // Button to open a different disk
        //
        button(header, "...").apply {
            layoutData = GridData(SWT.BEGINNING, SWT.CENTER, false, false)
        }

        //
        // Track number label
        //
        label(header, "Track number:").apply {
            layoutData = GridData(SWT.BEGINNING, SWT.CENTER, false, false)
        }

        //
        // Track number drop down
        //
        Combo(header, SWT.DROP_DOWN or SWT.READ_ONLY).apply {
            layoutData = GridData(SWT.BEGINNING, SWT.CENTER, false, false)
            val tracks = arrayListOf<String>()
            repeat(160) {
                tracks.add((it.toFloat() / 4).toString())
            }
            setItems(*tracks.toTypedArray())
            select(0)
            addSelectionListener(object: SelectionListener {
                override fun widgetSelected(e: SelectionEvent) {
                    val track = (e.widget as Combo).selectionIndex
                    UiState.currentTrack.value = track
                }

                override fun widgetDefaultSelected(e: SelectionEvent?) {}
            })
        }

        //
        // Byte algorithm label
        //
        label(header, "Bytes displayed").apply {
            layoutData = GridData(SWT.BEGINNING, SWT.CENTER, false, false)
        }

        //
        // Track number drop down
        //
        Combo(header, SWT.DROP_DOWN or SWT.READ_ONLY).apply {
            layoutData = GridData(SWT.BEGINNING, SWT.CENTER, false, false)
            setItems(ByteAlgorithm.RAW.toString(), ByteAlgorithm.SHIFTED.toString())
            select(0)
            addSelectionListener(object: SelectionListener {
                override fun widgetSelected(e: SelectionEvent) {
                    val newAlgo = when((e.widget as Combo).selectionIndex) {
                        0 -> ByteAlgorithm.RAW
                        else -> ByteAlgorithm.SHIFTED
                    }
                    UiState.byteAlgorithn.value = newAlgo
                }

                override fun widgetDefaultSelected(e: SelectionEvent?) {}
            })
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