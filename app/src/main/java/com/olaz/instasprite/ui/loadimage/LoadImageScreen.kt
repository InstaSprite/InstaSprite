package com.olaz.instasprite.ui.loadimage

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.olaz.instasprite.data.repository.loadDefaultColorPalette
import com.olaz.instasprite.domain.image2pixel.PixelArtConfig
import com.olaz.instasprite.domain.model.ColorPalette
import com.olaz.instasprite.ui.components.composable.BackButton
import com.olaz.instasprite.ui.components.composable.Bar
import com.olaz.instasprite.ui.loadimage.component.ImageConfigView
import com.olaz.instasprite.ui.loadimage.component.ProcessedImagePreview
import com.olaz.instasprite.ui.loadimage.contract.ImageConfigEvent
import com.olaz.instasprite.ui.loadimage.contract.LoadImageUiState
import com.olaz.instasprite.ui.theme.CatppuccinTypography
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import kotlinx.coroutines.launch

data class LoadImageScreenEvent(
    val onConfigEvent: (ImageConfigEvent) -> Unit,
    val onDismiss: () -> Unit,
    val onConfirm: () -> Unit,
    val onLaunchImagePicker: () -> Unit,
    val onPaletteViewClick: () -> Unit
)

@Composable
fun LoadImageScreen(
    onDismiss: () -> Unit,
    onConfirm: (id: String, width: Int, height: Int, name: String) -> Unit,
    onPaletteViewClick: () -> Unit = {},
    selectedPalette: ColorPalette? = null,
    viewModel: LoadImageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.applyPalette, selectedPalette) {
        val paletteList = if (uiState.applyPalette) {
            selectedPalette?.colors ?: loadDefaultColorPalette(context)
        } else {
            null
        }
        viewModel.setPaletteAndApplyConf(
            selectedPalette,
            paletteList?.toList()?.map { it.toArgb() })
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadImage(context, it) }
    }

    val event = remember(viewModel, uiState.processedBitmap, uiState.spriteName) {
        LoadImageScreenEvent(
            onConfigEvent = viewModel::onEvent,
            onDismiss = onDismiss,
            onConfirm = {
                if (uiState.processedBitmap != null && uiState.spriteName.isNotBlank()) {
                    scope.launch {
                        val result = viewModel.saveAndGetSpriteInfo(uiState.spriteName)
                        if (result != null) {
                            onConfirm(result.first, result.second, result.third, uiState.spriteName)
                        } else {
                            Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Please select an image and enter a name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onLaunchImagePicker = { imagePickerLauncher.launch("image/*") },
            onPaletteViewClick = onPaletteViewClick
        )
    }

    LoadImageScreenContent(
        uiState = uiState,
        event = event
    )
}

@Composable
fun LoadImageScreenContent(
    uiState: LoadImageUiState,
    event: LoadImageScreenEvent
) {
    BackHandler(onBack = event.onDismiss)

    Scaffold(
        topBar = {
            Bar(
                leftSlot = {
                    BackButton(onClick = event.onDismiss)
                },
                middleSlot = {
                    Text(
                        text = "Load Image",
                        color = CatppuccinUI.TextColorLight,
                        style = CatppuccinTypography.titleMedium
                    )
                },
                rightSlot = {

                    val isCreateButtonEnabled =
                        uiState.processedBitmap != null && !uiState.isLoading && uiState.spriteName.isNotBlank()

                    Button(
                        onClick = event.onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CatppuccinUI.AccentButtonColor,
                            disabledContainerColor = CatppuccinUI.Foreground0Color
                        ),
                        enabled = isCreateButtonEnabled
                    ) {
                        val textColor = if (isCreateButtonEnabled) {
                            CatppuccinUI.TextColorDark
                        } else {
                            CatppuccinUI.TextColorLight
                        }

                        Text("Create", color = textColor)
                    }
                }
            )
        },
        containerColor = CatppuccinUI.BackgroundColorDarker
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            ProcessedImagePreview(
                processedBitmap = uiState.processedBitmap,
                isLoading = uiState.isLoading,
                onLaunchImagePicker = event.onLaunchImagePicker,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(CatppuccinUI.BackgroundColor)
                    .padding(12.dp),
            ) {
                ImageConfigView(
                    uiState = uiState,
                    event = event,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
private fun LoadImageScreenPreview() {
    InstaSpriteTheme {
        LoadImageScreenContent(
            uiState = LoadImageUiState(
                sourceBitmap = null,
                processedBitmap = null,
                isLoading = false,
                config = PixelArtConfig(),
                spriteName = "Test Image",
                applyPalette = false,
                selectedPalette = null
            ),
            event = LoadImageScreenEvent(
                onConfigEvent = {},
                onDismiss = {},
                onConfirm = {},
                onLaunchImagePicker = {},
                onPaletteViewClick = {}
            )
        )
    }
}

