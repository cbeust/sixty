import com.beust.app.*
import com.beust.app.app.ITextScreen

import com.beust.sixty.*
import com.beust.swt.*
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import java.nio.file.*

class GraphicContext(val computer: () -> A2Computer, memory: () -> Apple2Memory) {
    val hiResWindow: HiResWindow
    val textScreen: TextWindow
    private val display: Display = Display()
    private val shell: Shell

    fun clear() {
        hiResWindow.clear()
        textScreen.clear()
    }

    private val keyProvider = object: IKeyProvider {
        override fun keyPressed(memory: IMemory, value: Int, shift: Boolean, control: Boolean) {
            memory.forceInternalRomValue(0xc000, value.or(0x80))
            memory.forceInternalRomValue(0xc010, 0x80)
        }
    }

    private fun maybeResize(control: Control) {
        if (control == hiResWindow) {
            control.display.asyncExec {
                val fullHeight = ACTUAL_HEIGHT
                val shortHeight = ACTUAL_HEIGHT * 5 / 6
                val b = control.bounds
                val newHeight = if (UiState.mainScreenMixed.value) shortHeight else fullHeight
                control.setBounds(b.x, b.y, b.width, newHeight)
                control.parent.layout()
            }
        }
    }

    private fun show(control: Control) {
        if (! control.isDisposed) with(control) {
            display.asyncExec {
                maybeResize(control)
                control.moveAbove(null)
            }
        }
    }

    fun run() {
        shell.open()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) display.sleep()
        }
        hiResWindow.stop()
//        display.dispose()
    }

    init {
        shell = Shell(display).apply {
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
                    keyProvider.keyPressed(memory(), av)
                }
            }
            display.addFilter(SWT.Traverse) { e ->
                if (e.keyCode == 0xd && e.widget is Shell) {
                    keyProvider.keyPressed(memory(), e.keyCode)
                    e.doit = false
                }
            }
        }

        //
        // Main screen of the emulator
        //

        textScreen = TextWindow(mainContainer, 0x400).apply {
            bounds = Rectangle(0, 0, ACTUAL_WIDTH, ACTUAL_HEIGHT)
        }.apply {
            pack()
        }

        //
        // Graphic screens
        //
        hiResWindow = HiResWindow(0x2000, mainContainer).apply {
            bounds = Rectangle(0, 0, ACTUAL_WIDTH, ACTUAL_HEIGHT)
        }

        //
        // Middle panel, where the buttons live
        //
        val buttonContainer = Composite(shell, SWT.BORDER)
        val rebootButton = button(buttonContainer, "Reboot").apply {
            addListener(SWT.Selection) { e ->
                computer().reboot()
            }
        }

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
        val parentHeight = textScreen.bounds.height + 120
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
        shell.setSize(textScreen.bounds.width + folder.bounds.width, parentHeight)
//        rebootButton.addListener(SWT.Selection) { e ->
//            computer.reboot()
//        }

        UiState.mainScreenHires.addListener { _, new ->
            if (new) show(hiResWindow)
        }
        UiState.mainScreenPage2.addAfterListener { _, _ ->
            if (!memory().store80On) {
                if (UiState.mainScreenText.value) {
                    show(textScreen)
                } else {
                    show(hiResWindow)
                }
            }
        }
        UiState.mainScreenText.addListener { _, new ->
            if (new) show(textScreen)
        }
        UiState.mainScreenMixed.addAfterListener { _, new ->
            maybeResize(hiResWindow)
        }
    }
}

class A2Computer: IPulse {
    val pulseListeners = arrayListOf<IPulse>()
    val memoryListeners = arrayListOf<MemoryListener>()
    var memory: Apple2Memory
    var computer: IComputer
    
    init {
        memory = Apple2Memory()
        val dc = DiskController(6).apply {
            UiState.currentDisk1File.value?.let { disk ->
                loadDisk(IDisk.create(disk), 0)
            }
        }
        pulseListeners.add(dc)
        pulseListeners.add(this)
        memoryListeners.add(dc)

        val a2Memory = memory
        computer = Computer.create {
            memory = a2Memory
        }.build()
        computer.cpu.PC = memory.word(0xfffc)
    }

    fun reboot() {
        computer.reboot()
        computer.cpu.PC = memory.word(0xfffc)
    }

    override fun onPulse(manager: PulseManager): PulseResult {
        return computer.onPulse(manager)
    }

    override fun stop() {
        TODO("Not yet implemented")
    }
}

