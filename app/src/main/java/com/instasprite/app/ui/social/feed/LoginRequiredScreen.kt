package com.instasprite.app.ui.social.feed

import com.instasprite.app.utils.pixelDp

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.instasprite.app.ui.components.shape.PixelShape
import com.instasprite.app.ui.theme.AppTheme

@Composable
fun LoginRequiredScreen(
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.BackgroundColorDarker),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Login,
            contentDescription = stringResource(R.string.login_required_1),
            modifier = Modifier.size(54.pixelDp),
            tint = AppTheme.colors.Foreground2Color
        )

        Spacer(modifier = Modifier.height(8.pixelDp))

        Text(
            text = stringResource(R.string.login_required_1),
            style = MaterialTheme.typography.titleMedium,
            color = AppTheme.colors.TextColorLight,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.pixelDp))

        Button(
            onClick = onLoginClick,
            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.SelectedColor),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Login,
                contentDescription = stringResource(R.string.login),
                modifier = Modifier.size(14.pixelDp)
            )
            Spacer(modifier = Modifier.width(6.pixelDp))
            Text(stringResource(R.string.login), color = AppTheme.colors.TextColorDark)
        }
    }
}