package com.olaz.instasprite.utils

import androidx.compose.ui.graphics.Color
import com.olaz.instasprite.data.database.ColorPaletteDao
import com.olaz.instasprite.data.model.ColorPaletteData
import com.olaz.instasprite.data.network.lospec.LospecService
import com.olaz.instasprite.data.network.lospec.model.PaletteDto
import com.olaz.instasprite.domain.model.ColorPalette
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

        }

        class MockLospecService() : LospecService {
            override suspend fun getPalette(paletteName: String): PaletteDto {
                return PaletteDto()
            }
        }
    }
}