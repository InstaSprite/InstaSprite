package com.olaz.instasprite.ui.palette

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.olaz.instasprite.domain.model.ColorPalette
import com.olaz.instasprite.ui.components.composable.BackButton
import com.olaz.instasprite.ui.components.composable.ColorPaletteList
import com.olaz.instasprite.ui.components.composable.Bar
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import com.olaz.instasprite.utils.DummyData
import com.olaz.instasprite.utils.rememberBottomBarVisibleState

@Composable
fun ColorPaletteScreen(
    onDismiss: () -> Unit,
    onPaletteSelected: (ColorPalette) -> Unit,
    viewModel: ColorPaletteViewModel = hiltViewModel()
) {
    BackHandler(onBack = onDismiss)

    val savedPalettes by viewModel.savedPalettes.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    ColorPaletteScreenDialogs(dialogState, viewModel)

    ColorPaletteSelectionContent(
        savedPalettes = savedPalettes,
        onPaletteSelected = onPaletteSelected,
        onPaletteDeleteButton = { paletteToDelete ->
            viewModel.openDialog(ColorPaletteDialog.DeletePalette(paletteToDelete))
        },
        onFabButton = {
            viewModel.openDialog(ColorPaletteDialog.ImportColorPalettes)
        },
        onDismiss = onDismiss,
    )
}

@Composable
private fun ColorPaletteSelectionContent(
    savedPalettes: List<ColorPalette>,
    onPaletteSelected: (ColorPalette) -> Unit = {},
    onPaletteDeleteButton: (ColorPalette) -> Unit = {},
    onFabButton: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {

    val lazyListState = rememberLazyListState()

    val isFabVisible by rememberBottomBarVisibleState(lazyListState)

    Scaffold(
        topBar = {
            Bar(
                leftSlot = {
                    BackButton(onClick = onDismiss)
                }
            )
        },
        containerColor = CatppuccinUI.BackgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ColorPaletteList(
                palettes = savedPalettes,
                lazyListState = lazyListState,
                onPaletteSelected = {
                    onPaletteSelected(it)
                },
                optionSlot = { palette ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 8.dp)
                    ) {
                        IconButton(
                            onClick = { onPaletteDeleteButton(palette) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete palette",
                                tint = CatppuccinUI.DismissButtonColor
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            AnimatedVisibility(
                visible = isFabVisible,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .size(70.dp)
                    .align(Alignment.BottomEnd)
            ) {
                FloatingActionButton(
                    onClick = onFabButton,
                    shape = CircleShape,
                    containerColor = CatppuccinUI.SelectedColor,
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Floating action button",
                        tint = CatppuccinUI.TextColorDark,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

        }
    }

}

@Preview
@Composable
private fun Preview() {
    InstaSpriteTheme() {
        val data = DummyData.palettes.toMutableList()
        data += DummyData.palettes
        data += DummyData.palettes
        data += DummyData.palettes

        ColorPaletteSelectionContent(
            savedPalettes = data,
        )
    }
}
