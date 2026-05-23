package com.instasprite.app.ui.palette

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instasprite.app.R
import com.instasprite.app.domain.model.ColorPalette
import com.instasprite.app.ui.components.composable.BackButton
import com.instasprite.app.ui.components.composable.Bar
import com.instasprite.app.ui.components.composable.ColorPaletteList
import com.instasprite.app.ui.components.composable.ExpandableFabMenu
import com.instasprite.app.ui.components.composable.FabMenuItem
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.DummyData
import com.instasprite.app.utils.rememberBottomBarVisibleState

@Composable
fun ColorPaletteScreen(
    onDismiss: () -> Unit,
    onPaletteSelected: (ColorPalette) -> Unit,
    onPaletteEdit: (ColorPalette) -> Unit = {},
    onCreateNewPalette: () -> Unit = {},
    viewModel: ColorPaletteViewModel = hiltViewModel()
) {
    BackHandler(onBack = onDismiss)

    val savedPalettes by viewModel.savedPalettes.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importPaletteFromGplUri(it)
        }
    }

    ColorPaletteScreenDialogs(
        dialogState = dialogState,
        viewModel = viewModel
    )

    ColorPaletteSelectionContent(
        savedPalettes = savedPalettes,
        onPaletteSelected = onPaletteSelected,
        onPaletteDeleteButton = { paletteToDelete ->
            viewModel.openDialog(ColorPaletteDialog.DeletePalette(paletteToDelete))
        },
        onPaletteEdit = onPaletteEdit,
        onImportLospec = {
            viewModel.openDialog(ColorPaletteDialog.LospecPaletteImport)
        },
        onImportGpl = {
            filePickerLauncher.launch("*/*")
        },
        onCreateNewPalette = onCreateNewPalette,
        onDismiss = onDismiss,
    )
}

@Composable
private fun ColorPaletteSelectionContent(
    savedPalettes: List<ColorPalette>,
    onPaletteSelected: (ColorPalette) -> Unit = {},
    onPaletteDeleteButton: (ColorPalette) -> Unit = {},
    onPaletteEdit: (ColorPalette) -> Unit = {},
    onImportLospec: () -> Unit = {},
    onImportGpl: () -> Unit = {},
    onCreateNewPalette: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {

    val lazyListState = rememberLazyListState()

    Box {
        Scaffold(
            topBar = {
                Bar(
                    leftSlot = {
                        BackButton(onClick = onDismiss)
                    }
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            },
            containerColor = AppTheme.colors.BackgroundColorDarker
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 8.dp)
                        ) {
                            if (palette.id > 0) {
                                IconButton(
                                    onClick = { onPaletteEdit(palette) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    PixelIcon(
                                        icon = R.drawable.ic_edit,
                                        contentDescription = "Edit Palette",
                                        tint = AppTheme.colors.AccentButtonColor,
                                    )
                                }
                                IconButton(
                                    onClick = { onPaletteDeleteButton(palette) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    PixelIcon(
                                        icon = R.drawable.ic_trash,
                                        contentDescription = stringResource(R.string.delete_palette),
                                        tint = AppTheme.colors.DismissButtonColor,

                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 21.dp)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                val createLabel = stringResource(R.string.create_palette)
                val importLospecLabel = stringResource(R.string.import_from_lospec)
                val importGplLabel = stringResource(R.string.import_from_gpl)

                val menuItems = remember(
                    onCreateNewPalette,
                    onImportLospec,
                    onImportGpl,
                    createLabel,
                    importLospecLabel,
                    importGplLabel
                ) {
                    listOf(
                        FabMenuItem(
                            icon = R.drawable.ic_edit,
                            label = createLabel,
                            onClick = onCreateNewPalette
                        ),
                        FabMenuItem(
                            icon = R.drawable.ic_cloud,
                            label = importLospecLabel,
                            onClick = onImportLospec
                        ),
                        FabMenuItem(
                            icon = R.drawable.ic_folder,
                            label = importGplLabel,
                            onClick = onImportGpl
                        )
                    )
                }

                ExpandableFabMenu(
                    items = menuItems
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    InstaSpriteTheme() {
        val data = DummyData.palettes.toMutableList()

        ColorPaletteSelectionContent(
            savedPalettes = data,
        )
    }
}
