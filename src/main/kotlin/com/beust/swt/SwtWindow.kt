package com.beust.swt

import org.eclipse.jface.action.*
import org.eclipse.jface.preference.RadioGroupFieldEditor
import org.eclipse.jface.resource.FontDescriptor
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.jface.resource.JFaceResources
import org.eclipse.jface.resource.LocalResourceManager
import org.eclipse.jface.window.ApplicationWindow
import org.eclipse.swt.SWT
import org.eclipse.swt.events.KeyEvent
import org.eclipse.swt.events.KeyListener
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.*
import java.awt.event.KeyAdapter


private val allowed = hashSetOf('a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F')

private fun isHex(c: Char) = Character.isDigit(c) || allowed.contains(c)

val WIDTH = 200
val HEIGHT = 300

fun main() {
    runWithShell { display, shell ->
        shell.layout = FillLayout(SWT.HORIZONTAL)
        shell.addKeyListener(object : KeyAdapter(), KeyListener {
            override fun keyPressed(e: KeyEvent) {
                if (e.character == 'q') {
                    shell.dispose()
                }
            }
            override fun keyReleased(e: KeyEvent?) {
            }
        })
//        val mainScreen = Composite(shell, SWT.NONE).apply {
//            setSize(WIDTH, HEIGHT)
////            background = Color(display, 0, 0, 0)
//        }
        MainWindow(shell)
        val rightScreen = Composite(shell, SWT.NONE).apply {
            setSize(WIDTH, HEIGHT)
        }

        // create the manager and bind to a widget

        // create the manager and bind to a widget
        val resManager = LocalResourceManager(JFaceResources.getResources(), shell)
        var hexFont: Font = resManager.createFont(FontDescriptor.createFrom("Arial", 10, SWT.BOLD))

//        with(Text(shell, SWT.CENTER)) {
//            setSize(300, 200)
//            font = hexFont
//            text = "D5 AA 96"
//            addVerifyListener { e ->
//                if (isHex(e.character)) {
//                    e.doit = true
//                    e.text = e.text.toUpperCase()
//                } else {
//                    e.doit = false
//                }
//            }
//
////            pack()
//        }
//        HexText(shell)
    }
}

fun runWithShell(init: (display: Display, shell: Shell) -> Unit) {
    val display = Display()
    val shell = Shell(display)
    init(display, shell)
//    Ch3_Group(shell)

//    val helloText = Text(shell, SWT.CENTER)
//    helloText.text = "Hello SWT!"
//    helloText.pack()

    shell.pack();
    shell.open();
    while (!shell.isDisposed())
    {
        if (!display.readAndDispatch())
            display.sleep();
    }
    display.dispose();
//    SwtWindow()
}


class HexText(parent: Composite) : Composite(parent, SWT.NONE) {

    private fun buildControls() {
        layout = FillLayout()
        val text = Text(this, SWT.MULTI or SWT.V_SCROLL).apply {
            text = "Foo"
            setSelection(40, 30)
        }
        text.addVerifyListener { e ->
            if (isHex(e.character)) {
                e.doit = true
                e.text = e.text.toUpperCase()
            } else {
                e.doit = false
            }
        }
    }

    init {
        buildControls()
    }
}


class Ch4_Contributions : ApplicationWindow(null) {
    var slm = StatusLineManager()
    var status_action: Ch4_StatusAction = Ch4_StatusAction(slm)
    var aci = ActionContributionItem(status_action)
    override fun createContents(parent: Composite): Control {
        shell.text = "Action/Contribution Example"
        parent.setSize(290, 150)
        aci.fill(parent)
        return parent
    }

    override fun createMenuManager(): MenuManager {
        val main_menu = MenuManager(null)
        val action_menu = MenuManager("Menu")
        main_menu.add(action_menu)
        action_menu.add(status_action)
        return main_menu
    }

    override fun createToolBarManager(style: Int): ToolBarManager {
        val tool_bar_manager = ToolBarManager(style)
        tool_bar_manager.add(status_action)
        return tool_bar_manager
    }

    override fun createStatusLineManager(): StatusLineManager {
        return slm
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val swin = Ch4_Contributions()
            swin.setBlockOnOpen(true)
            swin.open()
            Display.getCurrent().dispose()
        }
    }

    init {
        addStatusLine()
        addMenuBar()
        addToolBar(SWT.FLAT or SWT.WRAP)
    }
}

class Ch4_StatusAction(var statman: StatusLineManager) : Action("&Trigger@Ctrl+T", AS_PUSH_BUTTON) {
    var triggercount: Short = 0
    override fun run() {
        triggercount++
        statman.setMessage("The status action has fired. Count: " +
                triggercount)
    }

    init {
        toolTipText = "Trigger the Action"
        imageDescriptor = ImageDescriptor.createFromFile(this.javaClass, "eclipse.gif")
    }
}

class Ch3_Group(parent: Composite) : Composite(parent, SWT.NONE) {
    init {
        val group = Group(this, SWT.SHADOW_ETCHED_IN)
        group.setText("Group Label")
        val label = Label(group, SWT.NONE)
        label.setText("Two buttons:")
        label.setLocation(20, 20)
        label.pack()
        val button1 = Button(group, SWT.PUSH)
        button1.text = "Push button"
        button1.setLocation(20, 45)
        button1.pack()
        val button2 = Button(group, SWT.CHECK)
        button2.text = "Check button"
        button2.setBounds(20, 75, 90, 30)
        group.pack()
    }
}


class SwtWindow : ApplicationWindow(null) {
    override fun createContents(parent: Composite): Control {
        val rgfe = RadioGroupFieldEditor(
                "UserChoice", "Choose an option:", 1,
                arrayOf(arrayOf("Choice1", "ch1"), arrayOf("Choice2", "ch2"), arrayOf("Choice3", "ch3")),
                shell, true)
        Ch3_Group(parent)
        return parent
//        // Create a Hello, World label
//        with(Button(parent, SWT.CHECK or SWT.CENTER)) {
//            parent!!.setSize(300, 200)
//            setSize(100, 40)
//
//            text = "www.java2s.com"
//            return this
//        }
    }

    init {
        // Don't return from open() until window closes
        setBlockOnOpen(true)

        // Open the main window
        open()

        // Dispose the display
        Display.getCurrent().dispose()
    }
}
