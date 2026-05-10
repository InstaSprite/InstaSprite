package com.olaz.instasprite.ui.components.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olaz.instasprite.R
import com.olaz.instasprite.ui.theme.AppTheme

@Composable
fun MaintenanceScreen(
    modifier: Modifier = Modifier,
    onReload: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.BackgroundColorDarker),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = null,
            tint = AppTheme.colors.WarningColor,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.error_maintenance_title),
            color = AppTheme.colors.TextColorLight,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.error_maintenance_desc),
            color = AppTheme.colors.Subtext0Color,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onReload,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.LinkColor,
                contentColor = AppTheme.colors.BackgroundColorDarker
            )
        ) {
            Text(
                text = stringResource(R.string.reload),
                color = AppTheme.colors.TextColorDark,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF11111B)
@Composable
fun MaintenanceScreenPreview() {
    MaintenanceScreen()
}
