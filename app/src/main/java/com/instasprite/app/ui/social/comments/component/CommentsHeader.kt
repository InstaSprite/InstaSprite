package com.instasprite.app.ui.social.comments.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun CommentsHeader(
    commentsCount: Int
) {
    LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${stringResource(R.string.comments)} ",
            color = AppTheme.colors.TextColorLight,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "($commentsCount)",
            color = AppTheme.colors.TextColorLight.copy(alpha = 0.5f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
