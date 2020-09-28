package com.beust.swt

import com.beust.app.BitPattern
import com.beust.app.Board
import com.beust.app.SColor
import com.beust.sixty.IMemory
import com.beust.sixty.hh
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.GC
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Canvas
import org.eclipse.swt.widgets.Composite
import java.util.*

class HiResWindow(parent: Composite, style: Int = SWT.NONE): Composite(parent, style) {
    companion object {
        const val WIDTH = 280
        const val HEIGHT = 192
    }

    /**
     * 2000-2027
     */
    private val consecutives = listOf(0, 0x400, 0x800, 0xc00, 0x1000, 0x1400, 0x1800, 0x1c00)
    private val interleaving = listOf(
            0, 0x80, 0x100, 0x180, 0x200, 0x280, 0x300, 0x380,
            0x28, 0xa8, 0x128, 0x1a8, 0x228, 0x2a8, 0x328, 0x3a8,
            0x50, 0xd0, 0x150, 0x1d0, 0x250, 0x2d0, 0x350, 0x3d0)
    private val lineMap = hashMapOf<Int, Int>()
//    private val board: Board
    private val canvas: Canvas

    private val content = Array(WIDTH * HEIGHT) { SColor.BLACK }
    private fun index(x: Int, y: Int) = y * WIDTH + x
    private val FACTOR = 2
    private val blockWidth = FACTOR
    private val blockHeight = FACTOR
    class Command(val x: Int, val y: Int, val width: Int, val height: Int, val color: SColor)
    val lock = "commands"
    val commands = ArrayList<Command>()


    init {
        layout = GridLayout()
        background = white(display)
        canvas = Canvas(this, SWT.NO_REDRAW_RESIZE).apply {
            background = black(display)
            layoutData = GridData().apply {
                widthHint = WIDTH * FACTOR
                heightHint = HEIGHT * FACTOR
            }
        }
        canvas.addPaintListener { e ->
            synchronized(lock) {
                while (commands.isNotEmpty()) {
                    commands.removeAt(0)?.let { cmd ->
                        val c = content[index(cmd.x / FACTOR, cmd.y / FACTOR)].toSwtColor(display)
                        e.gc.background = c
                        e.gc.fillRectangle(cmd.x, cmd.y, cmd.width, cmd.height)
                    }
                }
            }
        }
//        Timer().schedule(object: TimerTask() {
//            override fun run() {
//                display.asyncExec {
//                    canvas.redraw()
//                }
//            }
//        }, 0, 10)
//
//        addPaintListener { e ->
////            GC(this).let { g ->
//                gc.background = blue(display)
//                gc.drawLine(0, 0, 100, 100)
//            }
//        }

        var l = 0
        interleaving.forEach { il ->
            consecutives.forEach { c ->
                lineMap[il + c] = l++
            }
        }
    }
//    fun drawMemoryLocation(memory: IMemory, location: Int) {}

    fun drawMemoryLocation(memory: IMemory, location: Int) {
        val even = location % 2 == 0
        val bitPattern = if (even) {
            val byte0 = memory[location]
            val byte1 = memory[location + 1]
            BitPattern(byte0, byte1)
        } else {
            // DD BB
            // 1101_1101  1011_1011
            // aa:1 bb:3 cc:1 dd:3 ee:1 ff:3 gg: 1
            val byte0 = memory[location - 1]
            val byte1 = memory[location]
            BitPattern(byte0, byte1)
        }

        //
        // Calculate x,y
        //
        val evenLocation = if (even) location else location - 1
        val loc = evenLocation - 0x2000
        var closest = Integer.MAX_VALUE
        var key = -1
        lineMap.keys.forEach { k ->
            val distance = loc - k
            if (distance in 0 .. closest) {
                closest = distance
                key = k
            }
        }
        val y = lineMap[key]
        val x = (loc - key) * 7

        var i = 0
//        println("=== Location " + location.toHex() + " at $x, $y")
//        if (bitPattern.colors().contains(Color.ORANGE)) {// && x >=140.0) {
//            println("PROBLEM")
//        }

        fun drawPixel(x: Int, y: Int, color: SColor) {
            if (x < WIDTH) {
                content[index(x, y)] = color
                synchronized(lock) {
                    commands.add(Command(x * FACTOR, y * FACTOR, FACTOR, FACTOR, color))
                }
                canvas.display.asyncExec { canvas.redraw() }
            }
        }

        y!!

        repeat(2) { drawPixel(x + i++, y, BitPattern.color(bitPattern.p0, bitPattern.aa, x)) }
        repeat(2) { drawPixel(x + i++, y, BitPattern.color(bitPattern.p0, bitPattern.bb, x + 1)) }
        repeat(2) { drawPixel(x + i++, y, BitPattern.color(bitPattern.p0, bitPattern.cc, x + 2)) }
        repeat(2) {
            drawPixel(x + i++, y, BitPattern.color(if (even) bitPattern.p0 else bitPattern.p1,
                    bitPattern.dd, x + 3))
        }
        repeat(2) { drawPixel(x + i++, y, BitPattern.color(bitPattern.p1, bitPattern.ee, x + 4)) }
        repeat(2) { drawPixel(x + i++, y, BitPattern.color(bitPattern.p1, bitPattern.ff, x + 5)) }
        repeat(2) { drawPixel(x + i++, y, BitPattern.color(bitPattern.p1, bitPattern.gg, x + 6)) }
    }
}