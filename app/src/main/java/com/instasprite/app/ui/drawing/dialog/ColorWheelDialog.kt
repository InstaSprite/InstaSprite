package com.instasprite.app.ui.drawing.dialog

import com.instasprite.app.utils.pixelDp


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toRect
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.ColorItem
import com.instasprite.app.ui.components.composable.ColorPaletteView
import com.instasprite.app.ui.components.dialog.CustomDialog
import com.instasprite.app.ui.components.shape.PixelShape
import com.instasprite.app.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor


@Composable
fun ColorWheelDialog(
    initialColor: Color = Color.Blue,
    colorPalette: List<Color>,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit,
    onOpenPaletteScreen: () -> Unit,
    showPalette: Boolean = true,
    showChoosePalette: Boolean = true
) {

    val hsv = remember {
        val hsvArray = floatArrayOf(0f, 0f, 0f)
        AndroidColor.colorToHSV(initialColor.toArgb(), hsvArray)
        mutableStateOf(Triple(hsvArray[0], hsvArray[1], hsvArray[2]))
    }

    val alphaValue = remember { mutableStateOf(initialColor.alpha) }

    val selectedColor = remember(hsv.value, alphaValue.value) {
        mutableStateOf(
            Color.hsv(
                hsv.value.first,
                hsv.value.second,
                hsv.value.third,
                alphaValue.value
            )
        )
    }

    val redValue = remember { mutableStateOf((selectedColor.value.red * 255).toInt().toString()) }
    val greenValue =
        remember { mutableStateOf((selectedColor.value.green * 255).toInt().toString()) }
    val blueValue = remember { mutableStateOf((selectedColor.value.blue * 255).toInt().toString()) }
    val alphaInputValue =
        remember { mutableStateOf((selectedColor.value.alpha * 255).toInt().toString()) }

    val hexValue = remember {
        mutableStateOf(String.format("%08X", selectedColor.value.toArgb()))
    }

    fun updateInputFields() {
        redValue.value = (selectedColor.value.red * 255).toInt().toString()
        greenValue.value = (selectedColor.value.green * 255).toInt().toString()
        blueValue.value = (selectedColor.value.blue * 255).toInt().toString()
        alphaInputValue.value = (selectedColor.value.alpha * 255).toInt().toString()
        hexValue.value = String.format("%08X", selectedColor.value.toArgb())
    }

    fun updateColorFromRGB() {
        try {
            val r = redValue.value.toIntOrNull()?.coerceIn(0, 255) ?: return
            val g = greenValue.value.toIntOrNull()?.coerceIn(0, 255) ?: return
            val b = blueValue.value.toIntOrNull()?.coerceIn(0, 255) ?: return
            val a = alphaInputValue.value.toIntOrNull()?.coerceIn(0, 255) ?: return

            val color = Color(r / 255f, g / 255f, b / 255f, a / 255f)
            val hsvArray = floatArrayOf(0f, 0f, 0f)
            AndroidColor.colorToHSV(color.toArgb(), hsvArray)
            hsv.value = Triple(hsvArray[0], hsvArray[1], hsvArray[2])
            alphaValue.value = a / 255f
        } catch (_: Exception) {
        }
    }

    fun updateColorFromHex() {
        try {
            val cleanHex = hexValue.value.removePrefix("#")
            when (cleanHex.length) {
                8 -> {
                    val colorLong = cleanHex.toLong(16)
                    val color = Color(colorLong.toInt())
                    val hsvArray = floatArrayOf(0f, 0f, 0f)
                    AndroidColor.colorToHSV(color.toArgb(), hsvArray)
                    hsv.value = Triple(hsvArray[0], hsvArray[1], hsvArray[2])
                    alphaValue.value = color.alpha
                }

                6 -> {
                    val colorInt = cleanHex.toLong(16).toInt()
                    val color = Color(colorInt or 0xFF000000.toInt())
                    val hsvArray = floatArrayOf(0f, 0f, 0f)
                    AndroidColor.colorToHSV(color.toArgb(), hsvArray)
                    hsv.value = Triple(hsvArray[0], hsvArray[1], hsvArray[2])
                    alphaValue.value = 1f
                }
            }
        } catch (_: Exception) {
        }
    }

    CustomDialog(
        onDismiss = onDismiss,
        confirmButtonText = "Select Color",
        onConfirm = {
            onColorSelected(selectedColor.value)
            onDismiss()
        },
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.pixelDp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SatValPanel(
                    hue = hsv.value.first,
                    saturation = hsv.value.second,
                    value = hsv.value.third
                ) { sat, value ->
                    hsv.value = Triple(hsv.value.first, sat, value)
                    updateInputFields()
                }

                Row {
                    HueBar(
                        hue = hsv.value.first,
                        modifier = Modifier
                            .height(26.pixelDp)
                            .weight(0.7f)
                    ) { hue ->
                        hsv.value = Triple(hue, hsv.value.second, hsv.value.third)
                        updateInputFields()
                    }

                    ColorItem(
                        color = selectedColor.value,
                        modifier = Modifier
                            .padding(start = 6.pixelDp)
                            .size(26.pixelDp)
                            .border(width = 4.pixelDp, color = AppTheme.colors.BackgroundColorDarker)
                    )
                }

                // Alpha bar
                AlphaBar(
                    alpha = alphaValue.value,
                    hue = hsv.value.first,
                    saturation = hsv.value.second,
                    value = hsv.value.third,
                    modifier = Modifier
                        .height(22.pixelDp)
                        .fillMaxWidth()
                ) { newAlpha ->
                    alphaValue.value = newAlpha
                    updateInputFields()
                }

                // Value/Brightness bar
                ValueBar(
                    hue = hsv.value.first,
                    saturation = hsv.value.second,
                    value = hsv.value.third,
                    modifier = Modifier
                        .height(22.pixelDp)
                        .fillMaxWidth()
                ) { newValue ->
                    hsv.value = Triple(hsv.value.first, hsv.value.second, newValue)
                    updateInputFields()
                }

                if (showPalette) {
                    ColorPaletteView(
                        colors = colorPalette,
                        onColorSelected = { color ->
                            val hsvArray = floatArrayOf(0f, 0f, 0f)
                            AndroidColor.colorToHSV(color.toArgb(), hsvArray)
                            hsv.value = Triple(hsvArray[0], hsvArray[1], hsvArray[2])
                            alphaValue.value = color.alpha
                            updateInputFields()
                        },
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.pixelDp)) {
                    ColorInputTextField(
                        value = hexValue.value,
                        onValueChange = { newText ->
                            val filtered = newText.uppercase().filter { it in "0123456789ABCDEF" }
                            hexValue.value = filtered.take(8)
                            updateColorFromHex()
                        },
                        label = "Hex",
                        labelColor = AppTheme.colors.SelectedColor,
                        placeholder = "FF000000",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.pixelDp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ColorInputTextField(
                            value = redValue.value,
                            onValueChange = { newText ->
                                val filtered = newText.filter { it in "0123456789" }
                                redValue.value = if (filtered.isEmpty()) "" else minOf(
                                    filtered.take(3).toInt(),
                                    255
                                ).toString()
                                updateColorFromRGB()
                            },
                            label = "R",
                            labelColor = AppTheme.colors.DismissButtonColor,
                            placeholder = "0",
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Number
                        )

                        ColorInputTextField(
                            value = greenValue.value,
                            onValueChange = { newText ->
                                val filtered = newText.filter { it in "0123456789" }
                                greenValue.value = if (filtered.isEmpty()) "" else minOf(
                                    filtered.take(3).toInt(),
                                    255
                                ).toString()
                                updateColorFromRGB()
                            },
                            label = "G",
                            labelColor = AppTheme.colors.AccentButtonColor,
                            placeholder = "0",
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Number
                        )

                        ColorInputTextField(
                            value = blueValue.value,
                            onValueChange = { newText ->
                                val filtered = newText.filter { it in "0123456789" }
                                blueValue.value = if (filtered.isEmpty()) "" else minOf(
                                    filtered.take(3).toInt(),
                                    255
                                ).toString()
                                updateColorFromRGB()
                            },
                            label = "B",
                            labelColor = AppTheme.colors.LinkColor,
                            placeholder = "0",
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Number
                        )

                        ColorInputTextField(
                            value = alphaInputValue.value,
                            onValueChange = { newText ->
                                val filtered = newText.filter { it in "0123456789" }
                                alphaInputValue.value = if (filtered.isEmpty()) "" else minOf(
                                    filtered.take(3).toInt(),
                                    255
                                ).toString()
                                updateColorFromRGB()
                            },
                            label = "A",
                            labelColor = AppTheme.colors.TextColorLight,
                            placeholder = "255",
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                if (showChoosePalette) {
                    Button(
                        onClick = onOpenPaletteScreen,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.AccentButtonColor
                        ),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.choose_another_palette),
                            color = AppTheme.colors.TextColorDark
                        )
                    }
                }
            }
        }
    )
}


