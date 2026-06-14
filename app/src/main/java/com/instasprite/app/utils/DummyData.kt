package com.instasprite.app.utils

import androidx.compose.ui.graphics.Color
import com.instasprite.app.data.database.ColorPaletteDao
import com.instasprite.app.data.model.ColorPaletteData
import com.instasprite.app.data.network.lospec.LospecService
import com.instasprite.app.data.network.lospec.model.PaletteDto
import com.instasprite.app.domain.model.Cel
import com.instasprite.app.domain.model.ColorPalette
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.domain.model.SpriteMeta
import com.instasprite.app.domain.model.SpriteWithMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


object DummyData {

    val palettes = listOf(
        ColorPalette(
            id = 1,
            colors = mutableListOf(
                Color(0xFF0077BE),
                Color(0xFF0096C7),
                Color(0xFF48CAE4),
                Color(0xFF90E0EF),
                Color(0xFFCAF0F8)
            )
        ),
        ColorPalette(
            id = 2,
            colors = mutableListOf(
                Color(0xFF355070),
                Color(0xFF6D597A),
                Color(0xFFB56576),
                Color(0xFFE56B6F),
                Color(0xFFEAAC8B)
            )
        ),
        ColorPalette(
            id = 3,
            colors = mutableListOf(
                Color(0xFF2D6A4F),
                Color(0xFF40916C),
                Color(0xFF52B788),
                Color(0xFF74C69D),
                Color(0xFF95D5B2)
            )
        )
    )

    val previewSprites = listOf(
        SpriteWithMeta(
            sprite = Sprite(
                id = "p1", width = 16, height = 16,
                layers = listOf(
                    Layer(
                        id = "l1", name = "Layer 1",
                        cel = Cel(
                            x = 0, y = 0, width = 16, height = 16,
                            pixels = IntArray(16 * 16) { 0xFF89B4FA.toInt() }
                        )
                    )
                )
            ),
            meta = SpriteMeta(spriteId = "p1", spriteName = "1")
        ),
        SpriteWithMeta(
            sprite = Sprite(
                id = "p2", width = 16, height = 24,
                layers = listOf(
                    Layer(
                        id = "l2", name = "Layer 1",
                        cel = Cel(
                            x = 0, y = 0, width = 16, height = 24,
                            pixels = IntArray(16 * 24) { 0xFFA6E3A1.toInt() }
                        )
                    )
                )
            ),
            meta = SpriteMeta(spriteId = "p2", spriteName = "2")
        ),
        SpriteWithMeta(
            sprite = Sprite(
                id = "p3", width = 32, height = 16,
                layers = listOf(
                    Layer(
                        id = "l3", name = "Layer 1",
                        cel = Cel(
                            x = 0, y = 0, width = 32, height = 16,
                            pixels = IntArray(32 * 16) { 0xFFF38BA8.toInt() }
                        )
                    )
                )
            ),
            meta = SpriteMeta(spriteId = "p3", spriteName = "3")
        ),
        SpriteWithMeta(
            sprite = Sprite(
                id = "p4", width = 32, height = 32,
                layers = listOf(
                    Layer(
                        id = "l4", name = "Layer 1",
                        cel = Cel(
                            x = 0, y = 0, width = 32, height = 32,
                            pixels = IntArray(32 * 32) { 0xFFCBA6F7.toInt() } // Mauve/Base
                        )
                    )
                )
            ),
            meta = SpriteMeta(spriteId = "p4", spriteName = "4")
        )
    )

    object MockClass {
        class MockColorPaletteDao() : ColorPaletteDao {
            override suspend fun insert(palette: ColorPaletteData) {
            }

            override suspend fun getAllPalette(): List<ColorPaletteData> {
                return emptyList()
            }

            override fun getAllPaletteFlow(): Flow<List<ColorPaletteData>> {
                return flowOf(emptyList())
            }

            override suspend fun getPaletteByName(name: String): ColorPaletteData? {
                return null
            }

            override suspend fun getPaletteById(id: Int): ColorPaletteData? {
                return null
            }


            override suspend fun deletePaletteByName(name: String) {

            }

            override suspend fun deletePaletteById(id: Int) {
            }

            override suspend fun setFavorite(id: Int, isFavorite: Boolean) {
            }

        }

        class MockLospecService() : LospecService {
            override suspend fun getPalette(paletteName: String): PaletteDto {
                return PaletteDto()
            }
        }
    }
}