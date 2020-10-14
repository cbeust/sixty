package com.beust.swt

import com.beust.app.*
import com.beust.sixty.h
import org.eclipse.jface.text.TextPresentation
import org.eclipse.jface.text.TextViewer
import org.eclipse.swt.SWT
import org.eclipse.swt.browser.Browser
import org.eclipse.swt.custom.StyleRange
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite

class ByteBufferWindow(parent: Composite) : Composite(parent, SWT.NONE) {
    private var disk: IDisk? = null
    private val rowSize = 16
    private val browser: Browser
    private val thisSmallFont = font(shell, "Courier New", 8, SWT.NORMAL)
    private var nibbleTrack: NibbleTrack? = null
    private val timingBitRanges = arrayListOf<StyleRange>()

    init {
        // Create a child composite to hold the controls
        layoutData = GridData(SWT.FILL, SWT.FILL, true, true)
        layout = GridLayout(2, false)
        background = white(display)


        browser = Browser(this, SWT.NONE).apply {
            layoutData = GridData(SWT.FILL, SWT.FILL, true, true)
        }

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
        UiState.addressPrologue.addAfterListener { _, _ -> updateBufferContent() }
        UiState.addressEpilogue.addAfterListener { _, _ -> updateBufferContent() }
        UiState.dataPrologue.addAfterListener { _, _ -> updateBufferContent() }
        UiState.dataEpilogue.addAfterListener { _, _ -> updateBufferContent() }

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
        browser.text = ""
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

            var row = 0
            val byteText = StringBuffer()
            val html = StringBuffer("""
                <head>
                    <style>
                        table { font: 14px Courier; border-spacing: 0 }
                        td { vertical-align:bottom }
                        .superscript { color: red; vertical-align: 30%; font-size: 80%}
                        .address { background-color: yellow }
                        .data { background-color: lightblue}
                        .offset { color: blue; font-weight: bold; padding-right: 15px; }
                    </style>
                </head>
                <body>
                    <table>
            """.trimIndent())
            timingBitRanges.clear()
            val analyzedTrack = nibbleTrack!!.analyze()
            var byteCount = 0
            while (byteCount < analyzedTrack.sizeInBits / 8 + 1) {
                if (byteCount % rowSize == 0) {
                    // New offset
                    if (byteCount > 0) html.append("</tr>\n")
                    html.append("<tr>")
                        .append("<td class=\"offset\">\$" + String.format("%04X", row) + "</td>")
                    row += rowSize
                }

                val nb = nextByte()
                byteText.append(nb.byte.h())
                val cls = when(analyzedTrack.typeFor(byteCount)) {
                    NibbleTrack.AnalyzedTrack.Type.ADDRESS -> " class=\"address\""
                    NibbleTrack.AnalyzedTrack.Type.DATA -> " class=\"data\""
                    else -> ""
                }
                val sup = if (nb.timingBitCount > 0) "<sup>${nb.timingBitCount}</sup>" else ""
                html.append("<td$cls>" + nb.byte.h() + sup + "</td>")

                // Add the timing bit count
                if (nb.timingBitCount > 0) {
                    timingBitRanges.add(StyleRange(byteText.length, 2, black(display), white(display)).apply {
                        font = thisSmallFont
                        rise = 4
                    })
                }
                byteText.append(if (nb.timingBitCount > 0) String.format("%-2d", nb.timingBitCount) else "  ")
                byteCount++
            }
            html.append("</table></body></html>")
            display.asyncExec {
                browser.text = html.toString()
            }
        }
    }
}
