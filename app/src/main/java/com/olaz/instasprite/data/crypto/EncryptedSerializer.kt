package com.olaz.instasprite.data.crypto

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64

open class EncryptedSerializer<T>(
    private val kSerializer: KSerializer<T>,
    override val defaultValue: T
) : Serializer<T> {

    override suspend fun readFrom(input: InputStream): T {
        return try {
            val encryptedBytes = withContext(Dispatchers.IO) {
                input.readBytes()
            }
            if (encryptedBytes.isEmpty()) return defaultValue

            val encryptedBytesDecoded = Base64.getDecoder().decode(encryptedBytes)
            val decryptedBytes = Crypto.decrypt(encryptedBytesDecoded)
            val decodedJsonString = decryptedBytes.decodeToString()

            Json.decodeFromString(kSerializer, decodedJsonString)
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        val json = Json.encodeToString(kSerializer, t)

        val bytes = json.toByteArray()
        val encryptedBytes = Crypto.encrypt(bytes)
        val encryptedBytesBase64 = Base64.getEncoder().encode(encryptedBytes)

        withContext(Dispatchers.IO) {
            output.write(encryptedBytesBase64)
        }
    }
}

open class PlainSerializer<T>(
    private val kSerializer: KSerializer<T>,
    override val defaultValue: T
) : Serializer<T> {

    override suspend fun readFrom(input: InputStream): T {
        return try {
            val bytes = withContext(Dispatchers.IO) {
                input.readBytes()
            }
            if (bytes.isEmpty()) return defaultValue

            val jsonString = bytes.decodeToString()
            Json.decodeFromString(kSerializer, jsonString)
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        val json = Json.encodeToString(kSerializer, t)
        val bytes = json.toByteArray()

        withContext(Dispatchers.IO) {
            output.write(bytes)
        }
    }
}