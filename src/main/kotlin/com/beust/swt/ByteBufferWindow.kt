package com.beust.swt

import com.beust.app.*
import com.beust.sixty.h
import org.eclipse.jface.resource.FontDescriptor
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.events.FocusAdapter
import org.eclipse.swt.events.FocusEvent
import org.eclipse.swt.events.FocusListener
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
            val byte = currentBytes[index]
//            val result = bytes.getText(e.caretOffset + pair.first, e.caretOffset+ pair.second)
//                    .split(" ")
//                    .map { Integer.parseInt(it, 16) }
            println("o:${o} line:$line mod:$mod index:${index.h()} byte:${currentBytes[index].h()}")
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
                    while (bitsToGo >= 0) {
                        offsetText.append("\$" + String.format("%04X", row) + "\n")
                        repeat(rowSize) {
                            val nb = nextByte()
                            byteText.append(nb.h() + " ")
                            currentBytes.add(nb)
                            bitsToGo -= 8
                        }
                        byteText.append("\n")
                        row += rowSize
                    }
                    offsets.text = offsetText.toString()
                    bytes.text = byteText.toString()
                } else {
                    println("NO OFFSET HERE")
                }
            }
        }
    }
}
