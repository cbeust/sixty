package com.beust.swt

import com.beust.app.*
import com.beust.sixty.h
import com.beust.sixty.log
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
    private val bytesStyledText: StyledText
    private val thisFont = font(shell, "Courier New", 10)
    private val thisFontBold = font(shell, "Courier New", 10, SWT.BOLD)
    private var graphicBuffer: GraphicBuffer? = null

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
        bytesStyledText = StyledText(this, SWT.NONE).apply {
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
                    log("position:${caretOffset} line:$line mod:$mod index:${index.h()} byte:${currentBytes[index].h()}")
                }
                UiState.caretSectorInfo.value = graphicBuffer?.currentSectorInfo(caretOffset / 3)
            }
        }
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
                    updateBuffer(it)
                }
            }
        }
        UiState.currentTrack.addListener { _, new ->
            updateBuffer(track = new)
        }
        UiState.byteAlgorithn.addListener { _, new ->
            updateBuffer(byteAlgorithm = new)
        }
        UiState.addressPrologue.addAfterListener { _, _ -> updateBuffer() }
        UiState.addressEpilogue.addAfterListener { _, _ -> updateBuffer() }
        UiState.dataPrologue.addAfterListener { _, _ -> updateBuffer() }
        UiState.dataEpilogue.addAfterListener { _, _ -> updateBuffer() }

        updateBuffer()
    }

    class TimedByte(val byte: Int, val timingBit: Boolean = false)
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

        fun hasNext() = index < sizeInBytes
        fun next(): TimedByte = bytes[index++]

        fun peek(n: Int): List<TimedByte>
            = if (index + n < sizeInBytes) bytes.slice(index..index + n - 1)
               else emptyList()

        fun addRange(start: Int, end: Int) {
            ranges.add(IntRange(start, end))
        }

        fun currentSectorInfo(position: Int) : SectorInfo? {
            val range = ranges.firstOrNull { position in it}
            val result =
                if (range != null) {
                    println("Found range: " + range + " should be d5: " + bytes[range.start])
                    var start = range.start + UiState.addressPrologue.value.size
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

    fun updateBuffer(passedDisk: IDisk? = null, track: Int = 0,
            byteAlgorithm: ByteAlgorithm = ByteAlgorithm.SHIFTED) {
        println("Updating buffer with file "+ UiState.currentDisk1File)
        bytesStyledText.text = ""
        val disk = passedDisk ?: IDisk.create(UiState.currentDisk1File.value)
        if (disk != null) {
            repeat(160) { disk.decTrack() }
            repeat(track * 4) {
                disk.incTrack()
            }

            fun nextByte(): TimedByte {
                var timed = false
                var byte = 0
                when (byteAlgorithm) {
                    ByteAlgorithm.SHIFTED -> {
                        var bitCount = 0
                        while (byte and 0x80 == 0) {
                            val bit = disk.nextBit()
                            if (bit == 0) {
                                if (bitCount == 1) {
                                    timed = true
                                    bitCount = -1
                                } else if (bitCount == 0) {
                                    bitCount = 1
                                }
                            } else {
                                bitCount = -1
                            }
                            byte = byte.shl(1).or(bit)
                        }
                    }
                    else -> {
                        repeat(8) {
                            val bit = disk.nextBit()
                            byte = byte.shl(1).or(bit)
                        }
                    }
                }
                return TimedByte(byte, timed)
            }

            graphicBuffer = GraphicBuffer(disk.phaseSizeInBytes(track)) { -> nextByte() }
            val gb = graphicBuffer!!

            var row = 0
            val offsetText = StringBuffer()
            val byteText = StringBuffer()
            val ranges = arrayListOf<StyleRange>()
            var addressStart = -1
            var dataStart = -1
            var byteSectorStart = -1
            while (gb.hasNext()) {
                if (gb.index > 0 && gb.index % rowSize == 0) {
                    offsetText.append("\$" + String.format("%04X", row) + "\n")
                    byteText.append("\n")
                    row += rowSize
                }
                val p2 = gb.peek(2).map { it.byte }
                val p3 = gb.peek(3).map { it.byte }
                if (p3 == UiState.addressPrologue.value) {
                    addressStart = byteText.length + 1
                    byteSectorStart = gb.index
                } else if (p2 == UiState.addressEpilogue.value && addressStart > 0) {
                    ranges.add(StyleRange(addressStart, byteText.length - addressStart + 6, null,
                            lightBlue(display)))
                    addressStart = -1
                } else if (p3 == UiState.dataPrologue.value) {
                    dataStart = byteText.length + 1
                } else if (p2 == UiState.dataEpilogue.value && dataStart > 0) {
                    ranges.add(StyleRange(dataStart, byteText.length - dataStart + 6, null,
                            lightYellow(display)))
                    dataStart = -1
                    gb.addRange(byteSectorStart, gb.index)
                }
                val nb = gb.next()
                byteText.append(if (nb.timingBit) "+" else " ")
                byteText.append(nb.byte.h())
            }
            display.asyncExec {
                offsets.text = offsetText.toString()
                bytesStyledText.text = byteText.toString()
                bytesStyledText.styleRanges = ranges.toTypedArray()
            }
        }
    }
}
