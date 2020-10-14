package com.beust.swt

import com.beust.app.*
import com.beust.sixty.h
import org.eclipse.jface.text.TextPresentation
import org.eclipse.jface.text.TextViewer
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StyleRange
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite

class ByteBufferWindow(parent: Composite) : Composite(parent, SWT.NONE) {
    private var disk: IDisk? = null
    private var woz: Woz? = null
    private val rowSize = 16
    private val offsets: StyledText
    private val textViewer: TextViewer
    private val thisFont = font(shell, "Courier New", 10)
    private val thisSmallFont = font(shell, "Courier New", 8)
    private val thisFontBold = font(shell, "Courier New", 10, SWT.BOLD)
    private var graphicBuffer: GraphicBuffer? = null
    private var nibbleTrack: NibbleTrack? = null
    private val timingBitRanges = arrayListOf<StyleRange>()
    /** Map a byte on the track to its offset in the TextViewer */
    private val offsetMap = hashMapOf<Int, Int>()

    init {
//        background = display.getSystemColor(SWT.COLOR_BLUE)
        // Create a child composite to hold the controls
        layoutData = GridData(SWT.FILL, SWT.FILL, true, true).apply {
            widthHint = Int.MAX_VALUE
        }
        layout = GridLayout(2, false)
        background = white(display)

        offsets = StyledText(this, SWT.NONE).apply {
//            editable = false
            font = thisFontBold
            foreground = blue(display)
            layoutData = GridData(SWT.FILL, SWT.FILL).apply {
                widthHint = 50
                heightHint = Int.MAX_VALUE
                grabExcessVerticalSpace = true
            }

        }
        textViewer = TextViewer(this, SWT.NONE).apply { with(textWidget) {
            editable = false
            font = thisFont
            layoutData = GridData(SWT.FILL, SWT.FILL, true, true).apply {
                heightHint = Int.MAX_VALUE
            }
            addCaretListener { e ->
                val caretOffset = e.caretOffset
                if (caretOffset < text.length) {
                    val line = getLineAtOffset(caretOffset)
                    val mod = (if (line == 0) caretOffset else (caretOffset - line)) % 48
                    val currentBytes = graphicBuffer!!.bytes.map { it.byte }
                    val index = line * 16 + (mod / 3)
                    UiState.currentBytes.value = listOf(currentBytes[index], currentBytes[index + 1])
                    //            val result = bytes.getText(e.caretOffset + pair.first, e.caretOffset+ pair.second)
                    //                    .split(" ")
                    //                    .map { Integer.parseInt(it, 16) }
//                    log("position:${caretOffset} line:$line mod:$mod index:${index.h()} byte:${currentBytes[index].h()}")
                }
                UiState.caretSectorInfo.value = graphicBuffer?.currentSectorInfo(caretOffset / 3)
            }
        }}
//        bytes.addFocusListener(object: FocusAdapter() {
//            override fun focusGained(e: FocusEvent) {
//                val st = (e.source as StyledText)
//                val line = bytes.getLineAtOffset(bytes.caretOffset)
//                println("Caret position:" + bytes.caretOffset + " line: " + line)
//            }
//
//        })
        UiState.currentDisk1File.addListener { _, new ->
            if (new != null) {
                UiState.currentTrack.value = 0
                disk = IDisk.create(new)
                disk?.let {
                    updateBufferContent(it)
                }
            }
        }
        UiState.currentTrack.addListener { _, new ->
            updateBufferContent(track = new)
        }
        UiState.byteAlgorithn.addListener { _, new ->
            updateBufferContent(byteAlgorithm = new)
        }
        UiState.addressPrologue.addAfterListener { _, _ -> updateBufferStyles() }
        UiState.addressEpilogue.addAfterListener { _, _ -> updateBufferStyles() }
        UiState.dataPrologue.addAfterListener { _, _ -> updateBufferStyles() }
        UiState.dataEpilogue.addAfterListener { _, _ -> updateBufferStyles() }

        updateBufferContent()
    }

    class TimedByte(val byte: Int, val timingBitCount: Int = 0)
    data class SectorInfo(val volume: Int, val track: Int, val sector: Int, val checksum: Int)

    class GraphicBuffer(private val sizeInBytes: Int, nextByte: () -> TimedByte) {
        var index = 0
        val bytes = arrayListOf<TimedByte>()
        private val ranges = arrayListOf<IntRange>()

        init {
            repeat(sizeInBytes) {
                bytes.add(nextByte())
            }
        }

        operator fun hasNext() = index < sizeInBytes
        operator fun next(): TimedByte = bytes[index++]

//        fun peek(n: Int): List<TimedByte>
//            = if (index + n < sizeInBytes) bytes.slice(index..index + n - 1)
//               else emptyList()

        fun addRange(start: Int, end: Int) {
            ranges.add(IntRange(start, end))
        }

