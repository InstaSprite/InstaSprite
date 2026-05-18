package com.instasprite.app.data.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.instasprite.app.ISprite
import java.io.InputStream
import java.io.OutputStream
import java.io.PushbackInputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object ISpriteSerializer : Serializer<ISprite> {
    override val defaultValue: ISprite = ISprite.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ISprite {
        val pushbackStream = PushbackInputStream(input, 2)
        val header = ByteArray(2)
        val bytesRead = pushbackStream.read(header)

        if (bytesRead > 0) {
            pushbackStream.unread(header, 0, bytesRead)
        }

        // gzip magic number
        val isCompressed = bytesRead == 2 &&
                (header[0].toInt() and 0xFF == 0x1F) &&
                (header[1].toInt() and 0xFF == 0x8B)

        return try {
            if (isCompressed) {
                GZIPInputStream(pushbackStream).use { gzip ->
                    ISprite.parseFrom(gzip)
                }
            } else {
                ISprite.parseFrom(pushbackStream)
            }
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: ISprite, output: OutputStream) {
        GZIPOutputStream(output).use { gzipStream ->
            t.writeTo(gzipStream)
            gzipStream.finish()
        }
    }
}
