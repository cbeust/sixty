package com.beust.sixty

import org.slf4j.LoggerFactory

object Loggers {
    val def = LoggerFactory.getLogger("Default")
    val memory = LoggerFactory.getLogger("Memory")
    val text = LoggerFactory.getLogger("Text")
    val graphics = LoggerFactory.getLogger("Graphics")
}

fun logMem(s: String) = Loggers.memory.debug(s)
fun logText(s: String) = Loggers.text.debug(s)
fun logGraphics(s: String) = Loggers.graphics.debug(s)
fun log(s: String) = Loggers.def.debug(s)

