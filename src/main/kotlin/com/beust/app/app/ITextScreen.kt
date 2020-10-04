package com.beust.app.app

interface ITextScreen {
    companion object {
        const val WIDTH = 40
        const val HEIGHT = 24
    }
    fun drawCharacter(x: Int, y: Int, value: Int)
}
