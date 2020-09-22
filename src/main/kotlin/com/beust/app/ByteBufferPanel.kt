package com.beust.app

import java.awt.*
import javax.swing.GroupLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane


class ByteBufferPanel(val disk: WozDisk) : JPanel() {
    val defaultFont = Font("SansSerif", Font.BOLD, 20)

    init {
        val content = JPanel().apply {
            val gl = GridLayout(40, 1)
            layout = gl
            val rowSize = 16
            var row = 0
            repeat(20) {
                val line = JPanel().apply {
                }
                val left = JLabel("\$" + String.format("%04x", row)).apply {
                    font = defaultFont
                }
                line.add(left)
                repeat(rowSize) {
                    val label = JLabel("FF").apply {
                        isOpaque = true
                        font = defaultFont
                    }
                    line.add(label)
                }
                add(line)
                row += rowSize
            }
        }
        val scrollPane = JScrollPane(content)
        add(scrollPane)
    }
}