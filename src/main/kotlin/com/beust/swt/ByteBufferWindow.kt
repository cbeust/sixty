package com.beust.swt

import com.beust.app.ByteAlgorithm
import com.beust.app.IDisk
import com.beust.app.UiState
import com.beust.app.Woz
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
    private val currentBytes = arrayListOf<Int>()

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
            layoutData = GridData(SWT.FILL, SWT.FILL, true, true)
            .apply {
//                widthHint = 300
                heightHint = Int.MAX_VALUE
            }
        }
        bytesStyledText.addCaretListener { e ->
            val o = e.caretOffset
            val line = bytesStyledText.getLineAtOffset(e.caretOffset)
            val mod = (if (line == 0) o else (o - line)) % 48
            val index = line * 16 + (mod / 3)
            UiState.currentBytes.value = listOf(currentBytes[index], currentBytes[index + 1])
//            val result = bytes.getText(e.caretOffset + pair.first, e.caretOffset+ pair.second)
//                    .split(" ")
//                    .map { Integer.parseInt(it, 16) }
            log("o:${o} line:$line mod:$mod index:${index.h()} byte:${currentBytes[index].h()}")
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
        UiState.addressPrologue.addAfterListener { _, _ -> updateBuffer() }
        UiState.addressEpilogue.addAfterListener { _, _ -> updateBuffer() }

        updateBuffer()
    }

    class GraphicBuffer(private val sizeInBytes: Int, nextByte: () -> Int) {
        var index = 0
        private val bytes = arrayListOf<Int>()
        init {
            repeat(sizeInBytes) {
                bytes.add(nextByte())
            }
        }

        fun hasNext() = index < sizeInBytes
        fun next(): Int = bytes[index++]

        fun peek(n: Int): List<Int>
            = if (index + n < sizeInBytes) bytes.slice(index..index + n - 1)
               else emptyList()
    }

    fun updateBuffer(passedDisk: IDisk? = null, track: Int = 0,
            byteAlgorithm: ByteAlgorithm = ByteAlgorithm.SHIFTED) {
        println("Updating buffer with file "+ UiState.currentDisk1File)
        bytesStyledText.text = ""
        currentBytes.clear()
        val disk = passedDisk ?: IDisk.create(UiState.currentDisk1File.value)
        if (disk != null) {
            repeat(160) { disk.decTrack() }
            repeat(track * 4) {
                disk.incTrack()
            }

            fun nextByte(): Int {
                var byte = 0
                when (byteAlgorithm) {
                    ByteAlgorithm.SHIFTED -> {
                        while (byte and 0x80 == 0) {
                            val bit = disk.nextBit()
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
                return byte
            }

            val bytes = GraphicBuffer(disk.phaseSizeInBytes(track)) { -> nextByte() }

            var row = 0
            val offsetText = StringBuffer()
            val byteText = StringBuffer()
            val ranges = arrayListOf<StyleRange>()
            var addressStart = -1
            var dataStart = -1
            while (bytes.hasNext()) {
                if (bytes.index > 0 && bytes.index % rowSize == 0) {
                    offsetText.append("\$" + String.format("%04X", row) + "\n")
                    byteText.append("\n")
                    row += rowSize
                }
                val p2 = bytes.peek(2)
                val p3 = bytes.peek(3)
                if (p3 == UiState.addressPrologue.value) {
                    addressStart = byteText.length
                } else if (p2 == UiState.addressEpilogue.value && addressStart > 0) {
                    ranges.add(StyleRange(addressStart, byteText.length - addressStart + 5, null,
                            lightBlue(display)))
                    addressStart = -1
                } else if (p3 == UiState.dataPrologue.value) {
                    dataStart = byteText.length
                } else if (p2 == UiState.dataEpilogue.value && dataStart > 0) {
                    ranges.add(StyleRange(dataStart, byteText.length - dataStart + 5, null,
                            lightYellow(display)))
                    dataStart = -1
                }
                val nb = bytes.next()
                byteText.append(nb.h() + " ")
                currentBytes.add(nb)
            }
            display.asyncExec {
                offsets.text = offsetText.toString()
                bytesStyledText.text = byteText.toString()
                bytesStyledText.styleRanges = ranges.toTypedArray()
            }
        }
    }
}
