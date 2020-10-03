package com.beust.swt

import com.beust.app.UiState
import com.beust.sixty.IComputer
import com.beust.sixty.IKeyProvider
import com.beust.sixty.IMemory
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*


private val allowed = hashSetOf('a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F')

const val WIDTH = 280
const val HEIGHT = 192
const val WIDTH_FACTOR = 2
const val HEIGHT_FACTOR = 2
const val ACTUAL_WIDTH = WIDTH * WIDTH_FACTOR
const val ACTUAL_HEIGHT = HEIGHT * HEIGHT_FACTOR

fun isHex(c: Char) = Character.isDigit(c) || allowed.contains(c)

//val WIDTH = 200
//val HEIGHT = 300

interface IGraphics {
    var computer: IComputer

    fun run()
}

fun createWindows(memory: IMemory, keyProvider: IKeyProvider): SwtContext {
    val display = Display()
    val shell = Shell(display).apply {
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
                keyProvider.keyPressed(memory, av)
            }
        }
        display.addFilter(SWT.Traverse) { e ->
            if (e.keyCode == 0xd && e.widget is Shell) {
                keyProvider.keyPressed(memory, e.keyCode)
                e.doit = false
            }
        }
    }

    //
    // Main screen of the emulator
    //

    val text1Window = TextWindow(mainContainer).apply {
        bounds = Rectangle(0, 0, ACTUAL_WIDTH, ACTUAL_HEIGHT)
    }
    text1Window.pack()

    //
    // Graphic screens
    //
    val hiResWindow = HiResWindow(0x2000, mainContainer).apply {
        bounds = Rectangle(0, 0, ACTUAL_WIDTH, ACTUAL_HEIGHT)
    }

    //
    // Middle panel, where the buttons live
    //
    val buttonContainer = Composite(shell, SWT.BORDER)
    val rebootButton = button(buttonContainer, "Reboot")
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
    val parentHeight = text1Window.bounds.height + 120
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
    shell.setSize(text1Window.bounds.width + folder.bounds.width, parentHeight)
    val result = SwtContext(display, shell, text1Window, hiResWindow)
    result.apply {
        rebootButton.addListener(SWT.Selection) { e ->
            computer.reboot()
        }
    }

    return result
}

fun createScrollableByteBuffer(parent: Composite): ScrolledComposite {
//    val composite = Composite(parent, SWT.NONE).apply {
//        layoutData = GridData(SWT.FILL, SWT.TOP, true, false)
//    }

    val result = ScrolledComposite(parent, SWT.BORDER or SWT.V_SCROLL or SWT.H_SCROLL).apply {
//        expandVertical = true
        background = black(display)
//        setMinSize(400, parentHeight)
//        layoutData = GridData(SWT.FILL, SWT.FILL, true, true, 1, 1)
    }

    val bb = ByteBufferWindow(result).let {
//        it.addListener(SWT.Resize) { event ->
////            val width: Int = clientArea.width
////            val s = computeSize(width, SWT.DEFAULT)
//            println("Resized to " + event.height)
////            setMinSize(s)
//        }
        it.pack()
        with(it.bounds) {
            result.setMinSize(Point(500, 4000))
//            result.size = Point(width, height)
//            height = parentHeight
        }
        result.content = it
        result.expandHorizontal = true
        result.expandVertical = true
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

//fun t(parent: Composite, parentHeight: Int): Composite {
//    val result = Composite(parent, SWT.NONE).apply {
//        layout = GridLayout(3, false)
//    }
//    label(result, "File:").apply {
//    }
//    label(result, "SomeDisk.woz").apply {
//        layoutData = GridData().apply {
//            horizontalSpan = 2
//            horizontalAlignment = GridData.FILL
//            grabExcessHorizontalSpace = true
//        }
//    }
////    label(result, "Rest").apply {
////        layoutData = GridData().apply {
////            background = blue(display)
////            horizontalAlignment = GridData.FILL
////            verticalAlignment = GridData.FILL
////            horizontalSpan = 3
////            grabExcessVerticalSpace = true
////        }
////    }
//    createScrollableByteBuffer(result, parentHeight).apply {
//        layoutData = GridData().apply {
//            horizontalAlignment = GridData.FILL
//            verticalAlignment = GridData.FILL
//            horizontalSpan = 3
//            grabExcessVerticalSpace = true
//        }
//
//    }
//    return result
//}

