package com.olaz.instasprite.domain.image2pixel

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

// ─────────────────────────────────────────────────────────────────────────────
//  Configuration
// ─────────────────────────────────────────────────────────────────────────────

/**
 * All user-tunable parameters for the pixel-art conversion.
 *
 * @param autoDetect        If true, the processor tries to detect the logical
 *                          pixel-block size automatically (good for images that
 *                          are already upscaled pixel art). Set to false and
 *                          supply [targetWidth] for plain pixelation.
 * @param targetWidth       Width (in logical pixels) of the output when
 *                          [autoDetect] is false. Height is derived from the
 *                          source aspect ratio.
 * @param colorCount        Maximum number of colours in the final palette (2–256).
 *                          Lower values give a more "retro" look.
 * @param prefilterStrength Bilateral noise-removal applied before quantization.
 *                          0.0 = disabled, 1.0 = maximum smoothing.
 * @param enableDithering   Apply Floyd-Steinberg dithering after colour
 *                          quantization to improve gradient representation.
 * @param exportScale       Integer upscale factor used in [PixelArtProcessor.export].
 *                          e.g. 10 → each logical pixel becomes a 10 × 10 block.
 * @param showGridOnExport  Draw 1-px separator lines between logical pixels in
 *                          the exported bitmap.
 */
data class PixelArtConfig(
    val autoDetect: Boolean = true,
    val targetWidth: Int = 64,
    val colorCount: Int = 32,
    val prefilterStrength: Float = 0.3f,
    val enableDithering: Boolean = false,
    val exportScale: Int = 10,
    val showGridOnExport: Boolean = false,
    val customPalette: List<Int>? = null,
)

/**
 * Result returned by the internal auto-detection step.
 *
 * @param blockSize  Detected logical pixel size in source pixels.
 * @param confidence 0.0–1.0 score; values above ~0.7 are considered reliable.
 */
data class DetectionResult(
    val blockSize: Int,
    val confidence: Float,
)

class PixelArtConverter {

    // ─────────────────────────────────────────────────────────────────────────
    //  Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Convert [source] into a pixel-art bitmap according to [config].
     *
     * The returned bitmap is the *base* resolution — one pixel per logical
     * pixel-art cell. Pass it to [export] to get a scaled-up version.
     *
     * This function is safe to call from any coroutine; heavy work runs on
     * [Dispatchers.Default].
     */
    suspend fun process(source: Bitmap, config: PixelArtConfig): Bitmap =
        withContext(Dispatchers.Default) {
            // Cap working resolution to avoid OOM on large inputs
            val working = scaledBitmap(source, maxDimension = 2000)

            val base: Bitmap = if (config.autoDetect) {
                val detection = detectPixelBlockSize(working)
                if (detection.blockSize > 1) {
                    downsampleToBase(working, detection.blockSize)
                } else {
                    pixelateToWidth(working, 64)
                }
            } else {
                pixelateToWidth(working, config.targetWidth)
            }

            applyColorProcessing(base, config)
            base
        }

    /**
     * Upscale [base] by [config].exportScale for saving or printing.
     *
     * Each logical pixel becomes an [exportScale] × [exportScale] block.
     * Optionally draws a grid between blocks when [config].showGridOnExport
     * is true.
     *
     * This is a synchronous call — run it off the main thread if the scale
     * factor is large.
     */
    fun export(base: Bitmap, config: PixelArtConfig): Bitmap {
        val s = config.exportScale
        val out = createBitmap(base.width * s, base.height * s)
        val canvas = Canvas(out)
        val paint = Paint().apply {
            isAntiAlias = false
            isFilterBitmap = false
        }
        canvas.drawBitmap(
            base,
            null,
            RectF(0f, 0f, out.width.toFloat(), out.height.toFloat()),
            paint,
        )
        if (config.showGridOnExport) {
            drawGrid(canvas, out.width, out.height, pixelSize = s)
        }
        return out
    }

    /**
     * Run only the detection step without a full conversion.
     * Useful for showing the user what block size was found before processing.
     */
    suspend fun detectPixelBlockSize(source: Bitmap): DetectionResult =
        withContext(Dispatchers.Default) {
            val small = scaledBitmap(source, maxDimension = 800)
            detectBlockSize(small)
        }

    // ─────────────────────────────────────────────────────────────────────────
    //  Auto-detection
    // ─────────────────────────────────────────────────────────────────────────

