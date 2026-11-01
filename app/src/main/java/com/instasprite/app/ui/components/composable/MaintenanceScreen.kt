package com.instasprite.app.ui.components.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.instasprite.app.R
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.pixelDp

@Composable
fun MaintenanceScreen(
    modifier: Modifier = Modifier,
    onReload: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceInset),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PixelIcon(
            icon = R.drawable.ic_wrench,
            contentDescription = null,
            tint = AppTheme.colors.orange,
            scale = 2f,
        )

        Spacer(modifier = Modifier.height(16.pixelDp))

        Text(
            text = stringResource(R.string.error_maintenance_title),
            color = AppTheme.colors.onSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.pixelDp)
        )

        Spacer(modifier = Modifier.height(6.pixelDp))

        Text(
            text = stringResource(R.string.error_maintenance_desc),
            color = AppTheme.colors.textMuted,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.pixelDp)
        )

        Spacer(modifier = Modifier.height(16.pixelDp))

        Button(
            onClick = onReload,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.blue,
                contentColor = AppTheme.colors.surfaceInset
            )
        ) {
            Text(
                text = stringResource(R.string.reload),
                color = AppTheme.colors.onAccent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview()
@Composable
private fun MaintenanceScreenPreview() {
    InstaSpriteTheme {
        MaintenanceScreen()
    }
}
