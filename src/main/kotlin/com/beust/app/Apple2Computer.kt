package com.beust.app

import com.beust.sixty.*

class Apple2Computer(private val gc: GraphicContext? = null): IComputer {
    override var memory = Apple2Memory()
    override val cpu: Cpu
        get() = computer.cpu
    var computer: IComputer
    val diskController: DiskController

    init {
        gc?.let { g ->
            val memoryListener = Apple2MemoryListener({ -> memory }, g.textWindow, g.hiResWindow)
            memory.listeners.add(memoryListener)
        }

        diskController = DiskController(6).apply {
            loadDisk(IDisk.create(UiState.diskStates[0].file.value), 0)
            UiState.diskStates[0].file.addListener { _, new -> loadDisk(IDisk.create(new), 0) }
            UiState.diskStates[1].file.addListener { _, new -> loadDisk(IDisk.create(new), 1) }
        }
        memory.listeners.add(diskController)

        val a2Memory = memory
        computer = Computer.create {
            memory = a2Memory
        }.build()
        computer.cpu.PC = memory.word(0xfffc)
    }

    override fun reboot() {
        computer.reboot()
        computer.cpu.PC = memory.word(0xfffc)
    }

    override fun step() : Computer.RunStatus {
        var result = Computer.RunStatus.RUN
        diskController.step()
        result = computer.step()
        diskController.step()
        cycles++

        return result
    }
}

//private fun runWatcher(memory: IMemory) {
//    val watcher = FileSystems.getDefault().newWatchService()
//
//    val dir = Paths.get("asm")
//    dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)
//    var done = false
//    while(! false) {
//        val key = watcher.take()
//        key.pollEvents().forEach { event ->
//            if (event.kind() != StandardWatchEventKinds.OVERFLOW) {
//                val ev = event as WatchEvent<Path>
//                val filename = ev.context()
//                if (filename.toString() == "a") {
//                    val file = Paths.get(dir.toAbsolutePath().toString(), filename.toString())
//                    println("Reloading $file")
//                    memory.load(file.toFile().inputStream().readAllBytes(), 0x300)
//                }
//                println("$filename modified")
//                if (key.isValid) {
//                    key.reset()
//                } else {
//                    done = true
//                }
//            }
//        }
//    }
//}
