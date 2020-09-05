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
            val debugMem = false
            apple2Computer(debugMem).run()//true, true)
        }
        else -> {
            val result = functionalTestComputer().run()
            with(result) {
                val sec = durationMillis / 1000
                val mhz = String.format("%.2f", cycles / sec / 1_000_000.0)
                println("Computer stopping after $cycles cycles, $sec seconds, $mhz MHz")
            }
        }
    }
}

