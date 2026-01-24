package com.olaz.instasprite.data.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.olaz.instasprite.SpritePixels
import java.io.InputStream
import java.io.OutputStream

object SpritePixelsSerializer : Serializer<SpritePixels> {
    override val defaultValue: SpritePixels = SpritePixels.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): SpritePixels {
        try {
            return SpritePixels.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: SpritePixels, output: OutputStream) {
        t.writeTo(output)
    }
}
