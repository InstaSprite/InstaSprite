package com.instasprite.app.ui.palette

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.ColorPaletteConfig
import com.instasprite.app.ui.components.composable.ColorPaletteView
import com.instasprite.app.data.model.InputField
import com.instasprite.app.ui.components.composable.InputTextField
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.drawing.dialog.ColorWheelDialog
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.DummyData
import com.instasprite.app.utils.drawCheckerboard
import com.instasprite.app.utils.pixelDp
import com.instasprite.app.utils.toHexString
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private data class ColorItemWithId(
    val id: String,
    val color: Color
)

@Composable
fun PaletteEditorScreen(
    viewModel: PaletteEditorViewModel,
    onDismiss: () -> Unit
) {
    val name by viewModel.paletteName.collectAsState()
    val author by viewModel.paletteAuthor.collectAsState()
    val colors by viewModel.paletteColors.collectAsState()
    val editingIndex by viewModel.editingColorIndex.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onDismiss()
        }
    }

    PaletteEditorContent(
        title = if (viewModel.initialPalette == null) stringResource(R.string.create_palette) else stringResource(R.string.edit_palette),
        name = name,
        author = author,
        colors = colors,
        isSaving = isSaving,
        onNameChange = viewModel::updateName,
        onAuthorChange = viewModel::updateAuthor,
        onSaveClick = viewModel::savePalette,
        onBackClick = onDismiss,
        onAddColorClick = viewModel::openAddColorDialog,
        onColorClick = viewModel::openEditColorDialog,
        onColorDelete = viewModel::deleteColor,
        onColorsReordered = viewModel::updateColors
    )

    editingIndex?.let { index ->
        val initialColor = if (index in colors.indices) colors[index] else Color.White
        ColorWheelDialog(
            initialColor = initialColor,
            colorPalette = colors,
            onDismiss = viewModel::closeColorDialog,
            onColorSelected = viewModel::saveColor,
            onOpenPaletteScreen = {},
            showPalette = false,
            showChoosePalette = false
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaletteEditorContent(
    title: String,
    name: String,
    author: String,
    colors: List<Color>,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    onAddColorClick: () -> Unit,
    onColorClick: (Int) -> Unit,
    onColorDelete: (Int) -> Unit,
    onColorsReordered: (List<Color>) -> Unit
) {
    Scaffold(
        containerColor = AppTheme.colors.BackgroundColorDarker,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.TextColorLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        PixelIcon(
                            icon = R.drawable.ic_left_arrow,
                            contentDescription = stringResource(R.string.back),
                            tint = AppTheme.colors.DismissButtonColor
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSaveClick,
                        enabled = !isSaving
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            color = if (isSaving) AppTheme.colors.Subtext0Color else AppTheme.colors.SelectedColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.BackgroundColor
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.palette_preview),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.Subtext0Color
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.BackgroundColor)
                        .padding(2.dp)
                ) {
                    if (colors.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.no_colors_added_yet),
                                color = AppTheme.colors.Subtext0Color,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        ColorPaletteView(
                            colors = colors,
                            config = ColorPaletteConfig(
                                backgroundColor = AppTheme.colors.BackgroundColorDarker,
                                isInteractive = false,
                                isWrap = true
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val lazyListState = rememberLazyListState()

                var uiColorsList by remember {
                    mutableStateOf(colors.mapIndexed { index, color ->
                        ColorItemWithId(id = java.util.UUID.randomUUID().toString(), color = color)
                    })
                }

                LaunchedEffect(colors) {
                    val currentColors = uiColorsList.map { it.color }
                    if (currentColors != colors) {
                        if (uiColorsList.size == colors.size) {
                            uiColorsList = uiColorsList.mapIndexed { index, item ->
                                if (item.color != colors[index]) {
                                    item.copy(color = colors[index])
                                } else {
                                    item
                                }
                            }
                        } else {
                            val oldItems = uiColorsList.toMutableList()
                            val newItems = colors.map { newColor ->
                                val matchIndex = oldItems.indexOfFirst { it.color == newColor }
                                if (matchIndex != -1) {
                                    oldItems.removeAt(matchIndex)
                                } else {
                                    ColorItemWithId(id = java.util.UUID.randomUUID().toString(), color = newColor)
                                }
                            }
                            uiColorsList = newItems
                        }
                    }
                }

                val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
                    val fromIdx = from.index - 3
                    val toIdx = to.index - 3
                    if (fromIdx in uiColorsList.indices && toIdx in uiColorsList.indices) {
                        uiColorsList = uiColorsList.toMutableList().apply {
                            add(toIdx, removeAt(fromIdx))
                        }
                    }
                }

                LazyColumn(
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        val paletteNameLabel = stringResource(R.string.palette_name)
                        val paletteNameField = remember(paletteNameLabel) {
                            InputField(
                                label = paletteNameLabel,
                                validator = { true }
                            )
                        }
                        InputTextField(
                            value = name,
                            onValueChange = onNameChange,
                            inputField = paletteNameField,
                            imeAction = ImeAction.Next,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        val authorLabel = stringResource(R.string.author)
                        val authorField = remember(authorLabel) {
                            InputField(
                                label = authorLabel,
                                validator = { true }
                            )
                        }
                        InputTextField(
                            value = author,
                            onValueChange = onAuthorChange,
                            inputField = authorField,
                            imeAction = ImeAction.Done,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.colors_count, colors.size),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.TextColorLight
                            )
                            Text(
                                text = stringResource(R.string.tap_color_to_edit),
                                fontSize = 12.sp,
                                color = AppTheme.colors.Subtext0Color
                            )
                        }
                    }

                    items(uiColorsList, key = { it.id }) { item ->
                        ReorderableItem(
                            state = reorderableLazyListState,
                            key = item.id
                        ) { isDragging ->
                            LaunchedEffect(isDragging) {
                                if (!isDragging) {
                                    val newColors = uiColorsList.map { it.color }
                                    if (newColors != colors) {
                                        onColorsReordered(newColors)
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AppTheme.colors.BackgroundColor)
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            ) {
                                IconButton(
                                    modifier = Modifier
                                        .draggableHandle()
                                        .width(32.dp),
                                    onClick = {},
                                ) {
                                    PixelIcon(
                                        icon = R.drawable.ic_grab_handle,
                                        tint = AppTheme.colors.TextColorLight,
                                        contentDescription = stringResource(R.string.reorder)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                val itemIndex = uiColorsList.indexOf(item)
                                ColorBlock(
                                    color = item.color,
                                    onClick = { onColorClick(itemIndex) },
                                    modifier = Modifier
                                        .height(40.dp)
                                        .weight(9f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { onColorDelete(itemIndex) },
                                    modifier = Modifier.size(36.dp).weight(1f)
                                ) {
                                    PixelIcon(
                                        icon = R.drawable.ic_trash,
                                        contentDescription = stringResource(R.string.delete_color),
                                        tint = AppTheme.colors.ErrorDarkColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(AppTheme.colors.BackgroundColor)
                        .border(1.pixelDp, AppTheme.colors.Foreground2Color, MaterialTheme.shapes.small)
                        .clickable { onAddColorClick() }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.add_color),
                        color = AppTheme.colors.AccentButtonColor,
                        fontWeight = FontWeight.Bold
                    )
                }

            }

            if (isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppTheme.colors.BackgroundColorDarker.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppTheme.colors.SelectedColor)
                }
            }
        }
    }
}

@Composable
private fun ColorBlock(
    color: Color,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier
) {
    val textColor = if (color.luminance() < 0.4f) Color.White else Color.Black
    val showAlpha = color.alpha < 1f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(
                if (showAlpha) Modifier.drawCheckerboard() else Modifier
            )
            .background(color)
            .border(
                width = 2.dp,
                color = AppTheme.colors.BackgroundColorDarker
            )
            .clickable(
                onClick = onClick
            )
    ) {
        Text(
            text = color.toHexString(includeAlpha = showAlpha),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
private fun Preview() {
    InstaSpriteTheme {
        PaletteEditorContent(
            title = "Edit Palette",
            name = DummyData.palettes.first().name,
            author = DummyData.palettes.first().author,
            colors = DummyData.palettes.first().colors,
            isSaving = false,
            onNameChange = {},
            onAuthorChange = {},
            onSaveClick = {},
            onBackClick = {},
            onAddColorClick = {},
            onColorClick = {},
            onColorDelete = {},
            onColorsReordered = {}
        )
    }
}
