package com.beust.swt

import com.beust.sixty.IComputer
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell

//class SwtContext(val display: Display, val shell: Shell, val textScreen: TextWindow, val hiResWindow: HiResWindow)
//    : IGraphics
//{
//    override lateinit var computer: IComputer
//
//    override fun run() {
//        shell.open()
//        while (!shell.isDisposed) {
//            if (!display.readAndDispatch()) display.sleep()
//        }
//        hiResWindow.stop()
////        display.dispose()
//    }
//
//    fun show(c: Control) {
//        c.moveAbove(null)
//    }
//}