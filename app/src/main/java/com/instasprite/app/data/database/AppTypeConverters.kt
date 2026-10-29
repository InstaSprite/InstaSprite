package com.instasprite.app.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.ByteBuffer

class AppTypeConverters {
    private val gson = Gson()


    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, type)
        }
    }

    @TypeConverter
    fun fromIntList(list: List<Int>?): ByteArray? {
        if (list == null) return null
        val buffer = ByteBuffer.allocate(list.size * 4)
        list.forEach { buffer.putInt(it) }
        return buffer.array()
    }

    @TypeConverter
    fun toIntList(bytes: ByteArray?): List<Int>? {
        if (bytes == null) return null
        val buffer = ByteBuffer.wrap(bytes)
        val list = mutableListOf<Int>()
        while (buffer.hasRemaining()) {
            list.add(buffer.int)
        }
        return list
    }
}
