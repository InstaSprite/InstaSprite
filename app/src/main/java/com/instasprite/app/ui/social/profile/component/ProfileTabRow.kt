package com.instasprite.app.ui.social.profile.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.instasprite.app.ui.social.profile.contract.ProfileTab
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun ProfileTabRow(
    selectedTabIndex: Int,
    tabs: Array<ProfileTab>,
    onTabSelected: (Int) -> Unit
) {
    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = AppTheme.colors.BackgroundColorDarker,
        contentColor = AppTheme.colors.TextColorLight,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                color = AppTheme.colors.TextColorLight
            )
        },
        divider = { HorizontalDivider(color = Color.Transparent) },
        modifier = Modifier.fillMaxWidth()
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = tab.title,
                        fontSize = 16.sp,
                        color = if (selectedTabIndex == index)
                            AppTheme.colors.TextColorLight
                        else
                            AppTheme.colors.TextColorLight.copy(alpha = 0.6f),
                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}
