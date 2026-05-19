package com.instasprite.app.utils

import com.instasprite.app.domain.model.BlendMode

fun blendPixel(dst: Int, src: Int, layerOpacity: Float, mode: BlendMode): Int {
    if (src == 0) return dst // fully transparent source → no-op
    val effectiveSrc = if (layerOpacity >= 1f) src else applyOpacity(src, layerOpacity)
    if (effectiveSrc == 0) return dst
    return when (mode) {
        BlendMode.NORMAL -> blendNormal(dst, effectiveSrc)
    }
}

fun blendPixel(dst: Int, src: Int, layerOpacity: Float): Int {
    return blendPixel(dst, src, layerOpacity, BlendMode.NORMAL)
}

private fun applyOpacity(argb: Int, opacity: Float): Int {
    if (opacity <= 0f) return 0
    val srcA = (argb ushr 24) and 0xFF
    val newA = (srcA * (opacity * 256).toInt()) ushr 8
    return if (newA == 0) 0 else (argb and 0x00FFFFFF) or (newA shl 24)
}

private fun blendNormal(dst: Int, src: Int): Int {
    val srcA = (src ushr 24) and 0xFF
    if (srcA == 0xFF) return src
    if (srcA == 0) return dst

    val dstA = (dst ushr 24) and 0xFF
    if (dstA == 0) return src

    val invSrcA = 255 - srcA

    val srcR = (src ushr 16) and 0xFF
    val srcG = (src ushr 8) and 0xFF
    val srcB = src and 0xFF

    val dstR = (dst ushr 16) and 0xFF
    val dstG = (dst ushr 8) and 0xFF
    val dstB = dst and 0xFF

    val outA = srcA + ((dstA * invSrcA + 127) / 255)

    if (outA == 0) return 0

    val outR = (srcR * srcA + dstR * dstA * invSrcA / 255 + outA / 2) / outA
    val outG = (srcG * srcA + dstG * dstA * invSrcA / 255 + outA / 2) / outA
    val outB = (srcB * srcA + dstB * dstA * invSrcA / 255 + outA / 2) / outA

    return (outA.coerceAtMost(255) shl 24) or
            (outR.coerceAtMost(255) shl 16) or
            (outG.coerceAtMost(255) shl 8) or
            outB.coerceAtMost(255)
}