//class Apple2Computer() {
//    fun run(pulseManager: PulseManager): Pair<SwtContext?, IComputer> {
//        val dc = DiskController(6).apply {
//            loadDisk(IDisk.create(DISK), 0)
//            UiState.currentDisk1File.value = DISK
//        }
//        val keyProvider = object: IKeyProvider {
//            override fun keyPressed(memory: IMemory, value: Int, shift: Boolean, control: Boolean) {
//                memory.forceInternalRomValue(0xc000, value.or(0x80))
//                memory.forceInternalRomValue(0xc010, 0x80)
//            }
//        }
//
//        pulseManager.addListener(dc)
//        val a2Memory = Apple2Memory()
//        val swtContext = createWindows( { -> a2Memory }, keyProvider)
//
//        fun maybeResize(control: Control) {
//            if (control == swtContext.hiResWindow) {
//                control.display.asyncExec {
//                    val fullHeight = ACTUAL_HEIGHT
//                    val shortHeight = ACTUAL_HEIGHT * 5 / 6
//                    val b = control.bounds
//                    val newHeight = if (UiState.mainScreenMixed.value) shortHeight else fullHeight
//                    control.setBounds(b.x, b.y, b.width, newHeight)
//                    control.parent.layout()
//                }
//            }
//        }
//
//        fun show(control: Control) {
//            if (! control.isDisposed) with(control) {
//                display.asyncExec {
//                    maybeResize(control)
//                    swtContext.show(this)
//                }
//            }
//        }
//
//        UiState.mainScreenHires.addListener { _, new ->
//            if (new) show(swtContext.hiResWindow)
//        }
//        UiState.mainScreenPage2.addAfterListener { _, _ ->
//            if (!a2Memory.store80On) {
//                if (UiState.mainScreenText.value) {
//                    show(swtContext.textScreen)
//                } else {
//                    show(swtContext.hiResWindow)
//                }
//            }
//        }
//        UiState.mainScreenText.addListener { _, new ->
//            if (new) show(swtContext.textScreen)
//        }
//        UiState.mainScreenMixed.addAfterListener { _, new ->
//            maybeResize(swtContext.hiResWindow)
//        }
//
//        val textPanel1 = ITextScreen(0x400)
//
//        val computer = Computer.create {
//            memory = a2Memory
//            memoryListeners.add(Apple2MemoryListener(a2Memory, textPanel1, swtContext.hiResWindow))
//            memoryListeners.add(dc)
//        }.build()
//        swtContext.computer = computer
//        val start = a2Memory.word(0xfffc) // memory[0xfffc].or(memory[0xfffd].shl(8))
//        computer.cpu.PC = start
////        loadPic(a2Memory)
//
//        return swtContext to computer
//    }
//
//    private fun loadPic(memory: IMemory) {
////    val bytes = Paths.get("d:", "PD", "Apple disks", "fishgame.pic").toFile().readBytes()
////    (4..0x2004).forEach {
////        memory[0x2000 + it - 4] = bytes[it].toInt()
////    }
//    }
//
//}

fun createApple2Memory(): Apple2Memory {
    val result = Apple2Memory(65536).apply {

//        load("d:\\pd\\Apple Disks\\roms\\APPLE2E.ROM", 0xc000)
//        load("d:\\pd\\Apple Disks\\roms\\C000.dump", 0xc000)
        loadResource("Apple2e.rom", 0xd000, 0x1000, 0x3000)

        val bytes = this::class.java.classLoader.getResource("Apple2e.rom").openStream().readAllBytes()

        loadCxxxInInternal(bytes, 0x100, 0xeff, 0x100)

        internalCxRom = false
        slotC3Rom = true
        // C600 in slot
        loadResource("DISK2.ROM", 0xc600)

        // Reset
        internalCxRom = false
        slotC3Rom = false
//        slotC3WasReset = false
        internalC8Rom = false
        Thread {
            runWatcher(this)
        }.start()

        // When restarting, no need to move the head 0x50 tracks
//        this[0xc63c] = 4
    }

    return result
}

//class Apple2Computer(val diskController: DiskController): IComputer, IPulse {
//    var pcListener: PcListener? = null
//    override val memory: IMemory = createApple2Memory()
//    override val cpu : Cpu = Cpu(memory)
//
//    private val computer = Computer(memory, cpu, pcListener)
//
//    override fun onPulse(manager: PulseManager) = computer.onPulse(manager)
//    override fun stop() = computer.stop()
//}

private fun runWatcher(memory: IMemory) {
    val watcher = FileSystems.getDefault().newWatchService()

    val dir = Paths.get("asm")
    dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)
    var done = false
    while(! false) {
        val key = watcher.take()
        key.pollEvents().forEach { event ->
            if (event.kind() != StandardWatchEventKinds.OVERFLOW) {
                val ev = event as WatchEvent<Path>
                val filename = ev.context()
                if (filename.toString() == "a") {
                    val file = Paths.get(dir.toAbsolutePath().toString(), filename.toString())
                    println("Reloading $file")
                    memory.load(file.toFile().inputStream().readAllBytes(), 0x300)
                }
                println("$filename modified")
                if (key.isValid) {
                    key.reset()
                } else {
                    done = true
                }
            }
        }
    }
}
