package com.olaz.instasprite.ui.drawing.dialog


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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toRect
import com.olaz.instasprite.ui.components.composable.ColorItem
import com.olaz.instasprite.ui.components.composable.ColorPaletteView
import com.olaz.instasprite.ui.components.composable.ColorPaletteConfig
import com.olaz.instasprite.ui.components.dialog.CustomDialog
import com.olaz.instasprite.ui.theme.CatppuccinTypography
import com.olaz.instasprite.ui.theme.CatppuccinUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor


@Composable
fun ColorWheelDialog(
    initialColor: Color = Color.Blue,
    colorPalette: List<Color>,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit,
    onOpenImportColorPaletteDialog: () -> Unit,
) {

    val hsv = remember {
        val hsvArray = floatArrayOf(0f, 0f, 0f)
        AndroidColor.colorToHSV(initialColor.toArgb(), hsvArray)
        mutableStateOf(Triple(hsvArray[0], hsvArray[1], hsvArray[2]))
    }

    val selectedColor = remember(hsv.value) {
        mutableStateOf(
            Color.hsv(
                hsv.value.first,
                hsv.value.second,
                hsv.value.third
            )
        )
    }

    val redValue = remember { mutableStateOf((selectedColor.value.red * 255).toInt().toString()) }
    val greenValue =
        remember { mutableStateOf((selectedColor.value.green * 255).toInt().toString()) }
    val blueValue = remember { mutableStateOf((selectedColor.value.blue * 255).toInt().toString()) }

    val hexValue = remember {
        mutableStateOf(String.format("%06X", (selectedColor.value.toArgb() and 0xFFFFFF)))
    }

    fun updateInputFields() {
        redValue.value = (selectedColor.value.red * 255).toInt().toString()
        greenValue.value = (selectedColor.value.green * 255).toInt().toString()
        blueValue.value = (selectedColor.value.blue * 255).toInt().toString()
        hexValue.value = String.format("%06X", (selectedColor.value.toArgb() and 0xFFFFFF))
    }

    fun updateColorFromRGB() {
        try {
            val r = redValue.value.toIntOrNull()?.coerceIn(0, 255) ?: return
            val g = greenValue.value.toIntOrNull()?.coerceIn(0, 255) ?: return
            val b = blueValue.value.toIntOrNull()?.coerceIn(0, 255) ?: return

            val color = Color(r / 255f, g / 255f, b / 255f)
            val hsvArray = floatArrayOf(0f, 0f, 0f)
            AndroidColor.colorToHSV(color.toArgb(), hsvArray)
            hsv.value = Triple(hsvArray[0], hsvArray[1], hsvArray[2])
        } catch (_: Exception) {
        }
    }

    fun updateColorFromHex() {
        try {
            val cleanHex = hexValue.value.removePrefix("#")
            if (cleanHex.length == 6) {
                val colorInt = cleanHex.toLong(16).toInt()
                val color = Color(colorInt or 0xFF000000.toInt())
                val hsvArray = floatArrayOf(0f, 0f, 0f)
                AndroidColor.colorToHSV(color.toArgb(), hsvArray)
                hsv.value = Triple(hsvArray[0], hsvArray[1], hsvArray[2])
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
                            .height(40.dp)
                            .weight(0.7f)
                    ) { hue ->
                        hsv.value = Triple(hue, hsv.value.second, hsv.value.third)
                        updateInputFields()
                    }

                    ColorItem(
                        color = selectedColor.value,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .border(width = 5.dp, color = CatppuccinUI.BackgroundColorDarker)
                    )
                }

                ColorPaletteView(
                    colors = colorPalette,
                    onColorSelected = { color ->
                        val hsvArray = floatArrayOf(0f, 0f, 0f)
                        AndroidColor.colorToHSV(color.toArgb(), hsvArray)
                        hsv.value = Triple(hsvArray[0], hsvArray[1], hsvArray[2])
                        updateInputFields()
                    },
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorInputTextField(
                        value = hexValue.value,
                        onValueChange = { newText ->
                            val filtered = newText.uppercase().filter { it in "0123456789ABCDEF" }
                            hexValue.value = filtered.take(6)
                            updateColorFromHex()
                        },
                        label = "Hex",
                        labelColor = CatppuccinUI.SelectedColor,
                        placeholder = "FFFFFF",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                            labelColor = CatppuccinUI.CurrentPalette.Red,
                            placeholder = "0",
                            modifier = Modifier.weight(0.2f),
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
                            labelColor = CatppuccinUI.CurrentPalette.Green,
                            placeholder = "0",
                            modifier = Modifier.weight(0.2f),
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
                            labelColor = CatppuccinUI.CurrentPalette.Blue,
                            placeholder = "0",
                            modifier = Modifier.weight(0.2f),
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                Button(
                    onClick = onOpenImportColorPaletteDialog,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CatppuccinUI.AccentButtonColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Import Color Palettes", color = CatppuccinUI.TextColorDark)
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
        textStyle = CatppuccinTypography.bodyLarge,
        placeholder = {
            Text(
                placeholder,
                color = CatppuccinUI.Subtext0Color,
                style = CatppuccinTypography.bodyLarge
            )
        },
        singleLine = true,
        colors = CatppuccinUI.OutlineTextFieldColors.colors(),
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
                width = 5.dp,
                color = CatppuccinUI.BackgroundColorDarker
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
            radius = 5.dp.toPx(),
            center = pressOffset.value,
            style = Stroke(
                width = 2.dp.toPx()
            )
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
            .height(200.dp)
            .border(
                width = 5.dp,
                color = CatppuccinUI.BackgroundColorDarker,
                shape = RectangleShape
            )
            .emitDragGesture(interactionSource)
            .clip(RectangleShape)
    ) {
        val cornerRadius = 12.dp.toPx()
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

        drawCircle(
            color = Color.White,
            radius = 8.dp.toPx(),
            center = pressOffset.value,
            style = Stroke(
                width = 2.dp.toPx()
            )
        )

        drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
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
