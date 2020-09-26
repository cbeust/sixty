package com.beust.swt

import com.beust.app.app.ITextScreen
import com.beust.sixty.IKeyProvider
import com.beust.sixty.IMemory
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.CTabItem
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.events.KeyAdapter
import org.eclipse.swt.events.KeyEvent
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.widgets.*


private val allowed = hashSetOf('a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F')

fun isHex(c: Char) = Character.isDigit(c) || allowed.contains(c)

//val WIDTH = 200
//val HEIGHT = 300

interface IGraphics {
    fun run()
}

class SwtContext(val display: Display, val shell: Shell, val textScreen: ITextScreen)
    : IGraphics
{
    override fun run() {
        shell.open()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) display.sleep()
        }
        display.dispose()
    }
}

fun createWindows(memory: IMemory, keyProvider: IKeyProvider): SwtContext {
    val display = Display()
    val shell = Shell(display).apply {
        layout = FillLayout()
    }

    //
    // Main screen of the emulator
    //
    val mainWindow = MainWindow(shell)
    mainWindow.pack()
    mainWindow.addKeyListener(object: KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            val av = if (Character.isAlphabetic(e.keyCode)) e.character.toUpperCase().toInt()
            else e.keyCode
            keyProvider.keyPressed(memory, av)
        }
    })
    mainWindow.layoutData = GridData().apply {
        horizontalAlignment = GridData.HORIZONTAL_ALIGN_END
        verticalAlignment = GridData.VERTICAL_ALIGN_END
    }

    //
    // Right panel
    //
    val folder = TabFolder(shell, SWT.NONE)

    val tab = TabItem(folder, SWT.NONE).apply {
        text = "DISK"
        control = createScrollableByteBuffer(folder)
    }

    shell.setSize(mainWindow.bounds.width + folder.bounds.width, mainWindow.bounds.height + 50)
    return SwtContext(display, shell, mainWindow)
}