@Composable
private fun ColorInputTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    labelColor: Color = Color.Unspecified,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = labelColor) },
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = {
            Text(
                placeholder,
                color = AppTheme.colors.Subtext0Color,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        singleLine = true,
        colors = AppTheme.colors.outlineTextFieldColors(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
    )
}

@Composable
private fun HueBar(
    hue: Float,
    modifier: Modifier = Modifier,
    setColor: (Float) -> Unit
) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val pressOffset = remember {
        mutableStateOf(Offset.Zero)
    }

    Canvas(
        modifier = modifier
            .border(
                width = 4.pixelDp,
                color = AppTheme.colors.BackgroundColorDarker
            )
            .clip(RectangleShape)
            .emitDragGesture(interactionSource)
    ) {
        val drawScopeSize = size
        val bitmap = createBitmap(size.width.toInt(), size.height.toInt())
        val hueCanvas = Canvas(bitmap)

        val huePanel = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())

        val hueColors = IntArray((huePanel.width()).toInt())
        var hueStep = 0f
        for (i in hueColors.indices) {
            hueColors[i] = AndroidColor.HSVToColor(floatArrayOf(hueStep, 1f, 1f))
            hueStep += 360f / hueColors.size
        }

        val linePaint = Paint()
        linePaint.strokeWidth = 0F
        for (i in hueColors.indices) {
            linePaint.color = hueColors[i]
            hueCanvas.drawLine(i.toFloat(), 0F, i.toFloat(), huePanel.bottom, linePaint)
        }

        drawBitmap(
            bitmap = bitmap,
            panel = huePanel
        )

        fun pointToHue(pointX: Float): Float {
            val width = huePanel.width()
            val x = when {
                pointX < huePanel.left -> 0F
                pointX > huePanel.right -> width
                else -> pointX - huePanel.left
            }
            return x * 360f / width
        }

        fun hueToPoint(hue: Float): Float {
            val width = huePanel.width()
            return (hue / 360f) * width
        }

        val indicatorX = hueToPoint(hue)
        pressOffset.value = Offset(indicatorX, size.height / 2)

        scope.collectForPress(interactionSource) { pressPosition ->
            val pressPos = pressPosition.x.coerceIn(0f..drawScopeSize.width)
            pressOffset.value = Offset(pressPos, size.height / 2)
            val selectedHue = pointToHue(pressPos)
            setColor(selectedHue)
        }

        drawCircle(
            Color.White,
            radius = 4.pixelDp.toPx(),
            center = pressOffset.value,
            style = Stroke(
                width = 2.pixelDp.toPx()
            )
        )
    }
}

