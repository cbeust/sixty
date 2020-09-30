package com.beust.swt

import com.beust.app.*
import com.beust.sixty.h
import com.beust.sixty.hh
import org.eclipse.swt.SWT
import org.eclipse.swt.SWT.NONE
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.layout.*
import org.eclipse.swt.widgets.*
import java.io.File

class DiskWindow(parent: Composite, parentHeight: Int): Composite(parent, NONE) {
    private lateinit var diskLabel: Label
    private val scrolledComposite: ScrolledComposite
    private var byteText: Text? = null
    private var wordText: Text? = null
    private var fourAndFourText: Text? = null

    init {
        layout = GridLayout(3, false)

        val header = createHeader(this)
        val dataInspector = createDataInspector(this)

        scrolledComposite = createScrollableByteBuffer(this).apply {
            layoutData = GridData(GridData.FILL, GridData.FILL, true, true).apply {
//                horizontalAlignment = GridData.FILL
//                verticalAlignment = GridData.FILL
                horizontalSpan = 2
                grabExcessVerticalSpace = true
                grabExcessHorizontalSpace = false
            }
        }

        UiState.currentDiskFile.addListener { _, new -> diskLabel.text = new?.name }
        UiState.currentBytes.addListener { _, _ -> updateInspector() }
    }

    private fun updateInspector() {
        val bytes = UiState.currentBytes.value
        display.asyncExec {
            byteText?.text = bytes[0].h()
            wordText?.text = word(bytes[0], bytes[1]).hh()
            fourAndFourText?.text = SixAndTwo.pair4And4(bytes[0], bytes[1]).h()
        }

    }
    private fun createLabelText(parent: Composite, label: String): Pair<Composite, Text> {
        val result = Composite(parent, SWT.NONE).apply {
            layoutData = GridData(GridData.FILL, GridData.FILL, true, false)
            layout = GridLayout(2, false)
        }
        label(result, label, SWT.RIGHT).apply {
            layoutData = GridData().apply {
                widthHint = 100
            }
        }
        val text = Text(result, SWT.BORDER).apply {
            layoutData = GridData().apply {
                widthHint = 100
            }
        }
        return result to text
    }

    private fun createDataInspector(parent: Composite): Composite {
        val result = Group(parent, NONE).apply {
            layout = GridLayout(1, false)
            text = "Data inspector"
            layoutData = GridData(SWT.FILL, SWT.FILL, true, true).apply {
                widthHint = 400
                verticalSpan = 2
            }
        }
        byteText = createLabelText(result, "Byte").second
        wordText = createLabelText(result, "Word").second
        fourAndFourText = createLabelText(result, "4-and-4").second

        return result
    }

    private fun createHeader(parent: Composite): Composite {
        val header = Composite(this, NONE).apply {
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
            addListener(SWT.Selection) { e ->
                println("Button pressed!")
                val disk = FileDialog(shell).apply {
                    text = "Pick a disk file"
                    filterExtensions = arrayOf("*.dsk;*.woz")
                    filterPath = "d:\\pd\\Apple disks"
                }.open()
                if (disk != null) {
                    UiState.currentDiskFile.value = File(disk)
                }
            }
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
        // Byte algorithm dropdown
        //
        Combo(header, SWT.DROP_DOWN or SWT.READ_ONLY).apply {
            layoutData = GridData(SWT.BEGINNING, SWT.CENTER, false, false)
            setItems(ByteAlgorithm.RAW.toString(), ByteAlgorithm.SHIFTED.toString())
            select(1)
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

        return header
    }
}