    private fun detectBlockSize(bitmap: Bitmap): DetectionResult {
        val pixels = bitmapToIntArray(bitmap)
        val w = bitmap.width
        val h = bitmap.height

        val hStats = analyzeHorizontalScanlines(pixels, w, h)
        val vStats = analyzeVerticalScanlines(pixels, w, h)
        val candidates = buildCandidates(hStats, vStats)

        return pickBestCandidate(pixels, w, h, candidates)
    }

    private fun analyzeHorizontalScanlines(pixels: IntArray, w: Int, h: Int): SizeStats {
        val segments = mutableListOf<Int>()
        val sampleCount = minOf(10, h)
        for (i in 0 until sampleCount) {
            val y = if (sampleCount > 1) h * i / (sampleCount - 1) else 0
            segments += scanlineSegments(pixels, w, h, pos = y, horizontal = true)
        }
        return calcStats(segments)
    }

    private fun analyzeVerticalScanlines(pixels: IntArray, w: Int, h: Int): SizeStats {
        val segments = mutableListOf<Int>()
        val sampleCount = minOf(10, w)
        for (i in 0 until sampleCount) {
            val x = if (sampleCount > 1) w * i / (sampleCount - 1) else 0
            segments += scanlineSegments(pixels, w, h, pos = x, horizontal = false)
        }
        return calcStats(segments)
    }

    /**
     * Walk a single row or column and return the lengths of same-colour runs.
     */
    private fun scanlineSegments(
        pixels: IntArray,
        w: Int,
        h: Int,
        pos: Int,
        horizontal: Boolean,
    ): List<Int> {
        val result = mutableListOf<Int>()
        var currentColor = Int.MIN_VALUE
        var runLength = 0
        val length = if (horizontal) w else h

        for (i in 0 until length) {
            val idx = if (horizontal) pos * w + i else i * w + pos
            val color = pixels.getOrElse(idx) { 0 }

            if (color == currentColor) {
                runLength++
            } else {
                if (runLength > 0) result += runLength
                currentColor = color
                runLength = 1
            }
        }
        if (runLength > 0) result += runLength
        return result
    }

    private data class SizeStats(val mode: Int, val gcd: Int)

    private fun calcStats(segments: List<Int>): SizeStats {
        if (segments.isEmpty()) return SizeStats(1, 1)
        val mode = segments.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: 1
        val gcd = segments.reduce { a, b -> gcd(a, b) }
        return SizeStats(mode, gcd)
    }

    private fun buildCandidates(h: SizeStats, v: SizeStats): List<Pair<Int, Float>> {
        val candidates = mutableListOf<Pair<Int, Float>>()
        // Same mode in both directions → very likely the true block size
        if (h.mode == v.mode && h.mode > 1) {
            candidates += h.mode to 0.9f
        }
        // Common GCD is a weaker signal
        val commonGcd = gcd(h.gcd, v.gcd)
        if (commonGcd in 2..16) {
            candidates += commonGcd to 0.7f
        }
        if (candidates.isEmpty()) candidates += 1 to 0.1f
        return candidates.sortedByDescending { it.second }
    }

    private fun pickBestCandidate(
        pixels: IntArray,
        w: Int,
        h: Int,
        candidates: List<Pair<Int, Float>>,
    ): DetectionResult {
        var best = DetectionResult(blockSize = 1, confidence = 0f)
        for ((size, baseConf) in candidates) {
            val conf = scoreBlockSize(pixels, w, h, size) * baseConf
            if (conf > best.confidence) best = DetectionResult(size, conf)
        }
        return best
    }

    /**
     * Score a candidate block size by measuring intra-block colour variance.
     * Lower variance → pixels inside each block look the same → good block size.
     */
    private fun scoreBlockSize(pixels: IntArray, w: Int, h: Int, blockSize: Int): Float {
        val sampleCount = minOf(100, (w * h) / blockSize.coerceAtLeast(1))
        var totalVariance = 0.0
        repeat(sampleCount) {
            val sx = (Math.random() * (w - blockSize)).toInt().coerceAtLeast(0)
            val sy = (Math.random() * (h - blockSize)).toInt().coerceAtLeast(0)
            totalVariance += blockVariance(pixels, w, h, sx, sy, blockSize)
        }
        val avg = if (sampleCount > 0) totalVariance / sampleCount else 0.0
        return (1.0 - avg / 10_000.0).toFloat().coerceIn(0f, 1f)
    }