@Composable
private fun AlphaBar(
    alpha: Float,
    hue: Float,
    saturation: Float,
    value: Float,
    modifier: Modifier = Modifier,
    onAlphaChanged: (Float) -> Unit
) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val pressOffset = remember { mutableStateOf(Offset.Zero) }

    Canvas(
        modifier = modifier
            .border(width = 4.pixelDp, color = AppTheme.colors.BackgroundColorDarker)
            .clip(RectangleShape)
            .emitDragGesture(interactionSource)
    ) {
        val drawScopeSize = size
        val w = size.width.toInt().coerceAtLeast(1)
        val h = size.height.toInt().coerceAtLeast(1)
        val bitmap = createBitmap(w, h)
        val alphaCanvas = Canvas(bitmap)
        val panel = RectF(0f, 0f, w.toFloat(), h.toFloat())

        // Draw checkerboard background
        val checkerSize = h / 2f
        val checkerPaint = Paint()
        for (row in 0..(h / checkerSize.toInt())) {
            for (col in 0..(w / checkerSize.toInt())) {
                val isLight = (row + col) % 2 == 0
                checkerPaint.color = if (isLight) 0xFFCCCCCC.toInt() else 0xFF999999.toInt()
                alphaCanvas.drawRect(
                    col * checkerSize, row * checkerSize,
                    (col + 1) * checkerSize, (row + 1) * checkerSize,
                    checkerPaint
                )
            }
        }

        // Draw alpha gradient overlay
        val opaqueColor = AndroidColor.HSVToColor(255, floatArrayOf(hue, saturation, value))
        val transparentColor = AndroidColor.HSVToColor(0, floatArrayOf(hue, saturation, value))
        val gradient = LinearGradient(
            0f, 0f, w.toFloat(), 0f,
            transparentColor, opaqueColor,
            Shader.TileMode.CLAMP
        )
        val gradientPaint = Paint().apply { shader = gradient }
        alphaCanvas.drawRect(panel, gradientPaint)

        drawBitmap(bitmap = bitmap, panel = panel)

        fun pointToAlpha(pointX: Float): Float {
            val x = pointX.coerceIn(0f, drawScopeSize.width)
            return x / drawScopeSize.width
        }

        val indicatorX = alpha * drawScopeSize.width
        pressOffset.value = Offset(indicatorX, size.height / 2)

        scope.collectForPress(interactionSource) { pressPosition ->
            val pressPos = pressPosition.x.coerceIn(0f..drawScopeSize.width)
            pressOffset.value = Offset(pressPos, size.height / 2)
            onAlphaChanged(pointToAlpha(pressPos))
        }

        drawCircle(
            Color.White,
            radius = 4.pixelDp.toPx(),
            center = pressOffset.value,
            style = Stroke(width = 2.pixelDp.toPx())
        )
    }
}

