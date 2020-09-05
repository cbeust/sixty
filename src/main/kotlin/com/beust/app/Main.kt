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
//    println("Running a 6502 program displaying HELLO")
//    val c = TestComputer.createComputer()
//    c.run(debugAsm = false)
    apple2Computer().run(true, true)
//    val result = functionalTestComputer().run()
//    with(result) {
//        val sec = durationMillis / 1000
//        val mhz = String.format("%.2f", cycles / sec / 1_000_000.0)
//        println("Computer stopping after $cycles cycles, $sec seconds, $mhz MHz")
//    }

}

