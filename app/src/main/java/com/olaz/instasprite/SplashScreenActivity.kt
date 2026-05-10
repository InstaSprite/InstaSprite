package com.olaz.instasprite

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.olaz.instasprite.ui.theme.AppTheme
import com.olaz.instasprite.utils.UiUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("RestrictedApi", "CustomSplashScreen")
class SplashScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_InstaSprite)
        super.onCreate(savedInstanceState)

        setContent(
            content = {
                SplashScreen()
            }
        )

        lifecycleScope.launch {
            delay(1000L)
            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            finish()
        }
    }
}

@Composable
@Preview
fun SplashScreen() {
    UiUtils.SetStatusBarColor(AppTheme.colors.BackgroundColor)
    UiUtils.SetNavigationBarColor(AppTheme.colors.BackgroundColor)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center
    )
    {
        Image(
            painter = painterResource(R.drawable.splash),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight(0.7f),
            contentDescription = "Splash Screen",
        )
    }
}