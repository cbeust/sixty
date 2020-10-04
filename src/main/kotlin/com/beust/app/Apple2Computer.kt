package com.beust.app

import com.beust.sixty.*

class Apple2Computer: IPulse {
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
            UiState.currentDisk2File.value?.let { disk ->
                loadDisk(IDisk.create(disk), 1)
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

    override fun onPulse(manager: PulseManager): PulseResult = computer.onPulse(manager)

    override fun stop() {
        TODO("Not yet implemented")
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
