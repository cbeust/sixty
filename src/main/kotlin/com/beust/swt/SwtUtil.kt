@file:Suppress("HasPlatformType", "unused")

package com.beust.swt

import org.eclipse.jface.resource.FontDescriptor
import org.eclipse.jface.resource.JFaceResources
import org.eclipse.jface.resource.LocalResourceManager
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.*

fun label(parent: Composite, t: String) = Label(parent, SWT.NONE).apply { text = t }
fun button(parent: Composite, t: String) = Button(parent, SWT.NONE).apply { text = t }
fun red(d: Display) = d.getSystemColor(SWT.COLOR_RED)
fun white(d: Display) = d.getSystemColor(SWT.COLOR_WHITE)
fun black(d: Display) = d.getSystemColor(SWT.COLOR_BLACK)
fun green(d: Display) = d.getSystemColor(SWT.COLOR_GREEN)
fun blue(d: Display) = d.getSystemColor(SWT.COLOR_BLUE)
fun grey(d: Display) = d.getSystemColor(SWT.COLOR_GRAY)
fun yellow(d: Display) = d.getSystemColor(SWT.COLOR_YELLOW)

fun font(shell: Shell, name: String, size: Int, style: Int = SWT.NONE)
    = LocalResourceManager(JFaceResources.getResources(), shell)
        .createFont(FontDescriptor.createFrom(name, size, style))

