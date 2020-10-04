package com.beust.app

import com.beust.sixty.Apple2Memory
import com.beust.sixty.IKeyProvider
import com.beust.sixty.IMemory
import com.beust.swt.*
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*

class GraphicContext(val computer: () -> Apple2Computer, memory: () -> Apple2Memory) {
    val hiResWindow: HiResWindow
    val textScreen: TextWindow
    private val display: Display = Display()
    private val shell: Shell

    fun clear() {
        hiResWindow.clear()
        textScreen.clear()
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
                val shortHeight = ACTUAL_HEIGHT * 5 / 6
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

        val mainContainer = Composite(shell, SWT.NONE).apply {
            background = blue(display)
            layoutData = GridData().apply {
                verticalAlignment = SWT.BEGINNING
                widthHint = ACTUAL_WIDTH
                heightHint = ACTUAL_HEIGHT
            }
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
        }

        //
        // Main screen of the emulator
        //

        textScreen = TextWindow(mainContainer, 0x400).apply {
            bounds = Rectangle(0, 0, ACTUAL_WIDTH, ACTUAL_HEIGHT)
        }.apply {
            pack()
        }

        //
        // Graphic screens
        //
        hiResWindow = HiResWindow(0x2000, mainContainer).apply {
            bounds = Rectangle(0, 0, ACTUAL_WIDTH, ACTUAL_HEIGHT)
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
        val parentHeight = textScreen.bounds.height + 120
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
        shell.setSize(textScreen.bounds.width + folder.bounds.width, parentHeight)
//        rebootButton.addListener(SWT.Selection) { e ->
//            computer.reboot()
//        }

        UiState.mainScreenHires.addListener { _, new ->
            if (new) show(hiResWindow)
        }
        UiState.mainScreenPage2.addAfterListener { _, _ ->
            if (!memory().store80On) {
                if (UiState.mainScreenText.value) {
                    show(textScreen)
                } else {
                    show(hiResWindow)
                }
            }
        }
        UiState.mainScreenText.addListener { _, new ->
            if (new) show(textScreen)
        }
        UiState.mainScreenMixed.addAfterListener { _, new ->
            maybeResize(hiResWindow)
        }
    }
}