@Composable
private fun ValueBar(
    hue: Float,
    saturation: Float,
    value: Float,
    modifier: Modifier = Modifier,
    onValueChanged: (Float) -> Unit
) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val pressOffset = remember { mutableStateOf(Offset.Zero) }

    Canvas(
        modifier = modifier
            .border(width = 4.pixelDp, color = AppTheme.colors.BackgroundColorDarker)
            .clip(RectangleShape)
            .emitDragGesture(interactionSource)
    ) {
        val drawScopeSize = size
        val w = size.width.toInt().coerceAtLeast(1)
        val h = size.height.toInt().coerceAtLeast(1)
        val bitmap = createBitmap(w, h)
        val valueCanvas = Canvas(bitmap)
        val panel = RectF(0f, 0f, w.toFloat(), h.toFloat())

        val darkColor = AndroidColor.HSVToColor(floatArrayOf(hue, saturation, 0f))
        val brightColor = AndroidColor.HSVToColor(floatArrayOf(hue, saturation, 1f))
        val gradient = LinearGradient(
            0f, 0f, w.toFloat(), 0f,
            darkColor, brightColor,
            Shader.TileMode.CLAMP
        )
        val gradientPaint = Paint().apply { shader = gradient }
        valueCanvas.drawRect(panel, gradientPaint)

        drawBitmap(bitmap = bitmap, panel = panel)

        fun pointToValue(pointX: Float): Float {
            val x = pointX.coerceIn(0f, drawScopeSize.width)
            return x / drawScopeSize.width
        }

        val indicatorX = value * drawScopeSize.width
        pressOffset.value = Offset(indicatorX, size.height / 2)

        scope.collectForPress(interactionSource) { pressPosition ->
            val pressPos = pressPosition.x.coerceIn(0f..drawScopeSize.width)
            pressOffset.value = Offset(pressPos, size.height / 2)
            onValueChanged(pointToValue(pressPos))
        }

        drawCircle(
            Color.White,
            radius = 4.pixelDp.toPx(),
            center = pressOffset.value,
            style = Stroke(width = 2.pixelDp.toPx())
        )
    }
}