        fun currentSectorInfo(position: Int) : SectorInfo? {
            val range = ranges.firstOrNull { position in it}
            val result =
                if (range != null) {
                    var start = range.start + 3
                    val values = arrayListOf<Int>()
                    repeat(4) {
                        values.add(SixAndTwo.pair4And4(bytes[start].byte, bytes[start + 1].byte))
                        start += 2
                    }
                    SectorInfo(values[0], values[1], values[2], values[3])
                } else {
                    null
                }
            return result
        }
    }

    private fun updateBufferContent(passedDisk: IDisk? = null, track: Int = 0,
            byteAlgorithm: ByteAlgorithm = ByteAlgorithm.SHIFTED) {
        println("Updating buffer with file "+ UiState.currentDisk1File)
        textViewer.textWidget.text = ""
        val disk = passedDisk ?: IDisk.create(UiState.currentDisk1File.value)
        if (disk != null) {
            nibbleTrack = NibbleTrack(disk, disk.sizeInBits)
            repeat(160) { disk.decPhase() }
            repeat(track) {
                disk.incPhase()
            }

            fun nextByte(): TimedByte {
                val result =
                    when (byteAlgorithm) {
                        ByteAlgorithm.SHIFTED -> {
                            nibbleTrack!!.nextByte()
                        }
                        else -> {
                            TimedByte(disk.nextByte(), 0)
                        }
                    }
                return result
            }

            graphicBuffer = GraphicBuffer(disk.phaseSizeInBits(track) * 8) { -> nextByte() }
            val gb = graphicBuffer!!

            var row = 0
            val offsetText = StringBuffer()
            val byteText = StringBuffer()
            val ranges = arrayListOf<StyleRange>()
            timingBitRanges.clear()
            while (gb.hasNext()) {
                if (gb.index > 0 && gb.index % rowSize == 0) {
                    // Timing bits
//                    byteText.append("\n")
//                    timingBits.forEach {
//                        byteText.append(String.format("%2d ", it))
//                    }
//                    timingBits.clear()
                    // New offset
                    offsetText.append("\$" + String.format("%04X", row) + "\n")
                    byteText.append("\n")
                    row += rowSize
                }

                val nb = gb.next()
                byteText.append(nb.byte.h())
                offsetMap[gb.index] = byteText.length + 1

                // Add the timing bit count
                if (nb.timingBitCount > 0) {
                    timingBitRanges.add(StyleRange(byteText.length, 2, black(display), white(display)).apply {
                        font = thisSmallFont
                        rise = 4
                    })
                }
                byteText.append(if (nb.timingBitCount > 0) String.format("%-2d", nb.timingBitCount) else "  ")
            }
            display.asyncExec {
                offsets.text = offsetText.toString()
                textViewer.textWidget.text = byteText.toString()
                updateBufferStyles()
            }
        }
    }

    fun updateBufferStyles() {
        println("Updating styles with " + nibbleTrack)
        val r = nibbleTrack!!.analyze()
        val ranges = arrayListOf<StyleRange>().apply {
            r.forEach { trackRange ->
                add(StyleRange().apply {
                    start = offsetMap[trackRange.range.start]!!
                    length = offsetMap[trackRange.range.endInclusive + 1]!! - start - 1
                    background = if (trackRange.isAddress) yellow(display) else lightBlue(display)
                })
            }
        }
        textViewer.textWidget.styleRanges = emptyArray()
        val tp = TextPresentation().apply {
            timingBitRanges.forEach {
                mergeStyleRange(it)
            }
            ranges.forEach {
                mergeStyleRange(it)
            }
        }
        TextPresentation.applyTextPresentation(tp, textViewer.textWidget)
    }

}

fun createTextViewer(parent: Composite, bytes: List<ByteBufferWindow.TimedByte>) {
    val f = font(parent.shell, "Courier New", 14)
    val f2 = font(parent.shell, "Courier New", 8)
    TextViewer(parent, SWT.BORDER).apply { with(textWidget) {
        val sb = StringBuffer()
        val ranges = arrayListOf<StyleRange>()
        bytes.forEach { tb ->
            ranges.add(StyleRange().apply {
                start = sb.toString().length
                length = 2
                font = f
            })
            sb.append(tb.byte.h())
            ranges.add(StyleRange().apply {
                start = sb.toString().length
                length = 2
                font = f2
                rise = 4
            })
            tb.timingBitCount.let { b ->
                sb.append((if (b != 0) String.format("%-2d", b) else " ") + " ")
            }
        }
        text = sb.toString()
        ranges.add(StyleRange().apply {
            start = 0
            length = 5
            background = yellow(display)
        })
        val tp = TextPresentation().apply {
            ranges.forEach {
                mergeStyleRange(it)
            }
        }
        TextPresentation.applyTextPresentation(tp, this)
//        styleRanges = ranges.toTypedArray()
    } }
}
