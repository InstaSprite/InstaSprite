package com.instasprite.app.ui.palette

import com.instasprite.app.utils.pixelDp

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instasprite.app.R
import com.instasprite.app.domain.model.ColorPalette
import com.instasprite.app.ui.components.composable.TopBar
import com.instasprite.app.ui.components.composable.ColorPaletteList
import com.instasprite.app.ui.components.composable.ExpandableFabMenu
import com.instasprite.app.ui.components.composable.FabMenuItem
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.DummyData
import com.instasprite.app.utils.noRippleClickable
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
    val favoritePalettes by viewModel.favoritePalettes.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val defaultPaletteId by viewModel.defaultPaletteId.collectAsState()

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

    val searchQuery by viewModel.searchQuery.collectAsState()
    val showSearchBar by viewModel.showSearchBar.collectAsState()

    ColorPaletteSelectionContent(
        savedPalettes = savedPalettes,
        favoritePalettes = favoritePalettes,
        defaultPaletteId = defaultPaletteId,
        searchQuery = searchQuery,
        showSearchBar = showSearchBar,
        onToggleSearchBar = { viewModel.toggleSearchBar() },
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onPaletteSelected = onPaletteSelected,
        onPaletteDeleteButton = { paletteToDelete ->
            viewModel.openDialog(ColorPaletteDialog.DeletePalette(paletteToDelete))
        },
        onPaletteEdit = onPaletteEdit,
        onToggleFavorite = { viewModel.toggleFavorite(it) },
        onSetDefault = { viewModel.setDefaultPalette(it.id) },
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
    favoritePalettes: List<ColorPalette>,
    defaultPaletteId: Int = -1,
    searchQuery: String = "",
    showSearchBar: Boolean = false,
    onToggleSearchBar: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onPaletteSelected: (ColorPalette) -> Unit = {},
    onPaletteDeleteButton: (ColorPalette) -> Unit = {},
    onPaletteEdit: (ColorPalette) -> Unit = {},
    onToggleFavorite: (ColorPalette) -> Unit = {},
    onSetDefault: (ColorPalette) -> Unit = {},
    onImportLospec: () -> Unit = {},
    onImportGpl: () -> Unit = {},
    onCreateNewPalette: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {

    // 0 = All, 1 = Favorites
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val displayedPalettes = if (selectedTab == 0) savedPalettes else favoritePalettes

    val lazyListState = rememberLazyListState()

    if (showSearchBar) {
        BackHandler {
            onToggleSearchBar()
        }
    }

    Box {
        Scaffold(
            topBar = {
                Box {
                    TopBar(
                        title = stringResource(R.string.palette),
                        onBackClick = onDismiss,
                        actions = {
                            IconButton(onClick = onToggleSearchBar) {
                                PixelIcon(
                                    icon = R.drawable.ic_search,
                                    contentDescription = "Search",
                                    tint = AppTheme.colors.WarningColor
                                )
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = showSearchBar,
                        enter = slideInVertically(initialOffsetY = { -it }),
                        exit = slideOutVertically(targetOffsetY = { -it }),
                        modifier = Modifier
                            .zIndex(5f)
                            .fillMaxWidth()
                            .height(38.pixelDp)
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.search_palette),
                                    color = AppTheme.colors.Subtext0Color
                                )
                            },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (searchQuery.isEmpty()) {
                                            onToggleSearchBar()
                                        }
                                        onSearchQueryChange("")
                                    },
                                ) {
                                    PixelIcon(
                                        icon = R.drawable.ic_close,
                                        contentDescription = "Clear",
                                        tint = AppTheme.colors.DismissButtonColor,
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppTheme.colors.BackgroundColor),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = AppTheme.colors.BackgroundColor,
                                disabledContainerColor = AppTheme.colors.BackgroundColor,
                                unfocusedContainerColor = AppTheme.colors.BackgroundColor,
                                focusedTextColor = AppTheme.colors.TextColorLight,
                                unfocusedTextColor = AppTheme.colors.TextColorLight,
                                cursorColor = AppTheme.colors.TextColorLight,
                                focusedBorderColor = AppTheme.colors.WarningColor,
                                unfocusedBorderColor = AppTheme.colors.WarningColor,
                                unfocusedPlaceholderColor = AppTheme.colors.WarningColor,
                                focusedPlaceholderColor = AppTheme.colors.Subtext0Color
                            ),
                        )
                    }
                }
            },
            bottomBar = {
                PaletteBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onImportLospec = onImportLospec,
                    onImportGpl = onImportGpl,
                    onCreateNewPalette = onCreateNewPalette,
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
                    palettes = displayedPalettes,
                    lazyListState = lazyListState,
                    onPaletteSelected = {
                        onPaletteSelected(it)
                    },
                    optionSlot = { palette ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.pixelDp),
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 4.pixelDp)
                        ) {
                            Text(
                                text = if (palette.id == defaultPaletteId) "Default" else "Set Default",
                                color = if (palette.id == defaultPaletteId)
                                    AppTheme.colors.AccentButtonColor
                                else
                                    AppTheme.colors.TextColorLight.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(
                                        if (palette.id == defaultPaletteId)
                                            AppTheme.colors.AccentButtonColor.copy(alpha = 0.15f)
                                        else
                                            AppTheme.colors.TextColorLight.copy(alpha = 0.05f),
                                        shape = MaterialTheme.shapes.extraSmall
                                    )
                                    .noRippleClickable { onSetDefault(palette) }
                                    .padding(horizontal = 6.pixelDp, vertical = 3.pixelDp)
                            )

                            if (palette.id > 0) {
                                IconButton(
                                    onClick = { onToggleFavorite(palette) },
                                    modifier = Modifier.size(16.pixelDp)
                                ) {
                                    PixelIcon(
                                        icon = R.drawable.ic_heart,
                                        contentDescription = "Toggle favorite",
                                        tint = if (palette.isFavorite)
                                            AppTheme.colors.DismissButtonColor
                                        else
                                            AppTheme.colors.TextColorLight.copy(alpha = 0.4f),
                                    )
                                }

                                IconButton(
                                    onClick = { onPaletteEdit(palette) },
                                    modifier = Modifier.size(16.pixelDp)
                                ) {
                                    PixelIcon(
                                        icon = R.drawable.ic_edit,
                                        contentDescription = "Edit Palette",
                                        tint = AppTheme.colors.AccentButtonColor,
                                    )
                                }
                                IconButton(
                                    onClick = { onPaletteDeleteButton(palette) },
                                    modifier = Modifier.size(16.pixelDp)
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
        }
    }
}

@Composable
private fun PaletteBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onImportLospec: () -> Unit,
    onImportGpl: () -> Unit,
    onCreateNewPalette: () -> Unit,
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.pixelDp)
            .background(AppTheme.colors.BackgroundColor),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // All tab
        PaletteTab(
            label = stringResource(R.string.all_palettes),
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )

        // FAB in center
        ExpandableFabMenu(
            items = menuItems
        )

        // Favorites tab
        PaletteTab(
            label = stringResource(R.string.favorites),
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PaletteTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor by animateColorAsState(
        targetValue = if (isSelected)
            AppTheme.colors.AccentButtonColor
        else
            AppTheme.colors.TextColorLight.copy(alpha = 0.5f),
        animationSpec = tween(200)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .noRippleClickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    InstaSpriteTheme() {
        val data = DummyData.palettes.toMutableList()

        ColorPaletteSelectionContent(
            savedPalettes = data,
            favoritePalettes = data.filter { it.isFavorite },
        )
    }
}
