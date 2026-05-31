package com.instasprite.app.domain.draw

interface ITransformManager {
    fun rotate()
    fun hFlip()
    fun vFlip()
    fun resizeCanvas(width: Int, height: Int)
}
