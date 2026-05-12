package com.instasprite.app.ui.home.component

import androidx.compose.ui.res.stringResource

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.ExpandableFabMenu
import com.instasprite.app.ui.components.composable.FabMenuColors
import com.instasprite.app.ui.components.composable.FabMenuItem
import com.instasprite.app.ui.gallery.contract.BottomBarEvent
import com.instasprite.app.ui.theme.AppTheme

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
                imageVector = Icons.Default.Menu,
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
                imageVector = Icons.Default.Search,
                onClick = {
                    onBottomBarEvent(BottomBarEvent.ToggleSearchBar)
                },
                iconTint = AppTheme.colors.TextColorLight
            )
            BottomBarItem(
                iconResourceId = R.drawable.ic_sort,
                onClick = {
                    onBottomBarEvent(BottomBarEvent.OpenSelectSortOption)
                },
                iconTint = AppTheme.colors.TextColorLight
            )
        }
    }
}

@Composable
fun BottomBarItem(
    imageVector: ImageVector,
    onClick: () -> Unit,
    size: Dp = 28.dp,
    iconTint: Color = Color.Unspecified
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = stringResource(R.string.floating_action_button),
            tint = iconTint,
            modifier = Modifier.size(size)
        )
    }
}

@Composable
fun BottomBarItem(
    @DrawableRes iconResourceId: Int,
    onClick: () -> Unit,
    size: Dp = 28.dp,
    iconTint: Color = Color.Unspecified
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Icon(
            painter = painterResource(id = iconResourceId),
            contentDescription = stringResource(R.string.floating_action_button),
            tint = iconTint,
            modifier = Modifier.size(size)
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
                icon = Icons.Default.Create,
                label = "New Canvas",
                onClick = onCreateCanvas
            ),
            FabMenuItem(
                icon = Icons.Default.FolderOpen,
                label = "Load Canvas",
                onClick = onLoadCanvas
            ),
            FabMenuItem(
                icon = Icons.Default.Image,
                label = "Load Image",
                onClick = onLoadImage
            )
        )
    }

    ExpandableFabMenu(
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
        shape = CircleShape,
        containerColor = AppTheme.colors.TextColorLight,
        modifier = Modifier
            .size(70.dp)
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = stringResource(R.string.create_post),
            tint = colors.fabIcon,
            modifier = Modifier
                .size(30.dp)
        )
    }
}
