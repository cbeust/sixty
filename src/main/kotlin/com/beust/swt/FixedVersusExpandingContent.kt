package com.beust.swt

import org.eclipse.swt.SWT
import org.eclipse.swt.custom.CLabel
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell


/**
 * Brings face to face a ScrolledComposite with fixed content and, on the right side, a ScrolledComposite with expanding content.
 */
object FixedVersusExpandingContent {
    private const val STYLE = SWT.BORDER or SWT.H_SCROLL or SWT.V_SCROLL
    @JvmStatic
    fun main(args: Array<String>) {
        val display = Display()
        val shell = Shell(display)
        shell.text = "ScrolledComposite in 'scroll' and 'browser' mode"
        shell.layout = FillLayout()
        val leftSC = ScrolledComposite(shell, STYLE)
        val leftLabel = CLabel(leftSC, SWT.NONE)
        leftLabel.background = display.getSystemColor(SWT.COLOR_DARK_GREEN)
        leftLabel.foreground = display.getSystemColor(SWT.COLOR_WHITE)
        val leftText = """
            Fixed size content.
            
            Scrollbars appear if the
            ScrolledComposite
            is resized to be too small
            to show the entire content.
            """.trimIndent()
        leftLabel.text = leftText
        leftLabel.alignment = SWT.CENTER
        leftLabel.setSize(250, 250)
        leftSC.content = leftLabel
        val rightSC = ScrolledComposite(shell, STYLE)
        val rightLabel = CLabel(rightSC, SWT.NONE)
        val rightText = """
            Expanding content.
            
            The content has a minimum size.
            If the ScrolledComposite is
            resized bigger than the minimum size,
            the content will grow in size.
            If the ScrolledComposite is made too small
            to show the minimum size, scrollbars will appear.
            """.trimIndent()
        rightLabel.text = rightText
        rightLabel.background = display.getSystemColor(SWT.COLOR_DARK_GRAY)
        rightLabel.foreground = display.getSystemColor(SWT.COLOR_WHITE)
        rightLabel.alignment = SWT.CENTER
        rightSC.content = rightLabel
        rightSC.expandHorizontal = true
        rightSC.expandVertical = true
        rightSC.setMinSize(250, 250)
        shell.setSize(600, 100)
        shell.open()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) display.sleep()
        }
        display.dispose()
    }
}