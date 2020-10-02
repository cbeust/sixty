package com.beust.sixty

import org.slf4j.LoggerFactory

object Loggers {
    val def = LoggerFactory.getLogger("Default")
    val memory = LoggerFactory.getLogger("Memory")
    val text = LoggerFactory.getLogger("Text")
    val graphics = LoggerFactory.getLogger("Graphics")
    val disk = LoggerFactory.getLogger("Disk")
    val uiStatus = LoggerFactory.getLogger("UiStatus")
}

fun logMem(s: String) = Loggers.memory.debug("[MEM] $s")
fun logText(s: String) = Loggers.text.debug("[TEXT] $s")
fun logGraphics(s: String) = Loggers.graphics.debug("[GFX] $s")
fun logDisk(s: String) = Loggers.disk.debug("[DISK] $s")
fun logUiStatus(s: String) = Loggers.disk.debug("[UI] $s")
fun log(s: String) = Loggers.def.debug(s)

