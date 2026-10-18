package com.instasprite.app.domain.canvashistory

import android.graphics.Rect
import com.google.protobuf.ByteString
import com.instasprite.app.AnimationCelData
import com.instasprite.app.AnimationImageData
import com.instasprite.app.CelEditEntryProto
import com.instasprite.app.HistoryCanvasStateProto
import com.instasprite.app.HistoryEntryProto
import com.instasprite.app.ISprite
import com.instasprite.app.LayerProto
import com.instasprite.app.OperationEntryProto
import com.instasprite.app.SelectionStateProto
import com.instasprite.app.TileCoordProto
import com.instasprite.app.TileDeltaProto
import com.instasprite.app.TransformEntryProto
import com.instasprite.app.TransformTypeProto
import com.instasprite.app.UndoEntryProto
import com.instasprite.app.data.mapper.toISprite
import com.instasprite.app.data.mapper.toSprite
import com.instasprite.app.domain.model.AnimationCel
import com.instasprite.app.domain.model.BlendMode
import com.instasprite.app.domain.model.CelImage
import com.instasprite.app.domain.model.Layer
import com.instasprite.app.domain.model.SelectionState
import com.instasprite.app.domain.model.TileCoord
import com.instasprite.app.historyCanvasStateProto
import com.instasprite.app.historyEntryProto
import com.instasprite.app.layerProto
import com.instasprite.app.operationEntryProto
import com.instasprite.app.selectionStateProto
import com.instasprite.app.tileCoordProto
import com.instasprite.app.tileDeltaProto
import com.instasprite.app.transformEntryProto
import com.instasprite.app.undoEntryProto
import com.instasprite.app.utils.decodeTilesFromByteArray
import com.instasprite.app.utils.encodeTilesToByteArray
import java.nio.ByteBuffer
import java.nio.ByteOrder

object HistoryEntryMapper {

    fun HistoryEntry.toProto(): HistoryEntryProto = historyEntryProto {
        when (val entry = this@toProto) {
            is CelEditEntry -> celEditEntry = entry.toCelEditProto()
            is UndoEntry -> undoEntry = entry.toUndoEntryProto()
            is OperationEntry -> operationEntry = entry.toOperationEntryProto()
            is TransformEntry -> transformEntry = entry.toTransformEntryProto()
        }
    }

    fun HistoryEntryProto.toDomain(): HistoryEntry {
        return when (entryCase) {
            HistoryEntryProto.EntryCase.CEL_EDIT_ENTRY -> celEditEntry.toCelEdit()

            HistoryEntryProto.EntryCase.UNDO_ENTRY ->
                undoEntry.toDomainUndoEntry()

            HistoryEntryProto.EntryCase.OPERATION_ENTRY ->
                operationEntry.toDomainOperationEntry()

            HistoryEntryProto.EntryCase.TRANSFORM_ENTRY ->
                transformEntry.toDomainTransformEntry()

            HistoryEntryProto.EntryCase.ENTRY_NOT_SET, null ->
                error("HistoryEntryProto has no entry set")
        }
    }

    private fun AnimationCel.toCelProto(): AnimationCelData = AnimationCelData.newBuilder()
        .setId(id).setLayerId(layerId).setFrameId(frameId).setDataId(dataId).build()

    private fun AnimationCelData.toCel() = AnimationCel(id, layerId, frameId, dataId)

    private fun CelImage.toImageProto(): AnimationImageData = AnimationImageData.newBuilder()
        .setId(id).setTiles(ByteString.copyFrom(encodeTilesToByteArray(tiles))).build()

    private fun AnimationImageData.toImage() = CelImage(id,
        requireNotNull(decodeTilesFromByteArray(tiles.toByteArray())))

    private fun CelEditEntry.toCelEditProto(): CelEditEntryProto {
        val builder = CelEditEntryProto.newBuilder().setLayerId(layerId).setFrameId(frameId)
        beforeCel?.let { builder.setBeforeCel(it.toCelProto()) }
        afterCel?.let { builder.setAfterCel(it.toCelProto()) }
        beforeImagePatch?.let { builder.setBeforeImage(it.toImageProto()) }
        afterImagePatch?.let { builder.setAfterImage(it.toImageProto()) }
        beforeSelection?.let { builder.setBeforeSelection(it.toSelectionStateProto()) }
        afterSelection?.let { builder.setAfterSelection(it.toSelectionStateProto()) }
        return builder.build()
    }

