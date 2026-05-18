package com.instasprite.app.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.instasprite.app.domain.model.PixelCanvas
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PixelCanvasRepositoryTest {

    private lateinit var repo: PixelCanvasRepository
    private val red = Color.Red.toArgb()
    private val blue = Color.Blue.toArgb()
    private val green = Color.Green.toArgb()
    private val transparent = Color.Transparent.toArgb()

    @Before
    fun setUp() {
        // Small 4x4 canvas for easy reasoning
        repo = PixelCanvasRepository(PixelCanvas(width = 4, height = 4))
    }

    // ============================
    // UC-DRAW-02: Pixel Read/Write
    // ============================

    @Test
    fun `setPixel and getPixel round-trip`() {
        repo.setPixel(0, 0, Color.Red)
        assertEquals(Color.Red, repo.getPixel(0, 0))
    }

    @Test
    fun `getPixel on empty canvas returns Transparent`() {
        assertEquals(Color.Transparent, repo.getPixel(0, 0))
    }

    @Test
    fun `setPixel out of bounds is ignored`() {
        repo.setPixel(-1, 0, Color.Red)
        repo.setPixel(0, -1, Color.Red)
        repo.setPixel(4, 0, Color.Red) // height = 4, so row 4 is out
        repo.setPixel(0, 4, Color.Red) // width = 4, so col 4 is out
        // No exception, and canvas remains empty
        val pixels = repo.getAllPixels()
        assertTrue(pixels.all { it == transparent })
    }

    @Test
    fun `setPixel with scale draws a block`() {
        repo.setPixel(1, 1, Color.Red, scale = 3)
        // Scale 3 should write a 3x3 block centered on (1,1) → rows 0-2, cols 0-2
        assertEquals(Color.Red, repo.getPixel(0, 0))
        assertEquals(Color.Red, repo.getPixel(0, 1))
        assertEquals(Color.Red, repo.getPixel(0, 2))
        assertEquals(Color.Red, repo.getPixel(1, 0))
        assertEquals(Color.Red, repo.getPixel(1, 1))
        assertEquals(Color.Red, repo.getPixel(1, 2))
        assertEquals(Color.Red, repo.getPixel(2, 0))
        assertEquals(Color.Red, repo.getPixel(2, 1))
        assertEquals(Color.Red, repo.getPixel(2, 2))
        // (3,3) should still be transparent
        assertEquals(Color.Transparent, repo.getPixel(3, 3))
    }

    @Test
    fun `getAllPixels returns composited pixels`() {
        repo.setPixel(0, 0, Color.Red)
        repo.setPixel(3, 3, Color.Blue)
        val pixels = repo.getAllPixels()
        assertEquals(16, pixels.size) // 4x4
        assertEquals(red, pixels[0])          // row 0, col 0
        assertEquals(blue, pixels[15])        // row 3, col 3
        assertEquals(transparent, pixels[1])  // row 0, col 1
    }

    @Test
    fun `getCompositedPixelAt returns correct value`() {
        repo.setPixel(2, 3, Color.Green)
        assertEquals(green, repo.getCompositedPixelAt(2, 3))
        assertEquals(transparent, repo.getCompositedPixelAt(0, 0))
    }

    // ============================
    // UC-DRAW-03: Layer Management
    // ============================

    @Test
    fun `init creates one layer`() {
        assertEquals(1, repo.layers.size)
    }

    @Test
    fun `addLayer increases count and switches active`() {
        val firstId = repo.activeLayerId
        repo.addLayer("Layer 2")
        assertEquals(2, repo.layers.size)
        assertNotEquals(firstId, repo.activeLayerId)
    }

    @Test
    fun `removeLayer decreases count`() {
        repo.addLayer("Layer 2")
        assertEquals(2, repo.layers.size)
        val secondId = repo.activeLayerId
        repo.removeLayer(secondId)
        assertEquals(1, repo.layers.size)
    }

    @Test
    fun `cannot remove the last remaining layer`() {
        val onlyId = repo.layers[0].id
        repo.removeLayer(onlyId)
        assertEquals(1, repo.layers.size) // Still 1
    }

    @Test
    fun `toggleVisibility flips layer visibility`() {
        val id = repo.layers[0].id
        assertTrue(repo.layers[0].isVisible)
        repo.toggleVisibility(id)
        assertFalse(repo.layers[0].isVisible)
        repo.toggleVisibility(id)
        assertTrue(repo.layers[0].isVisible)
    }

    @Test
    fun `toggleLock flips layer lock`() {
        val id = repo.layers[0].id
        assertFalse(repo.layers[0].isLocked)
        repo.toggleLock(id)
        assertTrue(repo.layers[0].isLocked)
    }

    @Test
    fun `locked layer rejects setPixel`() {
        repo.toggleLock(repo.activeLayerId)
        repo.setPixel(0, 0, Color.Red)
        assertEquals(Color.Transparent, repo.getPixel(0, 0))
    }

    @Test
    fun `hidden layer is skipped in compositing`() {
        // Draw red on layer 1
        repo.setPixel(0, 0, Color.Red)
        // Hide layer 1
        repo.toggleVisibility(repo.activeLayerId)
        // Composited should be transparent
        assertEquals(transparent, repo.getCompositedPixelAt(0, 0))
    }

    @Test
    fun `setActiveLayer switches active layer`() {
        repo.addLayer("Layer 2")
        val layer2Id = repo.activeLayerId
        val layer1Id = repo.layers[0].id

        repo.setActiveLayer(layer1Id)
        assertEquals(layer1Id, repo.activeLayerId)

        repo.setActiveLayer(layer2Id)
        assertEquals(layer2Id, repo.activeLayerId)
    }

    @Test
    fun `mergeLayerDown combines tiles`() {
        // Draw red on layer 1
        repo.setPixel(0, 0, Color.Red)
        // Add layer 2 and draw blue at same position
        repo.addLayer("Layer 2")
        repo.setPixel(0, 0, Color.Blue)

        val topId = repo.activeLayerId
        repo.mergeLayerDown(topId)

        assertEquals(1, repo.layers.size)
        // Blue (top) should overwrite Red (bottom)
        assertEquals(blue, repo.getCompositedPixelAt(0, 0))
    }

    @Test
    fun `reorderLayer swaps layers`() {
        repo.addLayer("Layer 2")
        val layer1Name = repo.layers[0].name
        val layer2Name = repo.layers[1].name
        repo.reorderLayer(0, 1)
        assertEquals(layer2Name, repo.layers[0].name)
        assertEquals(layer1Name, repo.layers[1].name)
    }

    // ============================
    // UC-DRAW-06: Canvas Transforms
    // ============================

    @Test
    fun `rotate swaps width and height`() {
        repo = PixelCanvasRepository(PixelCanvas(width = 4, height = 2))
        assertEquals(4, repo.width)
        assertEquals(2, repo.height)
        repo.rotate()
        assertEquals(2, repo.width)
        assertEquals(4, repo.height)
    }

    @Test
    fun `rotate moves pixel to correct position`() {
        // On a 4x2 canvas, pixel at (row=0, col=1) should move after CW rotation
        repo = PixelCanvasRepository(PixelCanvas(width = 4, height = 2))
        repo.setPixel(0, 1, Color.Red)
        repo.rotate()
        // After 90° CW: new_row = old_col, new_col = old_height - 1 - old_row
        // (0,1) → (1, 1) on a 2x4 canvas
        assertEquals(red, repo.getCompositedPixelAt(1, 1))
    }

    @Test
    fun `horizontalFlip mirrors pixels`() {
        repo.setPixel(0, 0, Color.Red)
        repo.horizontalFlip()
        // (0,0) → (0, width-1) = (0, 3)
        assertEquals(red, repo.getCompositedPixelAt(0, 3))
        assertEquals(transparent, repo.getCompositedPixelAt(0, 0))
    }

    @Test
    fun `verticalFlip mirrors pixels`() {
        repo.setPixel(0, 0, Color.Red)
        repo.verticalFlip()
        // (0,0) → (height-1, 0) = (3, 0)
        assertEquals(red, repo.getCompositedPixelAt(3, 0))
        assertEquals(transparent, repo.getCompositedPixelAt(0, 0))
    }

    @Test
    fun `resizeCanvas grows correctly`() {
        repo.setPixel(0, 0, Color.Red)
        repo.resizeCanvas(8, 8)
        assertEquals(8, repo.width)
        assertEquals(8, repo.height)
        // Original pixel preserved
        assertEquals(red, repo.getCompositedPixelAt(0, 0))
        // New area is transparent
        assertEquals(transparent, repo.getCompositedPixelAt(7, 7))
    }

    @Test
    fun `resizeCanvas shrinks and clips`() {
        repo.setPixel(3, 3, Color.Red)
        repo.resizeCanvas(2, 2)
        assertEquals(2, repo.width)
        assertEquals(2, repo.height)
        // Pixel at (3,3) is now outside the canvas — lost
        val pixels = repo.getAllPixels()
        assertTrue(pixels.all { it == transparent })
    }

    @Test
    fun `resizeCanvas to zero is ignored`() {
        repo.resizeCanvas(0, 0)
        assertEquals(4, repo.width) // unchanged
    }

    // ============================
    // Batch Operations
    // ============================

    @Test
    fun `batchSetPixels writes multiple pixels`() {
        val indices = intArrayOf(0, 5, 15) // (0,0), (1,1), (3,3)
        val colors = intArrayOf(red, green, blue)
        repo.batchSetPixels(indices, colors, 3)

        assertEquals(red, repo.getCompositedPixelAt(0, 0))
        assertEquals(green, repo.getCompositedPixelAt(1, 1))
        assertEquals(blue, repo.getCompositedPixelAt(3, 3))
    }

    @Test
    fun `batchSetPixels on locked layer does nothing`() {
        repo.toggleLock(repo.activeLayerId)
        val indices = intArrayOf(0)
        val colors = intArrayOf(red)
        repo.batchSetPixels(indices, colors, 1)
        assertEquals(transparent, repo.getCompositedPixelAt(0, 0))
    }

    // ============================
    // setCanvas / setCanvasData
    // ============================

    @Test
    fun `setCanvas resets to single layer`() {
        repo.addLayer("Layer 2")
        assertEquals(2, repo.layers.size)
        repo.setCanvas(8, 8)
        assertEquals(1, repo.layers.size)
        assertEquals(8, repo.width)
        assertEquals(8, repo.height)
    }

    @Test
    fun `getSprite returns correct dimensions and layers`() {
        repo.setPixel(0, 0, Color.Red)
        repo.addLayer("Layer 2")
        val sprite = repo.getSprite()
        assertEquals(4, sprite.width)
        assertEquals(4, sprite.height)
        assertEquals(2, sprite.layers.size)
    }
}
