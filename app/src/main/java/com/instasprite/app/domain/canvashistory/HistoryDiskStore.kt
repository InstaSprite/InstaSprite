package com.instasprite.app.domain.canvashistory

import com.instasprite.app.HistoryEntryProto
import com.instasprite.app.domain.canvashistory.HistoryEntryMapper.toDomain
import com.instasprite.app.domain.canvashistory.HistoryEntryMapper.toProto
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class HistoryDiskStore(private val historyDir: File) {

    var undoCount: Int = 0
        private set

    var redoCount: Int = 0
        private set

    fun appendUndo(entry: HistoryEntry) {
        try {
            historyDir.mkdirs()
            val file = undoFile(undoCount)
            FileOutputStream(file).use { output ->
                with(HistoryEntryMapper) {
                    entry.toProto().writeTo(output)
                }
            }
            undoCount++
        } catch (_: Exception) {
        }
    }

    fun popUndo(): HistoryEntry? {
        if (undoCount == 0) return null
        return try {
            undoCount--
            val file = undoFile(undoCount)
            val proto = FileInputStream(file).use { input ->
                HistoryEntryProto.parseFrom(input)
            }
            file.delete()
            with(HistoryEntryMapper) {
                proto.toDomain()
            }
        } catch (_: Exception) {
            null
        }
    }

    fun appendRedo(entry: HistoryEntry) {
        try {
            historyDir.mkdirs()
            val file = redoFile(redoCount)
            FileOutputStream(file).use { output ->
                with(HistoryEntryMapper) {
                    entry.toProto().writeTo(output)
                }
            }
            redoCount++
        } catch (_: Exception) {
        }
    }

    fun popRedo(): HistoryEntry? {
        if (redoCount == 0) return null
        return try {
            redoCount--
            val file = redoFile(redoCount)
            val proto = FileInputStream(file).use { input ->
                HistoryEntryProto.parseFrom(input)
            }
            file.delete()
            with(HistoryEntryMapper) {
                proto.toDomain()
            }
        } catch (_: Exception) {
            null
        }
    }

    fun clearRedo() {
        for (i in 0 until redoCount) {
            redoFile(i).delete()
        }
        redoCount = 0
    }

    fun clearAll() {
        for (i in 0 until undoCount) {
            undoFile(i).delete()
        }
        for (i in 0 until redoCount) {
            redoFile(i).delete()
        }
        undoCount = 0
        redoCount = 0
    }

    fun destroy() {
        historyDir.deleteRecursively()
    }

    private fun undoFile(index: Int): File {
        return File(historyDir, String.format("undo_%03d.pb", index))
    }

    private fun redoFile(index: Int): File {
        return File(historyDir, String.format("redo_%03d.pb", index))
    }
}
