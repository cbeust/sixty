package com.beust.swt

import com.beust.app.*
import com.beust.sixty.h
import com.beust.sixty.hh
import org.eclipse.jface.text.TextViewer
import org.eclipse.swt.SWT
import org.eclipse.swt.SWT.NONE
import org.eclipse.swt.browser.Browser
import org.eclipse.swt.custom.StyleRange
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.graphics.TextLayout
import org.eclipse.swt.graphics.TextStyle
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*


fun main(args: Array<String>) {
    val display = Display()
    val shell = Shell(display).apply {
        text = "Nibble viewer"
        layout = FillLayout()
    }

//    test6(shell)
    DiskWindow(shell)
    shell.bounds = Rectangle(0, 0, 1000, 800)

//    shell.pack()
    shell.open()
    while (!shell.isDisposed) {
        if (!display.readAndDispatch()) display.sleep()
    }
}

//fun test6(parent: Composite) {
//    val row1 = listOf(0xff to 2, 0xff to 2, 0xda to 0)
//            .map { TimedByte(it.first, it.second) }
//            .map { "<td>" + it.byte.h() +
//                  (if (it.timingBitCount > 0) "<sup>${it.timingBitCount}</sup>" else "") }
//            .joinToString("")
//    val row2 = listOf(0xff to 2, 0xda to 0, 0xff to 2)
//            .map { TimedByte(it.first, it.second)}
//            .map { "<td class=\"address\">" + it.byte.h() +
//                    (if (it.timingBitCount > 0) "<sup>${it.timingBitCount}</sup>" else "") }
//            .joinToString("")
//    val html = """
//            <head>
//                <style>
//                    table { font: 16px Courier; border-spacing: 0 }
//                    td { vertical-align:bottom }
//                    .superscript {color: red; vertical-align: 30%; font-size: 80%}
//                    .address { background-color: yellow }
//                    .data { background-color: lightblue}
//                    .offset { color: blue; font-weight: bold; padding-right: 10px; }
//                </style>
//            </head>
//            <table>
//            <tr>$row1</tr>
//            <tr>$row2</tr>
//            </table>
//            """
//    println(html)
//    Browser(parent, NONE).apply {
//        text = html
//    }
//}
//
//fun test5(parent: Shell) {
//    val display = parent.display
////    val font1 = Fonts.font(display, "Tahoma", 14, SWT.BOLD)
////    val font2 = Fonts.font(display, "MS Mincho", 20, SWT.ITALIC)
////    val font3 = Fonts.font(display, "Arabic Transparent", 18, SWT.NORMAL)
//    val big = Fonts.font(parent.shell, "Courier New", 12, SWT.NORMAL)
//    val small = Fonts.font(parent.shell, "Courier New", 6, SWT.NORMAL)
//
//    val blue: Color = display.getSystemColor(SWT.COLOR_BLUE)
//    val green: Color = display.getSystemColor(SWT.COLOR_GREEN)
//    val yellow: Color = display.getSystemColor(SWT.COLOR_YELLOW)
//    val gray: Color = display.getSystemColor(SWT.COLOR_GRAY)
//
//    val layout = TextLayout(display).apply {
//        font = big
//    }
//    val style1 = TextStyle().apply {
//        rise = 4
//        font = small
//    }
////    val style2 = TextStyle(font2, green, null)
////    val style3 = TextStyle(font3, blue, gray)
//
//    layout.setText("FF2 FF2 D5\nFF FF2 AA")
//    layout.setStyle(style1, 2, 3)
//    layout.setStyle(style1, 6, 7)
//    layout.setStyle(style1, 16, 17)
//    parent.setBackground(display.getSystemColor(SWT.COLOR_WHITE))
//    parent.addListener(SWT.Paint, Listener { event -> layout.draw(event.gc, 10, 10) })
//
//}
//
//fun test4(shell: Shell) {
//    val tb = listOf(0xff to 2, 0xff to 2, 0xd5 to 0, 0xff to 10, 0xd5 to 0, 0xaa to 0).map {
//        TimedByte(it.first, it.second)
//    }
////    createTextViewer(shell, tb.iterator())
//}
//
//fun test3(shell: Shell) {
//    val f = Fonts.font(shell, "Courier New", 14)
//    val f2 = Fonts.font(shell, "Courier New", 8)
//    val st = listOf(12, 16, 20, 24).map {
//        StyleRange().apply {
//            start = it
//            length = 2
//            background = red(shell.display)
//            rise = 5
//            font = f2
//        }
//    }.toTypedArray()
//    val text1 = "FF FF D5 FF"
//    val text2 = " 2   2   0   3 "
//    TextViewer(shell, SWT.NONE).apply {
//        with(textWidget) {
//            text = "$text1\n$text2"
//            font = f
//            styleRanges = st
//        }
//    }
//}
//
//
//fun test2(shell: Shell) {
//    val f = Fonts.font(shell, "Courier New", 14)
//    val f2 = Fonts.font(shell, "Courier New", 8)
//    Composite(shell, SWT.NONE).apply {
//        background = yellow(display)
//        layout = GridLayout(1, false).apply {
//            marginHeight = 0
//            verticalSpacing = 0
//            marginBottom = 30
//        }
//        label(this, "FF FF D5 FF").apply {
//            font = f
//        }
//        label(this, "2  2  0  3").apply {
////            background = blue(display)
//            layoutData = GridData(SWT.CENTER, SWT.BEGINNING, true, true)
//            font = f2
//        }
//    }
//}
//
//
//fun test1(shell: Shell) {
//    val f = Fonts.font(shell, "Courier New", 14)
//    val f2 = Fonts.font(shell, "Courier New", 8)
//    val st = listOf(3).map {
//        StyleRange().apply {
//            start = it
//            length = 2
//            rise = 5
//            font = f2
//        }
//    }.toTypedArray()
//    Composite(shell, SWT.NONE).apply {
//        layout = FillLayout()
//        StyledText(this, SWT.BORDER).apply {
//            font = f
//            text = "FF\n 2"
//            styleRanges = st
//        }
//    }
//}
//

