package com.beust.app

import com.beust.sixty.logUiStatus
import com.beust.swt.ByteBufferWindow
import org.eclipse.swt.widgets.Display
import java.io.File

typealias ObsListener<T> = (T, T) -> Unit

class Obs<T>(val name: String, val def: T) {
    private val listeners = mutableListOf<ObsListener<T>>()
    private val afterListeners = mutableListOf<ObsListener<T>>()

    fun addListener(l: (T, T) -> Unit) { listeners.add(l) }
    fun addAfterListener(l: (T, T) -> Unit) { afterListeners.add(l) }

    private var _value = def
    var value: T
        get() = _value
        set(f) {
            logUiStatus("$name=$f")
            Display.getDefault().asyncExec {
                listeners.forEach { it.invoke(_value, f) }
                _value = f
                afterListeners.forEach { it.invoke(_value, f) }
            }
        }

    override fun toString() = _value.toString()
}

enum class ByteAlgorithm { RAW, SHIFTED, DISK_CONTENT }

object UiState {
    //
    // General
    //
    val error = Obs("Error", "")

    //
    // Disk drives
    //
//    val currentSectorInfo: Obs<NibbleTrack.SectorInfo?> = Obs("Sector info", null)
    class DiskState(val file: Obs<File?> = Obs("Disk file", null),
            val motor: Obs<Boolean> = Obs("Motor", false),
            val currentPhase: Obs<Int> = Obs("Current phase", 0),
            val currentSector: Obs<NibbleTrack.SectorInfo?> = Obs("Current sector", null))
    val diskStates = arrayListOf(DiskState(), DiskState())

    init {
        diskStates[0].file.value = DISK
    }

    //
    // DISK tab
    //
    val currentBufferTrack = Obs("Current track", 0)
    val byteAlgorithn = Obs("Byte algorithm", ByteAlgorithm.SHIFTED)
    val currentBytes: Obs<List<Int>> = Obs("Current bytes", emptyList())
    val addressPrologue: Obs<String> = Obs("Address prologue", "d5aa96")
    val addressEpilogue: Obs<String> = Obs("Address epilogue", "deaa")
    val dataPrologue: Obs<String> = Obs("Address prologue", "d5aaad")
    val dataEpilogue: Obs<String> = Obs("Address epilogue", "deaa")
    val caretSectorInfo: Obs<NibbleTrack.SectorInfo?> = Obs("Current sector info", null)

    //
    // DEBUGGER tab
    //
    val debug: Obs<Boolean> = Obs("Debug", false)
    val speedMegahertz: Obs<Float> = Obs("Speed", 0.0f)

    //
    // Text/hires pages
    //
    val mainScreenText = Obs("text", true)
    val mainScreenMixed = Obs("mixed", false)
    val mainScreenHires = Obs("hires", false)
    val mainScreenPage2 = Obs("page2", false)

    //
    // Other memory switches
    //
    val store80On = Obs("80StoreOn", false)
    val readAux = Obs("Read aux", false)
    val writeAux = Obs("Write aux", false)
    val internalCxRom = Obs("Internal Cx rom", false)
    val altZp = Obs("Alt Zero Page", false)
    val slotC3Rom = Obs("Slot C3 rom", false)
}