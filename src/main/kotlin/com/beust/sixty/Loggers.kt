package com.beust.sixty

import org.slf4j.LoggerFactory

object Loggers {
    val memory = LoggerFactory.getLogger("Memory")
}

fun logMem(s: String) = Loggers.memory.debug(s)

