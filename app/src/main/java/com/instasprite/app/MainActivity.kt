package com.instasprite.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.instasprite.app.data.model.SettingPreferences
import com.instasprite.app.di.settingsDataStore
import com.instasprite.app.navigation.EntryProviderInstaller
import com.instasprite.app.navigation.Navigator
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.utils.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var entryProviderScopes: Set<@JvmSuppressWildcards EntryProviderInstaller>

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(AppSettings.onAttach(newBase))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (!isGranted) {
                    android.util.Log.w("MainActivity", "Notification permission denied")
                }
            }

            LaunchedEffect(Unit) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val permission = android.Manifest.permission.POST_NOTIFICATIONS
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            permission
                        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(permission)
                    }
                }
            }

            val settings by settingsDataStore.data.collectAsState(
                initial = SettingPreferences()
            )

            InstaSpriteTheme(flavour = settings.themeFlavour) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppTheme.colors.BackgroundColor)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {

                    val currentEntry = navigator.backStack.lastOrNull()

                    LaunchedEffect(currentEntry) {
                        navigator.onTransitionComplete()
                    }

                    NavDisplay(
                        backStack = navigator.backStack,
                        onBack = { navigator.goBack() },
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        entryProvider = entryProvider {
                            entryProviderScopes.forEach { builder -> this.builder() }
                        }
                    )
                }
            }
        }
    }
}