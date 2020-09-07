package com.beust.app

import com.beust.sixty.*
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.nio.file.Paths
import kotlin.system.exitProcess

val DEBUG = false

fun main() {
    val choice = 2

    when(choice) {
        1 -> {
            println("Running the following 6502 program which will display HELLO")
            val c = TestComputer.createComputer()
            c.disassemble(start = 0, length = 15)
            c.run(debugAsm = false)
        }
        2 -> {
            val d = true
            val debugMem = DEBUG
            val debugAsm = DEBUG
            apple2Computer(debugMem).run(debugMem, debugAsm)//true, true)
        }
        else -> {
            val result = functionalTestComputer(false).run()//true, true)
            with(result) {
                val sec = durationMillis / 1000
                val mhz = String.format("%.2f", cycles / sec / 1_000_000.0)
                println("Computer stopping after $cycles cycles, $sec seconds, $mhz MHz")
            }
        }
    }
}

