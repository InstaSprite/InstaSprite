package com.instasprite.app.data.model

import androidx.room.TypeConverter
import java.nio.ByteBuffer

class IntListConverter {

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
