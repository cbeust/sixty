package com.beust.sixty

import org.slf4j.LoggerFactory

object Loggers {
    val def = LoggerFactory.getLogger("Default")
    val memory = LoggerFactory.getLogger("Memory")
}

fun logMem(s: String) = Loggers.memory.debug(s)
fun log(s: String) = Loggers.def.debug(s)
