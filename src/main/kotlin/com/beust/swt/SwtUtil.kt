@file:Suppress("HasPlatformType", "unused")

package com.beust.swt

import com.beust.app.Obs
import com.beust.app.UiState
import org.eclipse.jface.resource.FontDescriptor
import org.eclipse.jface.resource.JFaceResources
import org.eclipse.jface.resource.LocalResourceManager
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.RGB
import org.eclipse.swt.widgets.*
import java.io.File

fun label(parent: Composite, t: String, style: Int = SWT.NONE) = Label(parent, style).apply { text = t }
fun button(parent: Composite, t: String, style: Int = SWT.NONE) = Button(parent, style).apply { text = t }
fun fileDialog(shell: Shell, button: Button, observable: Obs<File?>): Button {
    button.addListener(SWT.Selection) { e ->
        val disk = FileDialog(shell).apply {
            text = "Pick a disk file"
            filterExtensions = arrayOf("*.dsk;*.woz")
            filterPath = "d:\\pd\\Apple disks"
        }.open()
        if (disk != null) {
            observable.value = File(disk)
        }
    }
    return button
}
fun red(d: Display) = d.getSystemColor(SWT.COLOR_RED)
fun white(d: Display) = d.getSystemColor(SWT.COLOR_WHITE)
fun black(d: Display) = d.getSystemColor(SWT.COLOR_BLACK)
fun green(d: Display) = d.getSystemColor(SWT.COLOR_GREEN)
fun blue(d: Display) = d.getSystemColor(SWT.COLOR_BLUE)
fun grey(d: Display) = d.getSystemColor(SWT.COLOR_GRAY)
fun yellow(d: Display) = d.getSystemColor(SWT.COLOR_YELLOW)
fun magenta(d: Display) = d.getSystemColor(SWT.COLOR_MAGENTA)

// Need to be disposed
fun black2(d: Display) = Color(d, 0x60, 0x60, 0x60)
fun orange(d: Display) = Color(d, 0xff, 0x45, 0x00)
fun lightBlue(d: Display) = Color(d, 0x99, 0xcc, 0xff)
fun lightYellow(d: Display) = Color(d, 0xff, 0xff, 0x99)
fun lightGrey(d: Display) = Color(d, 0xe0, 0xe0, 0xe0)

fun font(shell: Shell, name: String, size: Int, style: Int = SWT.NONE)
    = LocalResourceManager(JFaceResources.getResources(), shell)
        .createFont(FontDescriptor.createFrom(name, size, style))

