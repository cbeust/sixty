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
            .apply {
//                widthHint = 300
                heightHint = Int.MAX_VALUE
            }
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
        updateBuffer()
    }

    private fun updateBuffer(passedDisk: IDisk? = null, track: Int = 0,
            byteAlgorithm: ByteAlgorithm = ByteAlgorithm.SHIFTED) {
        println("Updating buffer with file "+ UiState.currentDisk1File)
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

            var row = 0
            val offsetText = StringBuffer()
            val byteText = StringBuffer()
            val state = State()
            val ranges = arrayListOf<StyleRange>()
            var addressStart = -1
            var dataStart = -1
            repeat(disk.phaseSizeInBytes(track) / 16 + 20) {
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
                }
                byteText.append("\n")
                row += rowSize
            }
            display.asyncExec {
                offsets.text = offsetText.toString()
                bytes.text = byteText.toString()
                bytes.styleRanges = ranges.toTypedArray()
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