@Composable
private fun SatValPanel(
    hue: Float,
    saturation: Float,
    value: Float,
    setSatVal: (Float, Float) -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val scope = rememberCoroutineScope()
    var sat: Float
    var brightness: Float

    val pressOffset = remember {
        mutableStateOf(Offset.Zero)
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(134.pixelDp)
            .border(
                width = 4.pixelDp,
                color = AppTheme.colors.BackgroundColorDarker,
                shape = RectangleShape
            )
            .emitDragGesture(interactionSource)
            .clip(RectangleShape)
    ) {
        val cornerRadius = 8.pixelDp.toPx()
        val satValSize = size

        val bitmap = createBitmap(size.width.toInt(), size.height.toInt())
        val canvas = Canvas(bitmap)
        val satValPanel = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())

        val rgb = AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))

        val satShader = LinearGradient(
            satValPanel.left, satValPanel.top, satValPanel.right, satValPanel.top,
            -0x1, rgb, Shader.TileMode.CLAMP
        )
        val valShader = LinearGradient(
            satValPanel.left, satValPanel.top, satValPanel.left, satValPanel.bottom,
            -0x1, -0x1000000, Shader.TileMode.CLAMP
        )

        canvas.drawRoundRect(
            satValPanel,
            cornerRadius,
            cornerRadius,
            Paint().apply {
                shader = ComposeShader(
                    valShader,
                    satShader,
                    PorterDuff.Mode.MULTIPLY
                )
            }
        )

        drawBitmap(
            bitmap = bitmap,
            panel = satValPanel
        )

        fun pointToSatVal(pointX: Float, pointY: Float): Pair<Float, Float> {
            val width = satValPanel.width()
            val height = satValPanel.height()

            val x = when {
                pointX < satValPanel.left -> 0f
                pointX > satValPanel.right -> width
                else -> pointX - satValPanel.left
            }

            val y = when {
                pointY < satValPanel.top -> 0f
                pointY > satValPanel.bottom -> height
                else -> pointY - satValPanel.top
            }

            val satPoint = 1f / width * x
            val valuePoint = 1f - 1f / height * y

            return satPoint to valuePoint
        }

        fun satValToPoint(saturation: Float, brightness: Float): Offset {
            val width = satValPanel.width()
            val height = satValPanel.height()

            val x = saturation * width
            val y = (1f - brightness) * height

            return Offset(x, y)
        }

        val indicatorPosition = satValToPoint(saturation, value)
        pressOffset.value = indicatorPosition

        scope.collectForPress(interactionSource) { pressPosition ->
            val pressPositionOffset = Offset(
                pressPosition.x.coerceIn(0f..satValSize.width),
                pressPosition.y.coerceIn(0f..satValSize.height)
            )

            pressOffset.value = pressPositionOffset
            val (satPoint, valuePoint) = pointToSatVal(pressPositionOffset.x, pressPositionOffset.y)
            sat = satPoint
            brightness = valuePoint

            setSatVal(sat, brightness)
        }

        val indicatorColor = if (
            Color.hsv(hue, saturation, value).luminance() > 0.5f
        ) {
            Color.Black
        } else {
            Color.White
        }

        drawCircle(
            color = indicatorColor,
            radius = 6.pixelDp.toPx(),
            center = pressOffset.value,
            style = Stroke(
                width = 2.pixelDp.toPx()
            )
        )

        drawCircle(
            color = indicatorColor,
            radius = 2.pixelDp.toPx(),
            center = pressOffset.value,
        )
    }
}


private fun CoroutineScope.collectForPress(
    interactionSource: InteractionSource,
    setOffset: (Offset) -> Unit
) {
    launch {
        Log.d("Collect Press", "Recomposed")
        interactionSource.interactions.collect { interaction ->
            (interaction as? PressInteraction.Press)
                ?.pressPosition
                ?.let(setOffset)
        }
    }
}

private fun Modifier.emitDragGesture(
    interactionSource: MutableInteractionSource
): Modifier = composed {
    val scope = rememberCoroutineScope()

    pointerInput(Unit) {
        detectDragGestures { input, _ ->
            scope.launch {
                interactionSource.emit(PressInteraction.Press(input.position))
            }
        }
    }.clickable(interactionSource, null) {

    }
}

private fun DrawScope.drawBitmap(
    bitmap: Bitmap,
    panel: RectF
) {
    drawIntoCanvas {
        it.nativeCanvas.drawBitmap(
            bitmap,
            null,
            panel.toRect(),
            null
        )
    }
}