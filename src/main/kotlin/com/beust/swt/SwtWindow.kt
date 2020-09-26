package com.beust.swt

import com.beust.app.app.ITextScreen
import com.beust.sixty.IKeyProvider
import com.beust.sixty.IMemory
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.events.KeyAdapter
import org.eclipse.swt.events.KeyEvent
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.layout.*
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
//        layout = FillLayout()
        layout = GridLayout(2, false)
//        layout = FormLayout()
    }

    //
    // Main screen of the emulator
    //
    val mainWindow = MainWindow(shell).apply {
        pack()
        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                val av = if (Character.isAlphabetic(e.keyCode)) e.character.toUpperCase().toInt()
                else e.keyCode
                keyProvider.keyPressed(memory, av)
            }
        })
        layoutData = GridData().apply {
            horizontalAlignment = GridData.HORIZONTAL_ALIGN_END
            verticalAlignment = GridData.VERTICAL_ALIGN_END
        }
//        layoutData = FormData().apply {
////            width = SWT.FILL
//            top = FormAttachment(shell)
//            left = FormAttachment(shell)
//        }
    }

    //
    // Right panel
    //
//    val c = Composite(shell, SWT.NONE).apply {
//        layout = FillLayout()
//        layoutData = GridData().apply {
//            horizontalAlignment = GridData.HORIZONTAL_ALIGN_END
//            verticalAlignment = GridData.VERTICAL_ALIGN_END
//        }
//    }
//    createScrollableByteBuffer(shell)
    mainWindow.pack()
    val parentHeight = mainWindow.bounds.height + 50
    val folder = TabFolder(shell, SWT.NONE).apply {
//        layoutData = FormData(500, parentHeight).apply {
//            top = FormAttachment(shell)
//            left = FormAttachment(mainWindow)
//        }
        layoutData = GridData(GridData.FILL, SWT.BEGINNING, true, true).apply {
//            heightHint = parentHeight
        }
    }

    val tab = TabItem(folder, SWT.NONE).apply {
        text = "DISK"
        control = t(folder, parentHeight)
    }

//    folder.setSize(500, 900)

//    mainWindow.pack()
//    folder.pack()
    shell.pack()
    shell.setSize(mainWindow.bounds.width + 700, parentHeight)
    return SwtContext(display, shell, mainWindow)
}

fun createScrollableByteBuffer(parent: Composite, parentHeight: Int): ScrolledComposite {
//    val composite = Composite(parent, SWT.NONE).apply {
//        layoutData = GridData(SWT.FILL, SWT.TOP, true, false)
//    }

    val result = ScrolledComposite(parent, SWT.V_SCROLL or SWT.H_SCROLL).apply {
        layout = GridLayout()
        background = blue(display)
        expandVertical = true
//        layoutData = GridData(SWT.FILL, SWT.FILL, true, true, 1, 1)
//        addListener(SWT.Resize) { event ->
//            val width: Int = clientArea.width
//            val s = computeSize(width, SWT.DEFAULT)
//            println("Resizing to $s")
//            setMinSize(s)
//        }
    }

    val bb = ByteBufferTab(result).let {
        it.size = Point(300, parentHeight)
        result.setMinSize(Point(300, 700))
        result.size = Point(300, parentHeight)
        it.layoutData = GridData(SWT.FILL, SWT.FILL, false, false)
        result.content = it
        result.expandVertical = true
//        result.setSize(Point(300, 200))
    }

//    val c = Composite(result, SWT.NONE).let {
//        it.background = yellow(it.display)
//        it.size = Point(600, 1000)
//        result.content = it
//    }
//    val button = button(result, "THIS IS THE BUTTON").let {
//        it.size = Point(300, parentHeight)
//        result.setMinSize(Point(300, 700))
//        result.size = Point(300, parentHeight)
////        it.layoutData = GridData(SWT.FILL, SWT.FILL, false, false)
//        result.content = it
//        result.expandVertical = true
////        result.setMinSize(Point(300, 200))
//    }

    parent.layout()
    return result
}

fun t(parent: Composite, parentHeight: Int): Composite {
    val result = Composite(parent, SWT.NONE).apply {
        layout = GridLayout(3, false)
        background = yellow(display)
    }
    label(result, "File:").apply {
    }
    label(result, "SomeDisk.woz").apply {
        layoutData = GridData().apply {
            horizontalSpan = 2
            horizontalAlignment = GridData.FILL
            grabExcessHorizontalSpace = true
            background = red(display)
        }
    }
//    label(result, "Rest").apply {
//        layoutData = GridData().apply {
//            background = blue(display)
//            horizontalAlignment = GridData.FILL
//            verticalAlignment = GridData.FILL
//            horizontalSpan = 3
//            grabExcessVerticalSpace = true
//        }
//    }
    createScrollableByteBuffer(result, parentHeight).apply {
        layoutData = GridData().apply {
            background = blue(display)
            horizontalAlignment = GridData.FILL
            verticalAlignment = GridData.FILL
            horizontalSpan = 3
            grabExcessVerticalSpace = true
        }

    }
    return result
}

