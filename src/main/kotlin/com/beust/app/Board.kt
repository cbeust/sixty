package com.beust.app

import org.eclipse.swt.graphics.GC
import org.eclipse.swt.widgets.Control

class Board(private val control: Control) {
    private val DEFAULT = SColor.BLACK
    private val blockWidth = 2
    private val blockHeight = 2
    private val gap = 0
    private val WIDTH = 280
    private val HEIGHT = 192

    private fun index(x: Int, y: Int) = y * WIDTH + x

//    fun redraw(g: GC) {
//        repeat(HEIGHT) { y ->
//            repeat(WIDTH) { x ->
//                g.background = content[index(x, y)].toSwtColor(display)
//                val xx = x * (blockWidth + gap)
//                val yy = y * (blockHeight + gap)
//                g.fillRectangle(xx, yy, blockWidth, blockHeight)
//            }
//        }
//    }

    class Command(val x: Int, val y: Int, val width: Int, val height: Int, val color: SColor)
    val commands = ArrayList<Command>()

    fun redraw(gc: GC) {
        if (commands.isEmpty()) {
            println("Redraw all")
        } else while (commands.isNotEmpty()) {
            commands.removeAt(0).let { c ->
                gc.background = c.color.toSwtColor(control.display)
//                    println("Queuing " + c.x + "," + c.y + ": " + c.color)
//                    gc.drawPoint(c.x, c.y)
                gc.fillRectangle(c.x, c.y, c.width, c.height)
            }
        }
    }

    fun draw(x: Int, y: Int, color: SColor) {
        if (index(x,y) >= WIDTH*HEIGHT) {
            println("ERROR")
        }

        val xx = x * (blockWidth + gap)
        val yy = y * (blockHeight + gap)
        commands.add(Command(xx, yy, blockWidth, blockHeight, color))
        
//        control.display.asyncExec {
//            control.redraw()
//        }
//        gc.fillRectangle(xx, yy, blockWidth, blockHeight)
//        control.redraw()
    }
}