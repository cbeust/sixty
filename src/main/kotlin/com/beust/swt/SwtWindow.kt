package com.beust.swt

import com.beust.app.swing.ITextScreen
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.TabFolder
import org.eclipse.swt.widgets.TabItem


private val allowed = hashSetOf('a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F')

fun isHex(c: Char) = Character.isDigit(c) || allowed.contains(c)

//val WIDTH = 200
//val HEIGHT = 300

interface IGraphics {
    fun run()
}

class SwtContext(val display: Display, val shell: Shell, val textScreen: ITextScreen): IGraphics {
    override fun run() {
        shell.open()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) display.sleep()
        }
        display.dispose()
    }
}

fun createWindows(): SwtContext {
    val display = Display()
    val shell = Shell(display)
    shell.layout = FillLayout()
    val mainWindow = MainWindow(shell)
    mainWindow.pack()
    val height = mainWindow.bounds.height
    val tabFolder = TabFolder(shell, SWT.NONE)
    val scrolled = ScrolledComposite(tabFolder, SWT.V_SCROLL)
    val wozItem = TabItem(tabFolder, SWT.NONE).apply {
        text = "WOZ"
        control = scrolled
    }
    scrolled.content = ByteBufferTab(scrolled)

    shell.pack()
    shell.setSize(mainWindow.bounds.width + tabFolder.bounds.width + 10, height)
    return SwtContext(display, shell, mainWindow)
}
