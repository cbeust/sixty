package com.beust.swt

import com.beust.app.Apple2Computer
import com.beust.app.Obs
import com.beust.app.UiState
import com.beust.sixty.h
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Group

class DebuggerWindow(parent: Composite, private val computer: () -> Apple2Computer): Composite(parent, SWT.NONE) {
    class State(val name: String, val onAddress: Int, val offAddress: Int, val statusAddress: Int,
            val obs: Obs<Boolean>)

    private val graphicStates = listOf(
        State("Text", 0xc051, 0xc050, 0xc01a, UiState.mainScreenText),
        State("Mixed", 0xc053, 0xc052, 0xc01b, UiState.mainScreenMixed),
        State("Page 2", 0xc055, 0xc054, 0xc01c, UiState.mainScreenPage2),
        State("Hires", 0xc057, 0xc056, 0xc01d, UiState.mainScreenHires)
    )

    private val memoryStates = listOf(
        State("Store80", 0xc001, 0xc000, 0xc018, UiState.store80On),
        State("Read aux", 0xc003, 0xc002, 0xc013, UiState.readAux),
        State("Write aux", 0xc005, 0xc004, 0xc014, UiState.writeAux),
        State("Internal Cx rom", 0xc007, 0xc006, 0xc015, UiState.internalCxRom),
        State("Alt Zero Page", 0xc009, 0xc008, 0xc016, UiState.altZp),
        State("Slot C3 rom", 0xc00b, 0xc00a, 0xc017, UiState.slotC3Rom)
    )

    init {
        val columns = 2
        layout = GridLayout(columns, false)
        Button(this, SWT.CHECK).apply {
            text = "Show 6502 execution"
//            layoutData = GridData().apply {
//                horizontalSpan = columns - 1
//            }
            addSelectionListener(object: SelectionAdapter() {
                override fun widgetSelected(e: SelectionEvent?) {
                    UiState.debugAsm.value = selection
                }
            })
        }
        button(this, "Dump").apply {
            addSelectionListener(object: SelectionAdapter() {
                override fun widgetSelected(e: SelectionEvent?) {
                    with(computer().memory) {
                        save("d:/t/page1.pic", 0x2000, 0x2000)
                        save("d:/t/page2.pic", 0x4000, 0x2000)
                    }
                }
            })
        }
        createStateWidget(this, "Graphic switches", graphicStates)
        createStateWidget(this, "Memory switches", memoryStates)
//        Group(this, SWT.NONE).apply {
//            layout = GridLayout(1, false)
//            layoutData = GridData(SWT.FILL, SWT.FILL, true, false)
//            text = "Switches"
//        }
//        val speedMhz = label(this, "<speed in Mhz>", SWT.BORDER).apply {
//            font = font(shell, "Arial", 15, SWT.BOLD)
//        }
//        UiState.speedMegahertz.addAfterListener { _, new ->
//            display.asyncExec {
//                speedMhz.text = String.format("%2.2f Mhz", new)
//            }
//        }

    }

    private fun createStateWidget(parent: Composite, title: String, states: List<State>) {
        Group(this, SWT.NONE).apply {
            layout = GridLayout(1, false)
            layoutData = GridData(SWT.FILL, SWT.FILL, true, false)
            text = title

            Composite(this, SWT.NONE).apply {
                states.forEach { state ->
                    layout = GridLayout(3, false)
                    label(this, state.name).apply {
                        font = Fonts.makeBold(display, font)
                    }
                    val checkBox = Button(this, SWT.CHECK).apply {
                        selection = state.obs.value
                        addListener(SWT.Selection) { e ->
                            computer().memory[if (selection) state.onAddress else state.offAddress] = 0
                        }
                    }
                    val status = label(this, "\$00")// computer().memory[state.statusAddress].h())

                    state.obs.addAfterListener { _, new ->
                        val s = computer().memory[state.statusAddress].h()
                        checkBox.selection = new
                        status.text = "$" + s
                    }
                }
            }
        }
    }
}