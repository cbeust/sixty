package com.beust.swt

import com.beust.app.ByteAlgorithm
import com.beust.app.SixAndTwo
import com.beust.app.UiState
import com.beust.app.word
import com.beust.sixty.h
import com.beust.sixty.hh
import org.eclipse.swt.SWT
import org.eclipse.swt.SWT.NONE
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*

fun main(args: Array<String>) {
    val display = Display()
    val shell = Shell(display).apply {
        text = "Nibble viewer"
        layout = GridLayout()
    }

    DiskWindow(shell)
    shell.bounds = Rectangle(0, 0, 1000, 800)

//    shell.pack()
    shell.open()
    while (!shell.isDisposed) {
        if (!display.readAndDispatch()) display.sleep()
    }
}

class DiskWindow(parent: Composite): Composite(parent, NONE) {
    private lateinit var diskLabel: Label
//    private val scrolledComposite: ScrolledComposite
    private var byteText: Text? = null
    private var wordText: Text? = null
    private var fourAndFourText: Text? = null
    private val byteBufferWindow: ByteBufferWindow

    init {
        layout = GridLayout(3, false)

        createHeader(this)
        createRightSide(this)
        byteBufferWindow = createScrollableByteBuffer(this)

        UiState.currentDisk1File.addListener { _, new ->
            display.asyncExec {
                diskLabel.text = new?.name
            }
        }
        UiState.currentBytes.addAfterListener { _, _ -> updateInspector() }
    }

    private fun updateInspector() {
        val bytes = UiState.currentBytes.value
        if (bytes.isNotEmpty()) display.asyncExec {
            byteText?.text = bytes[0].h()
            wordText?.text = word(bytes[0], bytes[1]).hh()
            fourAndFourText?.text = SixAndTwo.pair4And4(bytes[0], bytes[1]).h()
        }

    }
    private fun createLabelText(parent: Composite, label: String): Pair<Composite, Text> {
        val result = Composite(parent, NONE).apply {
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

    private fun createRightSide(parent: Composite): Composite {
        return Composite(parent, SWT.NONE).apply {
            layout = FillLayout(SWT.VERTICAL)
            layoutData = GridData(SWT.FILL, SWT.FILL, true, true).apply {
                widthHint = 400
                verticalSpan = 2
            }
            createTrackInfo(this)
            createSectorInfo(this)
            createDataInspector(this)
        }
    }

    private fun textToHex(s: String): List<Int> {
        val allowed = hashSetOf('a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F')
        fun isHex(c: Char) = Character.isDigit(c) || allowed.contains(c)

        val result = arrayListOf<Int>()
        var current = 0
        var first = true
        s.forEach { c ->
            if (isHex(c)) {
                val value = Integer.parseInt(c.toString(), 16)
                if (! first) {
                    current = current.shl(4).or(value)
                    result.add(current)
                    current = 0
                } else {
                    current = value
                }
                first = ! first
            }
        }
        return result
    }

    private fun createTrackInfo(parent: Composite): Composite {
        val result = Group(parent, NONE).apply {
            layout = GridLayout()
            text = "Track info"
        }
        createLabelText(result, "Address prologue").second.apply {
            text = "D5 AA 96"
            addListener(SWT.FocusOut) { e ->
                UiState.addressPrologue.value = textToHex(text)
            }
        }
        createLabelText(result, "Address epilogue").second.apply {
            text = "DE AA"
            addListener(SWT.FocusOut) { e ->
                UiState.addressEpilogue.value = textToHex(text)
            }
        }
        createLabelText(result, "Data prologue").second.apply {
            text = "D5 AA AD"
            addListener(SWT.FocusOut) { e ->
                UiState.dataPrologue.value = textToHex(text)
            }
        }
        createLabelText(result, "Data epilogue").second.apply {
            text = "DE AA"
            addListener(SWT.FocusOut) { e ->
                UiState.dataEpilogue.value = textToHex(text)
            }
        }

        return result
    }

    private fun createSectorInfo(parent: Composite): Composite {
        val result = Group(parent, NONE).apply {
            layout = GridLayout()
            text = "Sector info"
        }
        createLabelText(result, "Volume").second.apply {
            UiState.caretSectorInfo.addAfterListener { _, new ->
                text = new?.volume?.h() ?: ""
            }
        }
        createLabelText(result, "Track").second.apply {
            UiState.caretSectorInfo.addAfterListener { _, new ->
                text = new?.track?.h() ?: ""
            }
        }
        createLabelText(result, "Sector").second.apply {
            UiState.caretSectorInfo.addAfterListener { _, new ->
                text = new?.sector?.h() ?: ""
            }
        }
        createLabelText(result, "Checksum").second.apply {
            UiState.caretSectorInfo.addAfterListener { _, new ->
                text = new?.checksum?.h() ?: ""
            }
        }

        return result
    }

    private fun createDataInspector(parent: Composite): Composite {
        val result = Group(parent, NONE).apply {
            layout = GridLayout()
            text = "Data inspector"
        }
        byteText = createLabelText(result, "Byte").second
        wordText = createLabelText(result, "Word").second
        fourAndFourText = createLabelText(result, "4-and-4").second

        return result
    }

    private fun createHeader(parent: Composite): Composite {
        val header = Composite(parent, NONE).apply {
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
        diskLabel = label(header, UiState.currentDisk1File.value?.name ?: "<none>").apply {
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
        val b = button(header, "...").apply {
            layoutData = GridData(SWT.BEGINNING, SWT.CENTER, false, false)
        }
        fileDialog(shell, b, UiState.currentDisk1File)

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

    private fun createScrollableByteBuffer(parent: Composite): ByteBufferWindow {
        val scrolled = ScrolledComposite(parent, SWT.BORDER or SWT.V_SCROLL or SWT.H_SCROLL).apply {
            background = black(display)

            layoutData = GridData().apply {
                heightHint = 600
                setMinSize(Point(500, 10000))
                expandHorizontal = true
                expandVertical = true
            }
        }

        val result = ByteBufferWindow(scrolled).apply {
            layoutData = GridData(GridData.FILL, GridData.FILL, true, true).apply {
                //                horizontalAlignment = GridData.FILL
                //                verticalAlignment = GridData.FILL
                heightHint = 1000
                horizontalSpan = 2
                //                grabExcessVerticalSpace = true
                //                grabExcessHorizontalSpace = false
            }

        }
        scrolled.content = result

        return result
    }

}

