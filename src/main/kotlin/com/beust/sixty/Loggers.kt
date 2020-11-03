package com.beust.sixty

import com.beust.app.DEBUG
import com.beust.app.DEBUG_ASM
import org.slf4j.LoggerFactory

object Loggers {
    val def = LoggerFactory.getLogger("Default")
    val memory = LoggerFactory.getLogger("Memory")
    val text = LoggerFactory.getLogger("Text")
    val graphics = LoggerFactory.getLogger("Graphics")
    val disk = LoggerFactory.getLogger("Disk")
    val uiStatus = LoggerFactory.getLogger("UiStatus")
    val asm = LoggerFactory.getLogger("Asm")
    val woz = LoggerFactory.getLogger("Woz")
    val asmTrace = LoggerFactory.getLogger("AsmTrace")
}

fun cycles(s: String) = String.format("%08X| $s", Cycles.cycles)

fun logMem(s: String) = Loggers.memory.debug("[MEM] $s")
fun logText(s: String) = Loggers.text.debug("[TEXT] $s")
fun logGraphics(s: String) = Loggers.graphics.debug("[GFX] $s")
fun logDisk(s: String) = Loggers.disk.debug("[DISK] $s")
fun logTraceDisk(s: String) = Loggers.disk.trace("[DISK] $s")
fun logUiStatus(s: String) = Loggers.uiStatus.debug("[UI] $s")
fun logAsm(s: String) { if (DEBUG_ASM) { Loggers.asm.debug(cycles(s)) } }
fun logWoz(s: String) = Loggers.woz.debug(s)
fun logNyi(s: String) = Loggers.def.warn("!!!!!!!!!!! Not yet implemented: $s")
fun log(s: String) { if (DEBUG) Loggers.def.debug(cycles(s)) }
fun warn(s: String) { Loggers.def.warn(cycles(s)) }
fun logAsmTrace(s: String) = Loggers.asmTrace.debug(cycles(s))
