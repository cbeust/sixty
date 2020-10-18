package com.beust.swt

import com.beust.app.BitPattern
import com.beust.app.SColor
import com.beust.sixty.ERROR
import com.beust.sixty.IMemory
import com.beust.sixty.hh
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.ImageData
import org.eclipse.swt.graphics.PaletteData
import org.eclipse.swt.graphics.RGB
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Canvas
import org.eclipse.swt.widgets.Composite

class HiResWindow(parent: Composite, style: Int = SWT.NONE): Composite(parent, style) {
    override fun toString() = "HiRes on page $page"

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
//    private val img: Image
//    private val gc: GC
    private val canvas: Canvas
    private val imageDatas = arrayListOf<ImageData>()

    private val content = Array(WIDTH * HEIGHT) { SColor.BLACK }
    private fun index(x: Int, y: Int) = y * WIDTH + x
    // https://mrob.com/pub/xapple2/colors.html
    private val PALETTE_DATA = PaletteData(
            RGB(0, 0, 0), RGB(0xff, 0xff, 0xff), RGB(20, 245, 60), RGB(255, 106, 60), RGB(255, 68, 253),
            RGB(20, 207, 254))
    private var stop = false
    fun stop() {
        stop = true
    }

    fun clear() {
        display.syncExec {
            imageDatas.forEach { id ->
                repeat(ACTUAL_HEIGHT) { y ->
                    repeat(ACTUAL_WIDTH) { x ->
                        id.setPixel(x, y, 0)
                    }
                }
            }
        }
    }

    private fun drawSquare(imageData: ImageData, xx: Int, yy: Int, color: Int) {
        val x = xx * WIDTH_FACTOR
        val y = yy * HEIGHT_FACTOR
        repeat(WIDTH_FACTOR) { w ->
            repeat(HEIGHT_FACTOR) { h ->
                imageData.setPixel(x + w, y + h, color)
            }
        }
    }

    private fun newImageData() = ImageData(ACTUAL_WIDTH, ACTUAL_HEIGHT, 4, PALETTE_DATA)

//    private fun newImage(): Image {
//        val imageData = newImageData()
//        drawSquare(imageData, 0, 0, 4)
//        return Image(display, imageData)
//    }

    var page = 0 // or 1
    private fun imageData() = if (page == 0) imageDatas[0] else imageDatas[1]

    init {
        layout = FillLayout()
        imageDatas.add(newImageData())
        imageDatas.add(newImageData())
        canvas = Canvas(this, SWT.DOUBLE_BUFFERED).apply {
            addPaintListener { e ->
                val image = Image(display, imageData())
                e.gc.apply {
                    drawImage(image, 0, 0)
                    image.dispose()
                }
            }
        }    // Set up the timer for the animation

        // Set up the timer for the animationval
        val TIMER_INTERVAL = 100
        val runnable: Runnable = object : Runnable {
            override fun run() {
                if (! display.isDisposed) {
                    canvas.redraw()
                    if (! stop) display.timerExec(TIMER_INTERVAL, this)
                }
            }
        }
        display.timerExec(TIMER_INTERVAL, runnable)


//        Timer().scheduleAtFixedRate(object: TimerTask() {
//            override fun run() {
//                display.asyncExec { lab.redraw() }
//            }
//        }, 0, 500)

        var l = 0
        interleaving.forEach { il ->
            consecutives.forEach { c ->
                lineMap[il + c] = l++
            }
        }
    }
//    fun drawMemoryLocation(memory: IMemory, location: Int) {}

    fun drawMemoryLocation(memory: IMemory, location: Int, page: Int) {
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
        val loc = evenLocation - (if (page == 0) 0x2000 else 0x4000)
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

        fun drawPixel(x: Int, y: Int, color: SColor, page: Int) {
            if (x < WIDTH) {
                content[index(x, y)] = color
                drawSquare(imageDatas[page], x, y, color.ordinal)
//                display.syncExec {
//                    println("Drawing pixel at " + (x * WIDTH_FACTOR) + "," + (x * WIDTH_FACTOR) + " color: " + color.ordinal)
//                    img.imageData.setPixel(x * WIDTH_FACTOR, y * HEIGHT_FACTOR, color.ordinal)
//                    label.redraw()
//                    gc.background = color.toSwtColor(display)
//                    gc.fillRectangle(x * WIDTH_FACTOR, y * HEIGHT_FACTOR, blockWidth, blockHeight)
//                }
            }
        }

        y!!

        repeat(2) { drawPixel(x + i++, y, BitPattern.color(bitPattern.p0, bitPattern.aa, x), page) }
        repeat(2) { drawPixel(x + i++, y, BitPattern.color(bitPattern.p0, bitPattern.bb, x + 1), page) }
        repeat(2) { drawPixel(x + i++, y, BitPattern.color(bitPattern.p0, bitPattern.cc, x + 2), page) }
        repeat(2) {
            drawPixel(x + i++, y, BitPattern.color(if (even) bitPattern.p0 else bitPattern.p1,
                    bitPattern.dd, x + 3), page)
        }
        repeat(2) { drawPixel(x + i++, y, BitPattern.color(bitPattern.p1, bitPattern.ee, x + 4), page) }
        repeat(2) { drawPixel(x + i++, y, BitPattern.color(bitPattern.p1, bitPattern.ff, x + 5), page) }
        repeat(2) { drawPixel(x + i++, y, BitPattern.color(bitPattern.p1, bitPattern.gg, x + 6), page) }
    }

}