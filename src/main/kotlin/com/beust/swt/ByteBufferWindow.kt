package com.beust.swt

import com.beust.app.IDisk
import com.beust.app.UiState
import com.beust.app.Woz
import com.beust.app.WozDisk
import com.beust.sixty.h
import org.eclipse.jface.resource.FontDescriptor
import org.eclipse.swt.SWT
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

    init {
//        background = display.getSystemColor(SWT.COLOR_BLUE)
        // Create a child composite to hold the controls
        layoutData = GridData(SWT.FILL, SWT.FILL, true, true).apply {
            widthHint = Int.MAX_VALUE
        }
        layout = GridLayout(2, false)
        background = white(display)

        offsets = StyledText(this, SWT.NONE).apply {
            editable = false
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
        UiState.currentDiskFile.addListener { _, new ->
            if (new != null) {
                disk = WozDisk(new)
                UiState.currentTrack.value = 0
                updateBuffer()
            }
        }
        updateBuffer()
    }

    private fun updateBuffer() {
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
                        while (byte and 0x80 == 0) {
//                        repeat(8) {
                            val (p, bit) = bitStream.next(position)
                            byte = byte.shl(1).or(bit)
                            position = p
                        }
                        return byte
                    }

                    var row = 0
                    val offsetText = StringBuffer()
                    val byteText = StringBuffer()
                    while (bitsToGo >= 0) {
                        offsetText.append("\$" + String.format("%04X", row) + "\n")
                        repeat(rowSize) {
                            byteText.append(nextByte().h() + " ")
                            bitsToGo -= 8
                        }
                        byteText.append("\n")
                        row += rowSize
                    }
                    offsets.text = offsetText.toString()
                    bytes.text = byteText.toString()
                }
            }
        }
    }
}