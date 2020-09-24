package com.beust.swt

import org.eclipse.jface.action.*
import org.eclipse.jface.preference.RadioGroupFieldEditor
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.jface.viewers.TableLayout
import org.eclipse.jface.window.ApplicationWindow
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.layout.RowLayout
import org.eclipse.swt.widgets.*


private val allowed = hashSetOf('a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F')

fun isHex(c: Char) = Character.isDigit(c) || allowed.contains(c)

//val WIDTH = 200
//val HEIGHT = 300

fun main() {
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
    shell.open()
    while (!shell.isDisposed) {
        if (!display.readAndDispatch()) display.sleep()
    }
    display.dispose()
}
