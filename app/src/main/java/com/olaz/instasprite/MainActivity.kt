package com.olaz.instasprite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.olaz.instasprite.navigation.NavigationRoot
import com.olaz.instasprite.ui.theme.InstaSpriteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InstaSpriteTheme {
                NavigationRoot(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}