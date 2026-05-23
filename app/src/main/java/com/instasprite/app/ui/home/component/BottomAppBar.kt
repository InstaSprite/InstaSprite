package com.instasprite.app.ui.home.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.ExpandableFabMenu
import com.instasprite.app.ui.components.composable.FabMenuColors
import com.instasprite.app.ui.components.composable.FabMenuItem
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.gallery.contract.BottomBarEvent
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.pixelDp

@Composable
fun HomeBottomBar(
    onBottomBarEvent: (BottomBarEvent) -> Unit,
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        containerColor = AppTheme.colors.BottomBarColor,
        modifier = modifier
//            .clip(
//                BottomNavShape(
//                    dockRadius = with(LocalDensity.current) { 40.dp.toPx() },
//                ),
//            )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 3.dp, vertical = 2.dp)
                .weight(1f)

        ) {
            BottomBarItem(
                icon = R.drawable.ic_menu,
                onClick = onMenuClick,
                iconTint = AppTheme.colors.TextColorLight,
            )
        }

        // for FAB cutout
        Spacer(modifier = Modifier.weight(0.5f))

        Row(
            modifier = Modifier
                .padding(horizontal = 3.dp, vertical = 2.dp)
                .weight(1f),
            horizontalArrangement = Arrangement.End
        ) {
            BottomBarItem(
                icon = R.drawable.ic_search,
                onClick = {
                    onBottomBarEvent(BottomBarEvent.ToggleSearchBar)
                },
                iconTint = AppTheme.colors.TextColorLight
            )
            BottomBarItem(
                icon = R.drawable.ic_sort,
                onClick = {
                    onBottomBarEvent(BottomBarEvent.OpenDisplayOptions)
                },
                iconTint = AppTheme.colors.TextColorLight
            )
        }
    }
}

@Composable
fun BottomBarItem(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    iconTint: Color = Color.Unspecified
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        PixelIcon(
            icon = icon,
            contentDescription = stringResource(R.string.floating_action_button),
            tint = iconTint,
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFab(
    onCreateCanvas: () -> Unit,
    onLoadCanvas: () -> Unit,
    onLoadImage: () -> Unit,
) {
    val menuItems = remember(onCreateCanvas, onLoadCanvas, onLoadImage) {
        listOf(
            FabMenuItem(
                icon = R.drawable.ic_edit,
                label = "New Canvas",
                onClick = onCreateCanvas
            ),
            FabMenuItem(
                icon = R.drawable.ic_folder,
                label = "Load Canvas",
                onClick = onLoadCanvas
            ),
            FabMenuItem(
                icon = R.drawable.ic_canvas,
                label = "Load Image",
                onClick = onLoadImage
            )
        )
    }

    ExpandableFabMenu(
        itemWidth = 120.pixelDp,
        items = menuItems
    )

}

@Composable
fun FeedFab(
    onCreatePost: () -> Unit,
    colors: FabMenuColors = FabMenuColors.defaults()
) {
    FloatingActionButton(
        onClick = onCreatePost,
        containerColor = colors.fab,
        modifier = Modifier
            .size(32.pixelDp)
    ) {
        PixelIcon(
            icon = R.drawable.ic_plus,
            contentDescription = stringResource(R.string.create_post),
            tint = colors.fabIcon,
        )
    }
}

@Preview(showBackground = true, name = "Bottom Bar")
@Composable
private fun HomeBottomBarPreview() {
    InstaSpriteTheme {
        HomeBottomBar(
            onBottomBarEvent = {}
        )
    }
}