    private fun CelEditEntryProto.toCelEdit() = CelEditEntry(layerId, frameId,
        if (hasBeforeCel()) beforeCel.toCel() else null,
        if (hasAfterCel()) afterCel.toCel() else null,
        if (hasBeforeImage()) beforeImage.toImage() else null,
        if (hasAfterImage()) afterImage.toImage() else null,
        if (hasBeforeSelection()) beforeSelection.toDomainSelectionState() else null,
        if (hasAfterSelection()) afterSelection.toDomainSelectionState() else null)

    private fun UndoEntry.toUndoEntryProto(): UndoEntryProto = undoEntryProto {
        deltas.addAll(this@toUndoEntryProto.deltas.map { it.toTileDeltaProto() })
        hasBeforeSelection = this@toUndoEntryProto.beforeSelection != null
        if (this@toUndoEntryProto.beforeSelection != null) {
            beforeSelection = this@toUndoEntryProto.beforeSelection.toSelectionStateProto()
        }
        hasAfterSelection = this@toUndoEntryProto.afterSelection != null
        if (this@toUndoEntryProto.afterSelection != null) {
            afterSelection = this@toUndoEntryProto.afterSelection.toSelectionStateProto()
        }
    }

    private fun UndoEntryProto.toDomainUndoEntry(): UndoEntry = UndoEntry(
        deltas = deltasList.map { it.toDomainTileDelta() },
        beforeSelection = if (hasBeforeSelection) {
            beforeSelection.toDomainSelectionState()
        } else {
            null
        },
        afterSelection = if (hasAfterSelection) {
            afterSelection.toDomainSelectionState()
        } else {
            null
        }
    )

    private fun TileDelta.toTileDeltaProto(): TileDeltaProto = tileDeltaProto {
        layerId = this@toTileDeltaProto.layerId
        coord = this@toTileDeltaProto.coord.toTileCoordProto()
        beforePixels = intArrayToByteString(this@toTileDeltaProto.before)
    }

    private fun TileDeltaProto.toDomainTileDelta(): TileDelta = TileDelta(
        layerId = layerId,
        coord = coord.toDomainTileCoord(),
        before = byteStringToIntArray(beforePixels)
    )

    private fun TileCoord.toTileCoordProto(): TileCoordProto = tileCoordProto {
        x = this@toTileCoordProto.x
        y = this@toTileCoordProto.y
    }

    private fun TileCoordProto.toDomainTileCoord(): TileCoord = TileCoord(
        x = x,
        y = y
    )

    private fun SelectionState.toSelectionStateProto(): SelectionStateProto =
        selectionStateProto {
            mask = booleanArrayToByteString(this@toSelectionStateProto.mask)
            boundsLeft = this@toSelectionStateProto.bounds.left
            boundsTop = this@toSelectionStateProto.bounds.top
            boundsRight = this@toSelectionStateProto.bounds.right
            boundsBottom = this@toSelectionStateProto.bounds.bottom
            canvasWidth = this@toSelectionStateProto.canvasWidth
            canvasHeight = this@toSelectionStateProto.canvasHeight
        }

    private fun SelectionStateProto.toDomainSelectionState(): SelectionState =
        SelectionState(
            mask = byteStringToBooleanArray(mask, canvasWidth * canvasHeight),
            bounds = Rect(boundsLeft, boundsTop, boundsRight, boundsBottom),
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight
        )

    private fun OperationEntry.toOperationEntryProto(): OperationEntryProto =
        operationEntryProto {
            before = this@toOperationEntryProto.before.toHistoryCanvasStateProto()
            after = this@toOperationEntryProto.after.toHistoryCanvasStateProto()
        }

    private fun OperationEntryProto.toDomainOperationEntry(): OperationEntry =
        OperationEntry(
            before = before.toDomainHistoryCanvasState(),
            after = after.toDomainHistoryCanvasState()
        )

