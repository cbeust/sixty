package com.beust.swt

import com.beust.app.*
import com.beust.sixty.h
import org.eclipse.swt.SWT
import org.eclipse.swt.browser.*
import org.eclipse.swt.custom.StyleRange
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite


class ByteBufferWindow(parent: Composite) : Composite(parent, SWT.NONE) {
    private var disk: IDisk? = null
    private val rowSize = 16
    private val browser: Browser
    private lateinit var nibbleTrack: NibbleTrack
    private val bufferContent = arrayListOf<TimedByte>()

    init {
        // Create a child composite to hold the controls
        layoutData = GridData(SWT.FILL, SWT.FILL, true, true)
        layout = GridLayout(2, false)
        background = white(display)


        browser = Browser(this, SWT.NONE).apply {
            layoutData = GridData(SWT.FILL, SWT.FILL, true, true)
        }

        UiState.diskStates[0].file.addListener { _, new ->
            if (new != null) {
                UiState.currentBufferTrack.value = 0
                disk = IDisk.create(new)
                disk?.let {
                    updateBufferContent(it)
                }
            }
        }
        UiState.currentBufferTrack.addListener { _, new ->
            updateBufferContent(track = new)
        }
        UiState.byteAlgorithn.addListener { _, new ->
            updateBufferContent(byteAlgorithm = new)
        }
        UiState.addressPrologue.addAfterListener { _, _ -> updateBufferContent() }
        UiState.addressEpilogue.addAfterListener { _, _ -> updateBufferContent() }
        UiState.dataPrologue.addAfterListener { _, _ -> updateBufferContent() }
        UiState.dataEpilogue.addAfterListener { _, _ -> updateBufferContent() }

        updateBufferContent()
    }

    private fun updateBufferContent(passedDisk: IDisk? = null, track: Int = 0,
            byteAlgorithm: ByteAlgorithm = ByteAlgorithm.SHIFTED) {
        bufferContent.clear()
        display.asyncExec { browser.text = "" }
        val disk = passedDisk ?: IDisk.create(UiState.diskStates[0].file.value)
        if (disk != null) {
            repeat(160) { disk.decPhase() }
            repeat(track) {
                disk.incPhase()
            }
            nibbleTrack = NibbleTrack(disk, disk.sizeInBits, createMarkFinders())

            fun nextByte(): TimedByte {
                val result =
                    when (byteAlgorithm) {
                        ByteAlgorithm.SHIFTED -> {
                            nibbleTrack.nextByte()
                        }
                        else -> {
                            TimedByte(disk.nextByte(), 0)
                        }
                    }
                return result
            }

            var row = 0
            val byteText = StringBuffer()
            val html = StringBuffer("""
                <head>
                    <style>
                        table { font: 14px Courier; border-spacing: 0 }
                        td { vertical-align:bottom }
                        .superscript { color: red; vertical-align: 30%; font-size: 80%}
                        .address { background-color: yellow }
                        .data { background-color: lightblue}
                        .offset { color: blue; font-weight: bold; padding-right: 15px; }
                    </style>
                </head>
                <body>
                    <table>
            """.trimIndent())
            val analyzedTrack = nibbleTrack.analyzedTrack
            var byteCount = 0
            while (byteCount < nibbleTrack.sizeInBits / 8 + 1) {
                if (byteCount % rowSize == 0) {
                    // New offset
                    if (byteCount > 0) html.append("</tr>\n")
                    html.append("<tr>")
                        .append("<td class=\"offset\">\$" + String.format("%04X", row) + "</td>")
                    row += rowSize
                }

                val nb = nextByte()
                bufferContent.add(nb)
                byteText.append(nb.byte.h())
                val clsName = if (analyzedTrack != null) when(analyzedTrack.typeFor(byteCount)) {
                        NibbleTrack.AnalyzedTrack.Type.ADDRESS -> " address"
                        NibbleTrack.AnalyzedTrack.Type.DATA -> " data"
                        else -> ""
                    } else {
                        ""
                    }
                val cls = " id=\"$byteCount\" class=\"nibble$clsName\""
                val sup = if (nb.timingBitCount > 0) "<sup>${nb.timingBitCount}</sup>" else ""
                html.append("<td$cls>" + nb.byte.h() + sup + "</td>")

                byteText.append(if (nb.timingBitCount > 0) String.format("%-2d", nb.timingBitCount) else "  ")
                byteCount++
            }
            val script = """
                   <script>
                        var elements = document.getElementsByClassName("nibble");
                        for (var i = 0; i < elements.length; i++) {
                            (function (index) {
                                elements[index].addEventListener("click", function () {
                                    for (var x = 0; x < elements.length; x++) {
                                        if (elements[x] === this) nibbleClicked(elements[x].id);
                                    }
                        
                                }, false);
                            })(i);
                        }
                    </script>
            """.trimIndent()
            html.append("</table></body>$script</html>")

            CustomFunction(browser, "nibbleClicked")

//            browser.addProgressListener(ProgressListener.completedAdapter({ event ->
//                browser.addLocationListener(object : LocationAdapter() {
//                    override fun changed(event: LocationEvent) {
//                        browser.removeLocationListener(this)
//                        println("left java function-aware page, so disposed CustomFunction")
//                        function.dispose()
//                    }
//                })
//            }))
            display.asyncExec {
                browser.text = html.toString()
            }
        }
    }

    inner class CustomFunction(browser: Browser?, name: String?)
        : BrowserFunction(browser, name) {
        override fun function(arguments: Array<Any>) {
            val index = Integer.parseInt(arguments[0].toString())
            UiState.currentBytes.value = listOf(bufferContent[index], bufferContent[index + 1]).map { it.byte }
            UiState.caretSectorInfo.value = nibbleTrack.sectorInfoForIndex(index)
        }
    }

    private fun createMarkFinders() = object: IMarkFinders {
        private fun matches(regexp: String, bytes: List<Int>)
            = bytes.joinToString("", transform = Int::h).matches(regexp.toRegex(RegexOption.IGNORE_CASE))

        override fun isAddressPrologue(bytes: List<Int>) = matches(UiState.addressPrologue.value, bytes)
        override fun isAddressEpilogue(bytes: List<Int>) = matches(UiState.addressEpilogue.value, bytes)
        override fun isDataPrologue(bytes: List<Int>) = matches(UiState.dataPrologue.value, bytes)
        override fun isDataEpilogue(bytes: List<Int>)  = matches(UiState.dataEpilogue.value, bytes)
    }
}