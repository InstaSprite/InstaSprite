package com.instasprite.app.ui.drawing.dialog

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import com.instasprite.app.R
import com.instasprite.app.domain.export.ImageExporter
import com.instasprite.app.domain.model.Sprite
import com.instasprite.app.ui.components.composable.CanvasPreviewer
import com.instasprite.app.ui.components.composable.ImageZoomableOverlay
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.components.dialog.CustomDialog
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.utils.getFileName
import com.instasprite.app.utils.pixelDp

@Composable
fun LoadISpriteDialog(
    onDismiss: () -> Unit,
    onFilePicked: suspend (Uri) -> Sprite?,
    onLoad: (Sprite) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var spriteData by remember { mutableStateOf<Sprite?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(), onResult = { uri: Uri? ->
            uri?.let {
                val fileName = getFileName(context, it)
                if (fileName?.endsWith(".isprite") == true) {
                    fileUri = it
                    isLoading = true
                    scope.launch(Dispatchers.IO) {
                        val result = onFilePicked(it)
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            spriteData = result
                            if (result == null) {
                                Toast.makeText(context, "Error loading file", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Invalid file type", Toast.LENGTH_SHORT).show()
                }
            }
        })

    val displayPath = fileUri?.let { getFileName(context, it) } ?: "Tap to select file"
    val spriteWidth = spriteData?.width ?: ""
    val spriteHeight = spriteData?.height ?: ""

    CustomDialog(
        title = "Import ISprite", onDismiss = onDismiss, onConfirm = {
            spriteData?.let {
                onLoad(it)
                onDismiss()
            } ?: Toast.makeText(
                context, "No file loaded", Toast.LENGTH_SHORT
            ).show()
        },
        confirmButtonText = stringResource(R.string.load),
        dismissButtonText = stringResource(R.string.cancel),
        content = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { filePickerLauncher.launch(arrayOf("*/*")) }) {
                    OutlinedTextField(
                        value = displayPath,
                        onValueChange = {},
                        label = {
                            Text(
                                stringResource(R.string.file),
                                color = AppTheme.colors.SelectedColor
                            )
                        },
                        readOnly = true,
                        enabled = false,
                        trailingIcon = {
                            PixelIcon(
                                icon = R.drawable.ic_folder,
                                contentDescription = stringResource(R.string.choose_file),
                                tint = AppTheme.colors.LinkColor
                            )
                        },
                        colors = AppTheme.colors.outlineTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(8.pixelDp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Width: $spriteWidth")
                    Text("Height: $spriteHeight")
                }

                spriteData?.let {
                    Spacer(Modifier.height(8.pixelDp))

                    var showOverlay by remember { mutableStateOf(false) }

                    CanvasPreviewer(
                        sprite = spriteData!!,
                        showBorder = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        onClick = {
                            showOverlay = true
                        }
                    )

                    if (showOverlay) {
                        val bitmapImage = remember(it) {
                            ImageExporter.convertToBitmap(
                                it.compositedPixels,
                                it.width,
                                it.height,
                            )?.asImageBitmap()
                        }

                        ImageZoomableOverlay(
                            bitmap = bitmapImage!!,
                            onDismiss = { showOverlay = false }
                        )
                    }
                }
                
                if (isLoading) {
                    Spacer(Modifier.height(16.pixelDp))
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = AppTheme.colors.SelectedColor
                    )
                }
            }
        })
}

