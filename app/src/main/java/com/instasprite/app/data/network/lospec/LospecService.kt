package com.instasprite.app.data.network.lospec

import com.instasprite.app.data.network.lospec.model.PaletteDto
import retrofit2.http.GET
import retrofit2.http.Path

interface LospecService {
    @GET("/palette-list/{paletteName}.json")
    suspend fun getPalette(@Path("paletteName") paletteName: String): PaletteDto
}
