package com.beust.app

import com.beust.sixty.Apple2Memory
import com.beust.sixty.IKeyProvider
import com.beust.sixty.IMemory
import com.beust.swt.*
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.GC
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.ImageData
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*

class GraphicContext(val computer: () -> Apple2Computer, memory: () -> Apple2Memory) {
    val hiResWindow: HiResWindow
    val textWindow: TextWindow
    private val display: Display = Display()
    private val shell: Shell

    fun clear() {
        hiResWindow.clear()
        textWindow.clear()
    }

    private val keyProvider = object: IKeyProvider {
        override fun keyPressed(memory: IMemory, value: Int, shift: Boolean, control: Boolean) {
            memory.forceInternalRomValue(0xc000, value.or(0x80))
            memory.forceInternalRomValue(0xc010, 0x80)
        }
    }

    private fun maybeResize(control: Control) {
        if (control == hiResWindow) {
            control.display.asyncExec {
                val fullHeight = ACTUAL_HEIGHT
                val shortHeight = ACTUAL_MIXED_HEIGHT
                val b = control.bounds
                val newHeight = if (UiState.mainScreenMixed.value) shortHeight else fullHeight
                control.setBounds(b.x, b.y, b.width, newHeight)
                control.parent.layout()
            }
        }
    }

    private fun show(control: Control) {
        if (! control.isDisposed) with(control) {
            display.asyncExec {
                maybeResize(control)
                control.moveAbove(null)
            }
        }
    }

    fun run() {
        shell.open()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) display.sleep()
        }
        hiResWindow.stop()
//        display.dispose()
    }

    init {
        shell = Shell(display).apply {
            layout = GridLayout(3, false)
        }

        //
        // Contains the text/graphic windows and below it, the drive1/swap/drive2 buttons
        //
        Composite(shell, SWT.NONE).apply {
            layout = GridLayout(3, false)
            layoutData = GridData().apply {
                verticalAlignment = SWT.BEGINNING
            }

            //
            // Contains the text/graphic windows
            // Span over the three columns
            //
            Composite(this, SWT.NONE).apply {
                background = blue(display)
                layoutData = GridData().apply { horizontalSpan = 3 }
                display.addFilter(SWT.KeyDown) { e ->
                    if (e.keyCode != 0xd) {
                        val av = if (Character.isAlphabetic(e.keyCode)) e.character.toUpperCase().toInt()
                        else e.keyCode
                        keyProvider.keyPressed(memory(), av)
                    }
                }
                display.addFilter(SWT.Traverse) { e ->
                    if (e.keyCode == 0xd && e.widget is Shell) {
                        keyProvider.keyPressed(memory(), e.keyCode)
                        e.doit = false
                    }
                }


                //
                // Text screens
                //
                textWindow = TextWindow(this, 0x400).apply {
                    bounds = Rectangle(0, 0, ACTUAL_WIDTH, ACTUAL_HEIGHT + 10)
                }
                //                    .apply {
                //                pack()
                //            }

                //
                // Graphic screens
                //
                hiResWindow = HiResWindow(0x2000, this).apply {
                    bounds = Rectangle(0, 0, ACTUAL_WIDTH, ACTUAL_HEIGHT + 10)
                }
            }

            //
            // Drive 1 / swap / Drive2 buttons
            //
            val width = 250
            val height = 150
            fun driveButton(parent: Composite, drive: Int) = Button(parent, SWT.WRAP).apply {
                val ins = this::class.java.classLoader.getResource("disk-04.png")!!.openStream()
                val imageData = ImageData(ins)
                image = Image(display, imageData)
                addPaintListener { e -> with(e.gc) {
                    if ((drive == 1 && UiState.motor1.value) || (drive == 2 && UiState.motor2.value)) {
                        background = red(display)
                        foreground = red(display)
                    } else {
                        background = black(display)
                        foreground = black(display)
                    }
                   fillOval(42, 102, 13, 13)
                } }
                UiState.motor1.addListener { _, _ -> display.asyncExec { redraw() } }
                UiState.motor2.addListener { _, _ -> display.asyncExec { redraw() } }
                layoutData = GridData().apply {
                    heightHint = height
                    widthHint = width
                }
            }

            driveButton(this, 1)
            button(this, "Swap").apply {
                layoutData = GridData().apply {
                    heightHint = height
                    widthHint = 50
                }
            }
            driveButton(this, 2)


        }


        //
        // Middle panel, where the buttons live
        //
        val buttonContainer = Composite(shell, SWT.BORDER)
        val rebootButton = button(buttonContainer, "Reboot").apply {
            addListener(SWT.Selection) { e ->
                computer().reboot()
            }
        }

        buttonContainer.apply {
            layoutData = GridData(SWT.FILL, SWT.FILL, false, true)
            layout = GridLayout(1, true)
            listOf(
                    rebootButton,
                    fileDialog(shell, button(this, "Disk 1", SWT.WRAP), UiState.currentDisk1File),
                    button(this, "Swap\ndisks", SWT.WRAP),
                    fileDialog(shell, button(this, "Disk 2", SWT.WRAP), UiState.currentDisk2File)
            ).forEach {
                it.layoutData = GridData().apply {
                    widthHint = 50
                    heightHint = 50
                }
            }
        }

        //
        // Right panel
        //
        val parentHeight = textWindow.bounds.height + 220
//
//    createScrollableByteBuffer(shell, parentHeight).apply {
////        layoutData = GridData(GridData.FILL_BOTH, GridData.FILL_BOTH, true, true)
//    }
        val folder = TabFolder(shell, SWT.NONE).apply {
//        layoutData = FormData(500, parentHeight).apply {
//            top = FormAttachment(shell)
//            left = FormAttachment(mainWindow)
//        }
            layoutData = GridData(GridData.FILL, GridData.FILL, true, true).apply {
                grabExcessVerticalSpace = true
//            heightHint = parentHeight
            }
        }

        val diskWindow = DiskWindow(folder)
        TabItem(folder, SWT.NONE).apply {
            text = "DISK"
            control = diskWindow
        }
        TabItem(folder, SWT.NONE).apply {
            text = "DEBUGGER"
            control = DebuggerWindow(folder)
        }

//    val hiResWindow = HiResWindow(folder)
//    TabItem(folder, SWT.NONE).apply {
//        text = "\$2000"
//        control = hiResWindow
//    }
        folder.setSelection(0)


//    folder.setSize(500, 900)

//    mainWindow.pack()
//    folder.pack()
        shell.pack()
        shell.setSize(textWindow.bounds.width + folder.bounds.width, parentHeight)
//        rebootButton.addListener(SWT.Selection) { e ->
//            computer.reboot()
//        }

        UiState.mainScreenHires.addListener { _, new ->
            if (new) show(hiResWindow)
        }
        UiState.mainScreenPage2.addAfterListener { _, _ ->
            if (!memory().store80On) {
                if (UiState.mainScreenText.value) {
                    show(textWindow)
                } else {
                    show(hiResWindow)
                }
            }
        }
        UiState.mainScreenText.addListener { _, new ->
            if (new) show(textWindow)
        }
        UiState.mainScreenMixed.addAfterListener { _, new ->
            maybeResize(hiResWindow)
        }
    }
}