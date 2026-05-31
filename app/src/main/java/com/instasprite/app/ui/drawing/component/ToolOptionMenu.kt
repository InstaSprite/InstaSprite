package com.instasprite.app.ui.drawing.component

import com.instasprite.app.utils.pixelDp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.instasprite.app.domain.tool.Tool
import com.instasprite.app.ui.components.composable.TitledBox
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun ToolOptionMenu(
    selectedTool: Tool,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    TitledBox(
        title = stringResource(id = selectedTool.nameRes),
        titleBackgroundColor = AppTheme.colors.BackgroundColorDarker,
        modifier = modifier
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.BackgroundColor),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.pixelDp),
            contentPadding = PaddingValues(horizontal = 6.pixelDp)
        ) {
            content()
        }
    }
}

