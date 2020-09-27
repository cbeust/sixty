package com.beust.swt

import com.beust.app.*
import com.beust.sixty.h
import com.beust.sixty.hh
import com.beust.sixty.log
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StyleRange
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite

class ByteBufferWindow(parent: Composite) : Composite(parent, SWT.NONE) {
    private var diskFile = UiState.currentDiskFile.value
    private var disk: IDisk? = null
    private var woz: Woz? = null
    private val rowSize = 16
    private val offsets: StyledText
    private val bytes: StyledText
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
        bytes = StyledText(this, SWT.NONE).apply {
            editable = false
            font = thisFont
            layoutData = GridData(SWT.FILL, SWT.FILL, true, true)
//            .apply {
//                widthHint = 300
//                heightHint = Int.MAX_VALUE
//            }
        }
        bytes.addCaretListener { e ->
            val o = e.caretOffset
            val line = bytes.getLineAtOffset(e.caretOffset)
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
        UiState.currentDiskFile.addListener { _, new ->
            if (new != null) {
                disk = WozDisk(new)
                UiState.currentTrack.value = 0
                updateBuffer()
            }
        }
        UiState.currentTrack.addListener { _, _ ->
            updateBuffer()
        }
        UiState.byteAlgorithn.addListener { _, _ ->
            updateBuffer()
        }
        updateBuffer()
    }

    private fun updateBuffer() {
        currentBytes.clear()
        diskFile?.let { df ->
            Woz(df.readBytes()).let { woz ->
                val track = UiState.currentTrack.value
                val offset = woz.tmap.offsetFor(track)
                var position = 0
                if (offset != -1) {
                    val bitStream = woz.bitStreamForTrack(track)
                    var bitsToGo = woz.trks.trks[offset].bitCount

                    fun nextByte(): Int {
                        var byte = 0
                        when(UiState.byteAlgorithn.value) {
                            ByteAlgorithm.SHIFTED -> {
                                while (byte and 0x80 == 0) {
                                    val (p, bit) = bitStream.next(position)
                                    byte = byte.shl(1).or(bit)
                                    position = p
                                }
                            }
                            else -> {
                                repeat(8) {
                                    val (p, bit) = bitStream.next(position)
                                    byte = byte.shl(1).or(bit)
                                    position = p
                                }
                            }
                        }
                        return byte
                    }

                    var row = 0
                    val offsetText = StringBuffer()
                    val byteText = StringBuffer()
                    val state = State()
                    val ranges = arrayListOf<StyleRange>()
                    var addressStart = -1
                    var addressEnd = -1
                    var dataStart = -1
                    var dataEnd = -1
                    while (bitsToGo >= 0) {
                        offsetText.append("\$" + String.format("%04X", row) + "\n")
                        repeat(rowSize) {
                            val nb = nextByte()
                            state.state(byteText.length, nb)
                            if (state.foundD5AA96) {
                                addressStart = state.start
                            } else if (state.foundD5AAAD) {
                                dataStart = state.start
                            } else if (state.foundDEAA) {
                                if (addressStart > 0) {
                                    ranges.add(StyleRange(addressStart, byteText.length - addressStart + 2, null,
                                            lightBlue(display)))
                                    addressStart = -1
                                } else if (dataStart > 0) {
                                    ranges.add(StyleRange(dataStart, byteText.length - dataStart + 2, null,
                                            lightYellow(display)))
                                    dataStart = -1
                                }
                            }
                            byteText.append(nb.h() + " ")
                            currentBytes.add(nb)
                            bitsToGo -= 8
                        }
                        byteText.append("\n")
                        row += rowSize
                    }
                    offsets.text = offsetText.toString()
                    bytes.text = byteText.toString()
                    bytes.styleRanges = ranges.toTypedArray()
                } else {
                    println("NO OFFSET HERE")
                }
            }
        }
    }
}

class State {
    private var fD5 = false
    private var fD5AA = false
    var foundD5AAAD = false
    var foundD5AA96 = false
    private var fDE = false
    var foundDEAA = false
    var start = -1

    private fun reset() {
        fD5 = false
        fD5AA = false
        foundD5AA96 = false
        foundD5AAAD = false
        fDE = false
        foundDEAA = false
        start = -1
    }

    fun state(position: Int, byte: Int) {
        when(byte) {
            0xd5 -> {
                start = position
                fD5 = true
            }
            0xaa -> if (fD5) {
                fD5 = false
                fD5AA = true
            } else if (fDE) {
                fDE = false
                foundDEAA = true
            } else {
                reset()
            }
            0x96 -> if (fD5AA) {
                foundD5AA96 = true
            } else {
                reset()
            }
            0xad -> if (fD5AA) {
                fD5AA = false
                foundD5AAAD = true
            } else {
                reset()
            }
            0xde -> {
                start = position
                fDE = true
            }
            else -> {
                reset()
            }
        }
    }
}