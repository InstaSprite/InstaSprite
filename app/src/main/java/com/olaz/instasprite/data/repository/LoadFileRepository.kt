package com.olaz.instasprite.data.repository

import android.content.Context
import android.net.Uri
import com.olaz.instasprite.data.mapper.toDomain
import com.olaz.instasprite.data.model.SpriteData
import com.olaz.instasprite.domain.model.Sprite
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException

object LoadFileRepository {
    fun loadFile(context: Context, fileUri: Uri): Sprite? {
        return try {
            val inputStream = context.contentResolver.openInputStream(fileUri) ?: return null
            val jsonData = inputStream.bufferedReader().use { it.readText() }
            val spriteData = Json.decodeFromString<SpriteData>(jsonData)
            spriteData.toDomain()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: SerializationException) {
            e.printStackTrace()
            null
        }
    }
}