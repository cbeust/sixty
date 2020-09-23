package com.beust.app

import java.awt.Dimension
import javax.swing.GroupLayout
import javax.swing.JFrame
import javax.swing.JTabbedPane

class Apple2Frame: JFrame() {
    val textScreen: TextScreenPanel
    val hiresScreen: HiResScreenPanel
    val tabbedPane = JTabbedPane()

    init {
        val layout = GroupLayout(contentPane)
        contentPane.layout = layout
        title = "Cédric's Apple ][ emulator. Disk: " + UiState.currentDiskName

        UiState.currentDiskName.addListener { _, new: String -> title = new }

        isVisible = true //making the frame visible
        setSize(1700, 1000)

        val w = HiResScreenPanel.WIDTH * 2
        val h = HiResScreenPanel.HEIGHT * 2

        textScreen = TextScreenPanel().apply {
            preferredSize = Dimension(w, h)
            setSize(w, h)
        }

        hiresScreen = HiResScreenPanel().apply {
            preferredSize = Dimension(w, h)
            setSize(w, h)
        }
        val wozPanel = WozDiskPanel(WOZ_DOS_3_3)
        tabbedPane.addTab("Bytes", null, ByteBufferPanel(WOZ_DOS_3_3))
        tabbedPane.addTab("Hires ($2000)", null, hiresScreen)
        tabbedPane.addTab("WOZ disk", null, wozPanel)

        layout.apply {
            autoCreateGaps = true
            autoCreateContainerGaps = true
            setHorizontalGroup(createSequentialGroup()
                    .addComponent(textScreen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Int.MAX_VALUE)
                    .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Int.MAX_VALUE)
            )
            setVerticalGroup(createParallelGroup()
                    .addComponent(textScreen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Int.MAX_VALUE)
                    .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Int.MAX_VALUE)
            )
//            pack()
        }

    }
}