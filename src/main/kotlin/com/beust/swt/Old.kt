package com.beust.swt

import org.eclipse.jface.action.*
import org.eclipse.jface.preference.RadioGroupFieldEditor
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.jface.viewers.TableLayout
import org.eclipse.jface.window.ApplicationWindow
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.CTabFolder
import org.eclipse.swt.custom.CTabItem
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.layout.RowLayout
import org.eclipse.swt.widgets.*


private const val STYLE = SWT.BORDER or SWT.H_SCROLL or SWT.V_SCROLL

fun main() {
    works()
}

fun createScrollableByteBuffer(parent: Composite): Composite {
    return ScrolledComposite(parent, SWT.V_SCROLL).let { scroller ->
        val bb = ByteBufferTab(scroller)
        scroller.apply {
            layout = FillLayout()
            minHeight = bb.computeSize(SWT.DEFAULT, SWT.DEFAULT).y
            content = bb
        }
    }
}

fun works() {
    val display = Display()
    val shell = Shell(display)
    shell.text = "ScrolledComposite in 'scroll' and 'browser' mode"
    shell.layout = FillLayout()

    val folder = CTabFolder(shell, SWT.TOP)

    val tab = CTabItem(folder, SWT.NONE).apply {
        text = "DISK"
        control = createScrollableByteBuffer(folder)
    }

    shell.setSize(600, 100)
    shell.open()
    while (!shell.isDisposed) {
        if (!display.readAndDispatch()) display.sleep()
    }
    display.dispose()

}

fun multipleWays() {
    val display = Display()
    val red = display.getSystemColor(SWT.COLOR_RED)
    val blue = display.getSystemColor(SWT.COLOR_BLUE)
    val shell = Shell(display)
    shell.layout = FillLayout()

    // set the size of the scrolled content - method 1

    // set the size of the scrolled content - method 1
    val sc1 = ScrolledComposite(shell, SWT.H_SCROLL or SWT.V_SCROLL or SWT.BORDER)
    val c1 = Composite(sc1, SWT.NONE)
    sc1.content = c1
    c1.background = red
    var layout = GridLayout()
    layout.numColumns = 4
    c1.layout = layout
    val b1 = Button(c1, SWT.PUSH)
    b1.text = "first button"
    c1.size = c1.computeSize(SWT.DEFAULT, SWT.DEFAULT)

    // set the minimum width and height of the scrolled content - method 2

    // set the minimum width and height of the scrolled content - method 2
    val sc2 = ScrolledComposite(shell, SWT.H_SCROLL or SWT.V_SCROLL or SWT.BORDER)
    sc2.expandHorizontal = true
    sc2.expandVertical = true
    val c2 = Composite(sc2, SWT.NONE)
    sc2.content = c2
    c2.background = blue
    layout = GridLayout()
    layout.numColumns = 4
    c2.layout = layout
    val b2 = Button(c2, SWT.PUSH)
    b2.text = "first button"
    sc2.setMinSize(c2.computeSize(SWT.DEFAULT, SWT.DEFAULT))

    val add = Button(shell, SWT.PUSH)
    add.text = "add children"
    val index = intArrayOf(0)
    add.addListener(SWT.Selection) {
        index[0]++
        var button = Button(c1, SWT.PUSH)
        button.text = "button " + index[0]
        // reset size of content so children can be seen - method 1
        c1.size = c1.computeSize(SWT.DEFAULT, SWT.DEFAULT)
        c1.layout()
        button = Button(c2, SWT.PUSH)
        button.text = "button " + index[0]
        // reset the minimum width and height so children can be seen - method 2
        sc2.setMinSize(c2.computeSize(SWT.DEFAULT, SWT.DEFAULT))
        c2.layout()
    }

    shell.setSize(300, 400)
//    shell.pack()
    shell.open()
    while (!shell.isDisposed) {
        if (!display.readAndDispatch()) display.sleep()
    }
    display.dispose()
}

fun simple() {
    val display = Display()
    val shell = Shell(display)
    shell.layout = TableLayout()

    button(shell, "Test button").apply {
        setSize(200, 600)
    }
    shell.pack()
    shell.setSize(300, 400)
    shell.open()
    while (!shell.isDisposed) {
        if (!display.readAndDispatch()) display.sleep()
    }
    display.dispose()
}