class DiskWindow(parent: Composite): Composite(parent, NONE) {
    private lateinit var diskLabel: Button
    private var byteText: Text? = null
    private var wordText: Text? = null
    private var fourAndFourText: Text? = null
    private val byteBufferWindow: ByteBufferWindow

    init {
        layout = GridLayout(3, false)

        createHeader(this)
        createRightSide(this)
        byteBufferWindow = ByteBufferWindow(this)

        UiState.diskStates[0].file.addListener { _, new ->
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

    /**
     * Create a pair of "Label:  [ Text field ]"
     */
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
            text = "Track info (regular expressions)"
        }
        createLabelText(result, "Address prologue").second.apply {
            text = "D5AA96"
            addListener(SWT.FocusOut) { e ->
                UiState.addressPrologue.value = text
            }
        }
        createLabelText(result, "Address epilogue").second.apply {
            text = "DEAA"
            addListener(SWT.FocusOut) { e ->
                UiState.addressEpilogue.value = text
            }
        }
        createLabelText(result, "Data prologue").second.apply {
            text = "D5AAAD"
            addListener(SWT.FocusOut) { e ->
                UiState.dataPrologue.value = text
            }
        }
        createLabelText(result, "Data epilogue").second.apply {
            text = "DEAA"
            addListener(SWT.FocusOut) { e ->
                UiState.dataEpilogue.value = text
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
        diskLabel = button(header, UiState.diskStates[0].file.value?.name ?: "<none>").apply {
            layoutData = GridData(SWT.BEGINNING, SWT.CENTER, false, false).apply {
////                background = grey(display)
                horizontalSpan = 2
//                horizontalAlignment = GridData.FILL
//                grabExcessHorizontalSpace = true
            }

        }

        //
        // Button to open a different disk
        //
        fileDialog(shell, diskLabel, UiState.diskStates[0].file)

        //
        // Phase number label
        //
        label(header, "Phase number:").apply {
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
            addSelectionListener(object : SelectionListener {
                override fun widgetSelected(e: SelectionEvent) {
                    val track = (e.widget as Combo).selectionIndex
                    UiState.currentBufferTrack.value = track
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
            addSelectionListener(object : SelectionListener {
                override fun widgetSelected(e: SelectionEvent) {
                    val newAlgo = when ((e.widget as Combo).selectionIndex) {
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

