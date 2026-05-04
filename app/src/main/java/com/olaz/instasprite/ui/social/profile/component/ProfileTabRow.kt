package com.olaz.instasprite.ui.social.profile.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.olaz.instasprite.ui.social.profile.contract.ProfileTab
import com.olaz.instasprite.ui.theme.CatppuccinUI

@Composable
fun ProfileTabRow(
    selectedTabIndex: Int,
    tabs: Array<ProfileTab>,
    onTabSelected: (Int) -> Unit
) {
    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = CatppuccinUI.BackgroundColorDarker,
        contentColor = CatppuccinUI.TextColorLight,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                color = CatppuccinUI.TextColorLight
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
                            CatppuccinUI.TextColorLight
                        else
                            CatppuccinUI.TextColorLight.copy(alpha = 0.6f),
                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}
