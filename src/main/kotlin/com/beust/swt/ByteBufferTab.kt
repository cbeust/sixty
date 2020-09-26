package com.beust.swt

import com.beust.app.IDisk
import com.beust.app.UiState
import com.beust.app.Woz
import com.beust.app.WozDisk
import com.beust.sixty.h
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite

class ByteBufferTab(parent: Composite) : Composite(parent, SWT.NONE) {
    private var diskFile = UiState.currentDiskFile.value
    private var disk: IDisk? = null
    private var woz: Woz? = null
    private val rowSize = 16

    init {
//        background = display.getSystemColor(SWT.COLOR_BLUE)
        // Create a child composite to hold the controls

        layout = GridLayout(rowSize + 1, true)
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
                        repeat(8) {
                            val (p, bit) = bitStream.next(position)
                            byte = byte.shl(1).or(bit)
                            position = p
                        }
                        return byte
                    }

                    var row = 0
                    while (bitsToGo >= 0) {
                        label(this, "\$" + String.format("%04X", row * 16))
                        repeat(rowSize) {
                            label(this, nextByte().h())
                            bitsToGo -= 8
                        }
                        row += rowSize
                    }
                }
            }
        }
    }
}