fun rowLayout() {
    val display = Display()
    val shell = Shell(display)
    // Create the layout.
    // Create the layout.
    val layout = RowLayout()
    // Optionally set layout fields.
    // Optionally set layout fields.
    layout.wrap = true
    // Set the layout into the composite.
    // Set the layout into the composite.
    shell.layout = layout
    // Create the children of the composite.
    // Create the children of the composite.
    Button(shell, SWT.PUSH).text = "B1"
    Button(shell, SWT.PUSH).text = "Wide Button 2"
    Button(shell, SWT.PUSH).text = "Button 3"
    shell.pack()
    shell.setSize(300, 200)
    shell.open()

    while (!shell.isDisposed) {
        if (!display.readAndDispatch()) display.sleep()
    }
}

class ByteBuffer {
    fun run() {
        val display = Display()
        val shell = Shell(display)
        createContents(shell)
        shell.setSize(300, 300)
        shell.open()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) {
                display.sleep()
            }
        }
        display.dispose()
    }

    private fun createContents(parent: Composite) {
        parent.layout = FillLayout()
        val sc = ScrolledComposite(parent, SWT.H_SCROLL or SWT.V_SCROLL)

        // Create a child composite to hold the controls
        val child = Composite(sc, SWT.NONE)
        child.layout = FillLayout()

        val bb = ByteBufferTab(child)
        // Create the buttons
//        Button(child, SWT.PUSH).text = "One"
//        Button(child, SWT.PUSH).text = "Two"
        /*
     * // Set the absolute size of the child child.setSize(400, 400);
     */
        // Set the child as the scrolled content of the ScrolledComposite
        sc.content = child

        // Set the minimum size
        sc.setMinSize(500, bb.size.y)

        // Expand both horizontally and vertically
        sc.expandHorizontal = true
        sc.expandVertical = true
    }
}


fun main2() {
    ByteBuffer().run()
//    runWithShell { display, shell ->
//        shell.setSize(300, 200)
////        shell.pack()
//        val container = ScrolledComposite(shell, SWT.NONE).apply {
//            layout = GridLayout()
//            setSize(300, 200)
//            content = shell
//        }
//        repeat(50) {
//            val label = label(container, "Test").apply {
//                background = blue(display)
//            }
//        }
//////        createContents2(shell)
////        shell.layout = GridLayout(1, true)
////        shell.layoutData = GridData().apply {
////            heightHint = 100
////        }
////        shell.pack()
//    }
}


class Works {
    fun run() {
        val display = Display()
        val shell = Shell(display)
        createContents(shell)
//        shell.setSize(300, 300)
        shell.open()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) {
                display.sleep()
            }
        }
        display.dispose()
    }

    private fun createContents(parent: Composite) {
        parent.layout = FillLayout()

        // Create the ScrolledComposite to scroll horizontally and vertically
        val sc = ScrolledComposite(parent, SWT.H_SCROLL
                or SWT.V_SCROLL)

        // Create a child composite to hold the controls
        val child = Composite(sc, SWT.NONE)
        child.layout = FillLayout()

        // Create the buttons
        Button(child, SWT.PUSH).text = "One"
        Button(child, SWT.PUSH).text = "Two"
        /*
     * // Set the absolute size of the child child.setSize(400, 400);
     */
        // Set the child as the scrolled content of the ScrolledComposite
        sc.content = child

        // Set the minimum size
        sc.setMinSize(500, 500)

        // Expand both horizontally and vertically
        sc.expandHorizontal = true
        sc.expandVertical = true
    }
}


private fun createContents2(parent: Composite) {
//    val sc = ScrolledComposite(parent, SWT.H_SCROLL or SWT.V_SCROLL).apply {
//        expandHorizontal = true
//        expandVertical = true
////        setSize(300, 100)
////        setMinSize(300, 100)
//    }
    val child = Composite(parent, SWT.NONE).apply {
        layout = GridLayout(1, true)
        background = yellow(display)
    }
    repeat(50) {
        val label = label(child, "Test").apply {
            background = blue(display)
        }
    }
//    sc.content = child
    child.setSize(300, 100)
    child.pack()
//    sc.pack()
}

private fun createContents(parent: Composite) {
    parent.layout = FillLayout()

    // Create the ScrolledComposite to scroll horizontally and vertically
    val sc = ScrolledComposite(parent, SWT.H_SCROLL
            or SWT.V_SCROLL)

    // Create a child composite to hold the controls
    val child = Composite(sc, SWT.NONE)
    child.layout = FillLayout()

    // Create the buttons
    Button(child, SWT.PUSH).apply {
        setSize(300, 1000)
        text = "One"
    }
    Button(child, SWT.PUSH).text = "Two"
    /*
     * // Set the absolute size of the child child.setSize(400, 400);
     */
    // Set the child as the scrolled content of the ScrolledComposite
    sc.content = child

    // Set the minimum size
//    sc.setMinSize(400, 400)

    // Expand both horizontally and vertically
    sc.expandHorizontal = true
    sc.expandVertical = true
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
