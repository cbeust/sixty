package com.beust.sixty

import org.slf4j.LoggerFactory
import java.nio.file.*
import java.util.concurrent.TimeUnit

class FileWatcher {
    private val log = LoggerFactory.getLogger(FileWatcher::class.java)
    private val DIR = "D:\\pd\\Apple disks\\"
    var stop = false
    class WatchedFile(val filename: String, val address: Int)

    private val files = listOf(WatchedFile("watched.pic", 0x1ffc))

    fun run(memory: IMemory) {
        log.info("Starting FileWatcher")
        val watcher = FileSystems.getDefault().newWatchService()

        val dir = Paths.get(DIR)
        dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)
        while(! stop) {
            val key = watcher.poll(1, TimeUnit.SECONDS)
            key?.pollEvents()?.forEach { event ->
                if (event.kind() != StandardWatchEventKinds.OVERFLOW) {
                    val ev = event as WatchEvent<Path>
                    val filename = ev.context()

                    files.forEach { wf ->
                        if (filename.toString() == wf.filename) {
                            val file = Paths.get(dir.toAbsolutePath().toString(), filename.toString())
                            log.info("Reloading $file at address " + wf.address.h())
                            memory.load(file.toFile().inputStream().readAllBytes(), wf.address)
                        }
                        if (key.isValid) {
                            key.reset()
                        } else {
                            stop = true
                        }
                    }
                }
            }
        }
        watcher.close()
    }
}