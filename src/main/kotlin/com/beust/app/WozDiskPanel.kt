package com.beust.app

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.cos
import kotlin.math.sin

class WozDiskPanel(val disk: WozDisk) : JPanel() {
    companion object {
        fun run(disk: WozDisk) {
            val f = JFrame().apply {
                setSize(1000, 1000)
                defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                contentPane.add(WozDiskPanel(disk).apply {
                    setSize(1000, 1000)
                }, BorderLayout.CENTER)
                isVisible = true
            }
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.color = Color.black
        var radius = 200
        val X = width / 2
        val Y = height / 2

//        generateSequence(0.0, {it + 0.01} )
//                .takeWhile { it <= Math.PI * 2 }
//                .forEach {
//                    val x = Math.cos(it) * radius + X
//                    val y = Math.sin(it) * radius + Y
//                    g.drawRect(x.toInt(), y.toInt(), 1, 1)
//                }
        g.color = Color.red
        g.drawOval(100, 100, 50, 50)


//        val map = disk.woz.tmap
//        repeat(160) { trackNumber ->
//            radius--
//            var circumference = 200 * Math.PI * radius
//            val offset = map.offsetFor(trackNumber)
//            if (offset != -1) {
//                val track = disk.woz.trks.trks[offset]
//                val bitCount = track.bitCount
//                val byteCount = bitCount / 8
//                val bitRatio = circumference / bitCount
//                val byteRatio = circumference / byteCount
//
//                var angle = 0.0
//                val step = Math.PI * 2 * radius / circumference
//                var bitsSoFar = 0.0
//                repeat(circumference.toInt()) { circ ->
//                    val new = bitsSoFar + bitRatio
//                    val allowed = (new - bitsSoFar).toInt()
//                    bitsSoFar = new
//                    val bits = arrayListOf<Int>()
//                    repeat(allowed) {
//                        bits.add(disk.nextBit())
//                    }
//
//                    g.color = bitsToColor(bits)
//                    val x = cos(angle) * radius + X
//                    val y = sin(angle) * radius + Y
//                    g.drawRect(x.toInt(), y.toInt(), 1, 1)
//                    angle += step
//                }
//                ""
//            } else {
//                println("Ignoring track $trackNumber: map is 0xff")
//            }
//        }
    }

    fun bitsToColor(bits: List<Int>): Color {
        val blacks = bits.count { it == 0 }
        val percent = blacks / bits.size
        val color = Color(percent, percent, percent)
        return color
    }
}