    private fun HistoryCanvasState.toHistoryCanvasStateProto(): HistoryCanvasStateProto =
        historyCanvasStateProto {
            this@toHistoryCanvasStateProto.sprite?.let {
                spriteDocument = ByteString.copyFrom(it.toISprite().toByteArray())
            }
            activeFrameId = this@toHistoryCanvasStateProto.activeFrameId
            width = this@toHistoryCanvasStateProto.width
            height = this@toHistoryCanvasStateProto.height
            if (this@toHistoryCanvasStateProto.sprite == null) {
                layers.addAll(this@toHistoryCanvasStateProto.layers.map { it.toLayerProto() })
            }
            activeLayerId = this@toHistoryCanvasStateProto.activeLayerId
            hasSelectionState = this@toHistoryCanvasStateProto.selectionState != null
            if (this@toHistoryCanvasStateProto.selectionState != null) {
                selectionState =
                    this@toHistoryCanvasStateProto.selectionState.toSelectionStateProto()
            }
        }

    private fun HistoryCanvasStateProto.toDomainHistoryCanvasState(): HistoryCanvasState =
        HistoryCanvasState(
            width = width,
            height = height,
            layers = layersList.map { it.toDomainLayer() },
            sprite = if (spriteDocument.isEmpty) null else ISprite.parseFrom(spriteDocument).toSprite(),
            activeFrameId = activeFrameId,
            activeLayerId = activeLayerId,
            selectionState = if (hasSelectionState) {
                selectionState.toDomainSelectionState()
            } else {
                null
            }
        )

    private fun Layer.toLayerProto(): LayerProto = layerProto {
        id = this@toLayerProto.id
        name = this@toLayerProto.name
        isVisible = this@toLayerProto.isVisible
        isLocked = this@toLayerProto.isLocked
        opacity = this@toLayerProto.opacity
        blendMode = this@toLayerProto.blendMode.name
        tileData = ByteString.copyFrom(
            encodeTilesToByteArray(this@toLayerProto.tiles)
        )
    }

    private fun LayerProto.toDomainLayer(): Layer = Layer(
        id = id,
        name = name,
        isVisible = isVisible,
        isLocked = isLocked,
        opacity = opacity,
        blendMode = BlendMode.valueOf(blendMode),
        tiles = decodeTilesFromByteArray(tileData.toByteArray()) ?: emptyMap()
    )

    private fun TransformEntry.toTransformEntryProto(): TransformEntryProto =
        transformEntryProto {
            transform = when (this@toTransformEntryProto.transform) {
                TransformType.ROTATE_CW -> TransformTypeProto.ROTATE_CW
                TransformType.ROTATE_CCW -> TransformTypeProto.ROTATE_CCW
                TransformType.FLIP_H -> TransformTypeProto.FLIP_H
                TransformType.FLIP_V -> TransformTypeProto.FLIP_V
            }
        }

    private fun TransformEntryProto.toDomainTransformEntry(): TransformEntry =
        TransformEntry(
            transform = when (transform) {
                TransformTypeProto.ROTATE_CW -> TransformType.ROTATE_CW
                TransformTypeProto.ROTATE_CCW -> TransformType.ROTATE_CCW
                TransformTypeProto.FLIP_H -> TransformType.FLIP_H
                TransformTypeProto.FLIP_V -> TransformType.FLIP_V
                TransformTypeProto.UNRECOGNIZED ->
                    error("Unrecognized TransformTypeProto value")
            }
        )

    private fun intArrayToByteString(arr: IntArray): ByteString {
        val buffer = ByteBuffer.allocate(arr.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        for (value in arr) {
            buffer.putInt(value)
        }
        return ByteString.copyFrom(buffer.array())
    }

    private fun byteStringToIntArray(bs: ByteString): IntArray {
        val bytes = bs.toByteArray()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val result = IntArray(bytes.size / 4)
        for (i in result.indices) {
            result[i] = buffer.int
        }
        return result
    }

    private fun booleanArrayToByteString(arr: BooleanArray): ByteString {
        val byteCount = (arr.size + 7) / 8
        val bytes = ByteArray(byteCount)
        for (i in arr.indices) {
            if (arr[i]) {
                bytes[i / 8] = (bytes[i / 8].toInt() or (1 shl (i % 8))).toByte()
            }
        }
        return ByteString.copyFrom(bytes)
    }

    private fun byteStringToBooleanArray(bs: ByteString, size: Int): BooleanArray {
        val bytes = bs.toByteArray()
        val result = BooleanArray(size)
        for (i in 0 until size) {
            if (i / 8 < bytes.size) {
                result[i] = (bytes[i / 8].toInt() and (1 shl (i % 8))) != 0
            }
        }
        return result
    }
}