    private fun blockVariance(
        pixels: IntArray,
        w: Int,
        h: Int,
        sx: Int,
        sy: Int,
        bs: Int,
    ): Double {
        var sumR = 0.0; var sumG = 0.0; var sumB = 0.0; var count = 0

        for (y in sy until minOf(sy + bs, h)) {
            for (x in sx until minOf(sx + bs, w)) {
                val c = pixels[y * w + x]
                sumR += Color.red(c); sumG += Color.green(c); sumB += Color.blue(c)
                count++
            }
        }
        if (count == 0) return 0.0

        val ar = sumR / count; val ag = sumG / count; val ab = sumB / count
        var variance = 0.0
        for (y in sy until minOf(sy + bs, h)) {
            for (x in sx until minOf(sx + bs, w)) {
                val c = pixels[y * w + x]
                variance += (Color.red(c) - ar).pow(2) +
                        (Color.green(c) - ag).pow(2) +
                        (Color.blue(c) - ab).pow(2)
            }
        }
        return variance / count
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Scaling
    // ─────────────────────────────────────────────────────────────────────────

    private fun scaledBitmap(src: Bitmap, maxDimension: Int): Bitmap {
        val max = maxOf(src.width, src.height)
        if (max <= maxDimension) return src
        val scale = maxDimension.toFloat() / max
        return src.scale(
            (src.width * scale).toInt().coerceAtLeast(1),
            (src.height * scale).toInt().coerceAtLeast(1),
            false,
        )
    }

    /**
     * Average each [blockSize] × [blockSize] region of [src] into one output pixel.
     */
    private fun downsampleToBase(src: Bitmap, blockSize: Int): Bitmap {
        val tw = (src.width / blockSize).coerceAtLeast(1)
        val th = (src.height / blockSize).coerceAtLeast(1)
        val srcPixels = bitmapToIntArray(src)
        val dstPixels = IntArray(tw * th)

        for (y in 0 until th) {
            for (x in 0 until tw) {
                dstPixels[y * tw + x] = averageBlockColor(
                    srcPixels, src.width, src.height,
                    startX = x * blockSize,
                    startY = y * blockSize,
                    blockSize = blockSize,
                )
            }
        }

        return createBitmap(tw, th).also {
            it.setPixels(dstPixels, 0, tw, 0, 0, tw, th)
        }
    }

    private fun averageBlockColor(
        pixels: IntArray,
        w: Int,
        h: Int,
        startX: Int,
        startY: Int,
        blockSize: Int,
    ): Int {
        var r = 0L; var g = 0L; var b = 0L; var a = 0L; var count = 0
        for (y in startY until minOf(startY + blockSize, h)) {
            for (x in startX until minOf(startX + blockSize, w)) {
                val c = pixels[y * w + x]
                r += Color.red(c); g += Color.green(c); b += Color.blue(c); a += Color.alpha(c)
                count++
            }
        }
        if (count == 0) return Color.TRANSPARENT
        return Color.argb((a / count).toInt(), (r / count).toInt(), (g / count).toInt(), (b / count).toInt())
    }

    /** Scale [src] down to [targetWidth] pixels wide using nearest-neighbour. */
    private fun pixelateToWidth(src: Bitmap, targetWidth: Int): Bitmap {
        val tw = targetWidth.coerceAtLeast(1)
        val th = (src.height.toFloat() * tw / src.width).roundToInt().coerceAtLeast(1)
        return src.scale(tw, th, false)
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Colour processing  (all operations are in-place on [bmp])
    // ─────────────────────────────────────────────────────────────────────────

    private fun applyColorProcessing(bmp: Bitmap, config: PixelArtConfig) {
        var pixels = bitmapToIntArray(bmp)

        if (config.prefilterStrength > 0f) {
            pixels = bilateralFilter(pixels, bmp.width, bmp.height, config.prefilterStrength)
        }

        if (config.customPalette != null) {
            pixels = medianCutQuantize(pixels, config.colorCount)
            pixels = IntArray(pixels.size) { i ->
                config.customPalette.minByOrNull { colorDistanceSq(pixels[i], it) } ?: pixels[i]
            }
        } else {
            pixels = medianCutQuantize(pixels, config.colorCount)
        }

        if (config.enableDithering) {
            pixels = floydSteinbergDither(pixels, bmp.width, bmp.height, config.colorCount, config.customPalette)
        }

        bmp.setPixels(pixels, 0, bmp.width, 0, 0, bmp.width, bmp.height)
    }

    // ── Bilateral filter ─────────────────────────────────────────────────────

    /**
     * Edge-preserving noise removal. Smooths uniform regions while keeping
     * sharp colour boundaries intact.
     *
     * @param strength 0.0–1.0; controls how aggressively colour differences
     *                 are allowed to influence the filter weight.
     */
    private fun bilateralFilter(
        pixels: IntArray,
        w: Int,
        h: Int,
        strength: Float,
    ): IntArray {
        val out = pixels.copyOf()
        val colorSigma = 30.0 * strength
        val radius = 1

        for (y in radius until h - radius) {
            for (x in radius until w - radius) {
                val center = pixels[y * w + x]
                val cR = Color.red(center).toDouble()
                val cG = Color.green(center).toDouble()
                val cB = Color.blue(center).toDouble()

                var sumR = 0.0; var sumG = 0.0; var sumB = 0.0; var sumWeight = 0.0

                for (dy in -radius..radius) {
                    for (dx in -radius..radius) {
                        val neighbor = pixels[(y + dy) * w + (x + dx)]
                        val nR = Color.red(neighbor).toDouble()
                        val nG = Color.green(neighbor).toDouble()
                        val nB = Color.blue(neighbor).toDouble()

                        // Spatial Gaussian — penalise distance from center
                        val spatialW = exp(-(dx * dx + dy * dy) / 2.0)
                        // Range Gaussian — penalise colour difference
                        val colorDist = sqrt((cR - nR).pow(2) + (cG - nG).pow(2) + (cB - nB).pow(2))
                        val colorW = exp(-colorDist.pow(2) / (2 * colorSigma * colorSigma))

                        val w2 = spatialW * colorW
                        sumR += nR * w2; sumG += nG * w2; sumB += nB * w2
                        sumWeight += w2
                    }
                }

                out[y * w + x] = Color.argb(
                    Color.alpha(center),
                    (sumR / sumWeight).roundToInt().coerceIn(0, 255),
                    (sumG / sumWeight).roundToInt().coerceIn(0, 255),
                    (sumB / sumWeight).roundToInt().coerceIn(0, 255),
                )
            }
        }
        return out
    }

    // ── Median-cut colour quantization ───────────────────────────────────────

    /**
     * Reduce the image to at most [colorCount] colours using the median-cut
     * algorithm. Each pixel is then mapped to the closest palette colour.
     */
    private fun medianCutQuantize(pixels: IntArray, colorCount: Int): IntArray {
        val boxes: ArrayDeque<ColorBox> = ArrayDeque<ColorBox>().also {
            it += ColorBox(pixels.toMutableList())
        }

        while (boxes.size < colorCount) {
            val box = boxes.maxByOrNull { it.largestRange } ?: break
            val split = box.split() ?: break
            boxes.remove(box)
            boxes.addAll(split)
        }

        val palette = boxes.map { it.averageColor() }

        return IntArray(pixels.size) { i ->
            palette.minByOrNull { colorDistanceSq(pixels[i], it) } ?: pixels[i]
        }
    }

    // ── Floyd-Steinberg dithering ────────────────────────────────────────────

    /**
     * Distribute quantization error to neighbouring pixels so gradients look
     * smoother even with a small palette.
     *
     * Error diffusion pattern:
     * ```
     *          X    7/16
     *  3/16  5/16  1/16
     * ```
     */
    private fun floydSteinbergDither(
        pixels: IntArray,
        w: Int,
        h: Int,
        colorCount: Int,
        customPalette: List<Int>?
    ): IntArray {
        // Build the palette once up-front
        val palette = customPalette ?: buildPalette(pixels, colorCount)

        // Work in floating-point to accumulate error
        val rBuf = FloatArray(pixels.size) { Color.red(pixels[it]).toFloat() }
        val gBuf = FloatArray(pixels.size) { Color.green(pixels[it]).toFloat() }
        val bBuf = FloatArray(pixels.size) { Color.blue(pixels[it]).toFloat() }

        val out = pixels.copyOf()

        for (y in 0 until h) {
            for (x in 0 until w) {
                val idx = y * w + x
                val oldR = rBuf[idx].roundToInt().coerceIn(0, 255)
                val oldG = gBuf[idx].roundToInt().coerceIn(0, 255)
                val oldB = bBuf[idx].roundToInt().coerceIn(0, 255)

                val quantColor = palette.minByOrNull {
                    colorDistanceSq(Color.rgb(oldR, oldG, oldB), it)
                } ?: Color.rgb(oldR, oldG, oldB)

                out[idx] = Color.argb(Color.alpha(pixels[idx]), Color.red(quantColor), Color.green(quantColor), Color.blue(quantColor))

                val errR = oldR - Color.red(quantColor).toFloat()
                val errG = oldG - Color.green(quantColor).toFloat()
                val errB = oldB - Color.blue(quantColor).toFloat()

                fun spread(dx: Int, dy: Int, factor: Float) {
                    val nx = x + dx; val ny = y + dy
                    if (nx in 0 until w && ny in 0 until h) {
                        val ni = ny * w + nx
                        rBuf[ni] += errR * factor
                        gBuf[ni] += errG * factor
                        bBuf[ni] += errB * factor
                    }
                }
                spread( 1,  0, 7f / 16)
                spread(-1,  1, 3f / 16)
                spread( 0,  1, 5f / 16)
                spread( 1,  1, 1f / 16)
            }
        }
        return out
    }

    /** Build a colour palette using median-cut (reused for dithering). */
    private fun buildPalette(pixels: IntArray, colorCount: Int): List<Int> {
        val boxes: ArrayDeque<ColorBox> = ArrayDeque<ColorBox>().also {
            it += ColorBox(pixels.toMutableList())
        }
        while (boxes.size < colorCount) {
            val box = boxes.maxByOrNull { it.largestRange } ?: break
            val split = box.split() ?: break
            boxes.remove(box); boxes.addAll(split)
        }
        return boxes.map { it.averageColor() }
    }

    // ── Grid drawing ─────────────────────────────────────────────────────────

    /**
     * Draw 1-px separator lines on [canvas] for every [pixelSize] interval.
     */
    private fun drawGrid(canvas: Canvas, w: Int, h: Int, pixelSize: Int) {
        val paint = Paint().apply {
            color = Color.argb(77, 0, 0, 0)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        var x = 0f
        while (x <= w) { canvas.drawLine(x, 0f, x, h.toFloat(), paint); x += pixelSize }
        var y = 0f
        while (y <= h) { canvas.drawLine(0f, y, w.toFloat(), y, paint); y += pixelSize }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Utilities
    // ─────────────────────────────────────────────────────────────────────────

    private fun bitmapToIntArray(bmp: Bitmap): IntArray {
        val arr = IntArray(bmp.width * bmp.height)
        bmp.getPixels(arr, 0, bmp.width, 0, 0, bmp.width, bmp.height)
        return arr
    }

    /** Squared Euclidean distance in RGB — sufficient for nearest-colour lookups. */
    private fun colorDistanceSq(a: Int, b: Int): Int {
        val dr = Color.red(a) - Color.red(b)
        val dg = Color.green(a) - Color.green(b)
        val db = Color.blue(a) - Color.blue(b)
        return dr * dr + dg * dg + db * db
    }

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
}

// ─────────────────────────────────────────────────────────────────────────────
//  ColorBox — internal helper for median-cut
// ─────────────────────────────────────────────────────────────────────────────

private class ColorBox(private val pixels: MutableList<Int>) {

    var largestRange: Int = 0
        private set

    private var splitChannel: Int = 0

    init { recompute() }

    private fun recompute() {
        var minR = 255; var minG = 255; var minB = 255
        var maxR = 0;   var maxG = 0;   var maxB = 0

        for (p in pixels) {
            minR = minOf(minR, Color.red(p));   maxR = maxOf(maxR, Color.red(p))
            minG = minOf(minG, Color.green(p)); maxG = maxOf(maxG, Color.green(p))
            minB = minOf(minB, Color.blue(p));  maxB = maxOf(maxB, Color.blue(p))
        }

        val rangeR = maxR - minR
        val rangeG = maxG - minG
        val rangeB = maxB - minB

        largestRange = maxOf(rangeR, rangeG, rangeB)
        splitChannel = when (largestRange) {
            rangeR -> 0
            rangeG -> 1
            else   -> 2
        }
    }

    fun averageColor(): Int {
        if (pixels.isEmpty()) return Color.BLACK
        var r = 0L; var g = 0L; var b = 0L; var a = 0L
        for (p in pixels) {
            r += Color.red(p); g += Color.green(p)
            b += Color.blue(p); a += Color.alpha(p)
        }
        val n = pixels.size
        return Color.argb(
            (a / n).toInt(), (r / n).toInt(), (g / n).toInt(), (b / n).toInt()
        )
    }

    fun split(): List<ColorBox>? {
        if (pixels.size < 2) return null
        val ch = splitChannel
        pixels.sortBy { p ->
            when (ch) { 0 -> Color.red(p); 1 -> Color.green(p); else -> Color.blue(p) }
        }
        val mid = pixels.size / 2
        return listOf(
            ColorBox(pixels.subList(0, mid).toMutableList()),
            ColorBox(pixels.subList(mid, pixels.size).toMutableList()),
        )
    }
}