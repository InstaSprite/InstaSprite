package com.instasprite.app.ui.gallery.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.gallery.contract.SearchBarContract
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun SearchBar(
    onSearchBarEvent: (SearchBarContract) -> Unit,
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    var isFocused by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BackHandler(enabled = isFocused) {
        if (searchQuery.isEmpty()) {
            onSearchBarEvent(SearchBarContract.ToggleSearchBar)
        }
        focusManager.clearFocus()
    }

    TextField(
        value = searchQuery,
        onValueChange = {
            onSearchBarEvent(
                SearchBarContract.UpdateSearchQuery(
                    it
                )
            )
        },
        placeholder = {
            Text(
                text = stringResource(R.string.search_sprites),
                color = AppTheme.colors.textMuted
            )
        },
        singleLine = true,
        trailingIcon = {
            IconButton(
                onClick = {
                    if (searchQuery.isEmpty()) {
                        onSearchBarEvent(SearchBarContract.ToggleSearchBar)
                    }
                    onSearchBarEvent(SearchBarContract.UpdateSearchQuery(""))
                },
            ) {
                PixelIcon(
                    icon = R.drawable.ic_close,
                    contentDescription = stringResource(R.string.dismiss),
                    tint = AppTheme.colors.red,
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged {
                isFocused = it.isFocused
            },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppTheme.colors.background,
            disabledContainerColor = AppTheme.colors.background,
            unfocusedContainerColor = AppTheme.colors.background,
            focusedTextColor = AppTheme.colors.onSurface,
            unfocusedTextColor = AppTheme.colors.onSurface,
            cursorColor = AppTheme.colors.onSurface,
            focusedBorderColor = AppTheme.colors.orange,
            unfocusedBorderColor = AppTheme.colors.orange,
            unfocusedPlaceholderColor = AppTheme.colors.orange,
            focusedPlaceholderColor = AppTheme.colors.textMuted
        ),
    )

}