package com.beust.swt

import com.beust.app.Apple2Computer
import com.beust.app.Obs
import com.beust.app.UiState
import com.beust.sixty.Apple2Memory
import com.beust.sixty.h
import com.beust.sixty.hh
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Group

class DebuggerWindow(parent: Composite, private val computer: () -> Apple2Computer): Composite(parent, SWT.NONE) {
    class State(val name: String, val obs: Obs<Boolean>, val statusAddress: Int)

    private val states = listOf(
        State("Text", UiState.mainScreenText, 0xc01a),
        State("Mixed", UiState.mainScreenMixed, 0xc01b),
        State("Page 2", UiState.mainScreenPage2, 0xc01c),
        State("Hires", UiState.mainScreenHires, 0xc01d)
    )

    init {
        layout = GridLayout(1, false)
        Button(this, SWT.CHECK).apply {
            text = "Debug"
            addSelectionListener(object: SelectionAdapter() {
                override fun widgetSelected(e: SelectionEvent?) {
                    UiState.debug.value = selection
                }
            })
        }
        Group(this, SWT.NONE).apply {
            layout = GridLayout(1, false)
            layoutData = GridData(SWT.FILL, SWT.FILL, true, false)
            text = "Graphic switches"
            createStateWidget(this, states)
        }
//        val speedMhz = label(this, "<speed in Mhz>", SWT.BORDER).apply {
//            font = font(shell, "Arial", 15, SWT.BOLD)
//        }
//        UiState.speedMegahertz.addAfterListener { _, new ->
//            display.asyncExec {
//                speedMhz.text = String.format("%2.2f Mhz", new)
//            }
//        }

    }

    private fun createStateWidget(parent: Composite, states: List<State>) {
        Composite(parent, SWT.NONE).apply {
            states.forEach { state ->
                layout = GridLayout(3, false)
                label(this, state.name)
                val checkBox = Button(this, SWT.CHECK).apply {
                    selection = state.obs.value
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