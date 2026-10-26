package com.instasprite.app.domain.canvashistory

import com.instasprite.app.HistoryEntryProto
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class HistoryDiskStore(private val historyDir: File) {

    init {
        val historyFileName = Regex("(undo|redo)_\\d+\\.pb")
        historyDir.listFiles()?.forEach { file ->
            if (file.isFile && historyFileName.matches(file.name)) file.delete()
        }
    }

    var undoCount: Int = 0
        private set

    var redoCount: Int = 0
        private set

    fun appendUndo(entry: HistoryEntry): Boolean {
        return try {
            historyDir.mkdirs()
            val file = undoFile(undoCount)
            FileOutputStream(file).use { output ->
                with(HistoryEntryMapper) {
                    entry.toProto().writeTo(output)
                }
            }
            undoCount++
            true
        } catch (_: Exception) {
            undoFile(undoCount).delete()
            false
        }
    }

    fun popUndo(): HistoryEntry? {
        if (undoCount == 0) return null
        return try {
            val file = undoFile(undoCount - 1)
            val proto = FileInputStream(file).use { input ->
                HistoryEntryProto.parseFrom(input)
            }
            val entry = with(HistoryEntryMapper) {
                proto.toDomain()
            }
            undoCount--
            file.delete()
            entry
        } catch (_: Exception) {
            null
        }
    }

    fun appendRedo(entry: HistoryEntry): Boolean {
        return try {
            historyDir.mkdirs()
            val file = redoFile(redoCount)
            FileOutputStream(file).use { output ->
                with(HistoryEntryMapper) {
                    entry.toProto().writeTo(output)
                }
            }
            redoCount++
            true
        } catch (_: Exception) {
            redoFile(redoCount).delete()
            false
        }
    }

    fun popRedo(): HistoryEntry? {
        if (redoCount == 0) return null
        return try {
            val file = redoFile(redoCount - 1)
            val proto = FileInputStream(file).use { input ->
                HistoryEntryProto.parseFrom(input)
            }
            val entry = with(HistoryEntryMapper) {
                proto.toDomain()
            }
            redoCount--
            file.delete()
            entry
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
