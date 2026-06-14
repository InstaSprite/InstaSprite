package com.instasprite.app.ui.onboarding

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import com.instasprite.app.R
import com.instasprite.app.ui.components.composable.FlavourCard
import com.instasprite.app.ui.components.composable.FontCard
import com.instasprite.app.ui.components.composable.PixelIcon
import com.instasprite.app.ui.theme.AppFont
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.Catppuccin
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.ui.theme.ThemeFlavour
import com.instasprite.app.utils.UiUtils
import com.instasprite.app.utils.pixelDp
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onNavigateHome: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val colors = AppTheme.colors

    UiUtils.SetStatusBarColor(colors.BackgroundColorDarker)
    UiUtils.SetNavigationBarColor(colors.BackgroundColorDarker)

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.pixelDp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.pixelDp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(8.pixelDp)
                                .clip(MaterialTheme.shapes.small)
                                .background(
                                    if (isSelected) colors.LinkColor else colors.Foreground2Color
                                )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            viewModel.finishOnboarding()
                            onNavigateHome()
                        }
                    },
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.AccentButtonColor,
                        contentColor = colors.TextColorDark
                    )
                ) {
                    Text(
                        text = if (pagerState.currentPage < 2) stringResource(R.string.onboarding_next) else stringResource(
                            R.string.onboarding_get_started
                        ),
                        color = colors.TextColorDark
                    )
                }
            }
        },
        containerColor = colors.BackgroundColorDarker
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> AppearancePage(
                    uiState = uiState,
                    onAppFontSelected = { viewModel.setAppFont(it) },
                    onThemeFlavourSelected = { viewModel.setThemeFlavour(it) }
                )

                2 -> CursorModePage(
                    uiState = uiState,
                    onCursorModeSelected = { viewModel.setCursorMode(it) }
                )
            }
        }
    }
}

@Composable
fun WelcomePage() {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.pixelDp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.BackgroundColorDarker)
                .padding(8.pixelDp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Icon
                PixelIcon(
                    icon = R.drawable.ic_launcher,
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.size(80.pixelDp)
                )
                Spacer(modifier = Modifier.height(12.pixelDp))

                Text(
                    text = stringResource(R.string.onboarding_welcome_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.TextColorLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.pixelDp))


                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.pixelDp),
                    modifier = Modifier.padding(horizontal = 32.pixelDp)
                ) {
                    val palette = Catppuccin.Mocha
                    listOf(
                        palette.Red, palette.Peach, palette.Yellow, palette.Green,
                        palette.Teal, palette.Blue, palette.Mauve, palette.Pink,
                        palette.Flamingo, palette.Rosewater
                    ).forEach { color ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .size(4.pixelDp)
                                .background(color)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.pixelDp))

                Text(
                    text = stringResource(R.string.onboarding_welcome_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.Subtext0Color,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AppearancePage(
    uiState: OnboardingUiState,
    onAppFontSelected: (AppFont) -> Unit,
    onThemeFlavourSelected: (ThemeFlavour) -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.pixelDp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(colors.BackgroundColorDarker)
                .border(1.pixelDp, colors.LinkColor, MaterialTheme.shapes.small)
                .padding(8.pixelDp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.onboarding_personalize_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.TextColorLight
                )
                Spacer(modifier = Modifier.height(8.pixelDp))
                Text(
                    text = stringResource(R.string.onboarding_personalize_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.Subtext0Color,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.pixelDp))

                // Font Section
                Text(
                    text = stringResource(R.string.onboarding_font),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.TextColorLight,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.pixelDp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AppFont.entries.forEach { font ->
                        val isSelected = uiState.appFont == font
                        FontCard(
                            font = font,
                            isSelected = isSelected,
                            onClick = { onAppFontSelected(font) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 1.pixelDp)
                                .height(64.pixelDp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.pixelDp))

                // Theme Section
                Text(
                    text = stringResource(R.string.theme),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.TextColorLight,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.pixelDp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ThemeFlavour.entries.forEach { flavour ->
                        val previewColors = Catppuccin.toAppColors(Catppuccin.fromFlavour(flavour))
                        val isSelected = uiState.themeFlavour == flavour
                        FlavourCard(
                            label = flavour.label,
                            previewColors = previewColors,
                            isSelected = isSelected,
                            onClick = { onThemeFlavourSelected(flavour) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.pixelDp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CursorModePage(
    uiState: OnboardingUiState,
    onCursorModeSelected: (Boolean) -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.pixelDp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(colors.BackgroundColorDarker)
                .border(1.pixelDp, colors.LinkColor, MaterialTheme.shapes.small)
                .padding(8.pixelDp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.onboarding_drawing_mode_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.TextColorLight
                )
                Spacer(modifier = Modifier.height(8.pixelDp))
                Text(
                    text = stringResource(R.string.onboarding_drawing_mode_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.Subtext0Color,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.pixelDp))

                // Options List
                val context = LocalContext.current
                val imageLoader = coil3.ImageLoader.Builder(context)
                    .components {
                        if (Build.VERSION.SDK_INT >= 28) {
                            add(AnimatedImageDecoder.Factory())
                        } else {
                            add(GifDecoder.Factory())
                        }
                    }
                    .build()

                // Selected GIF at the top
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (uiState.isCursorMode) R.drawable.pen_mode else R.drawable.normal_mode)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = "Mode Demo",
//                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .aspectRatio(3/4f)
                        .clip(MaterialTheme.shapes.small)
                        .background(colors.Foreground1Color)
                )

                Spacer(modifier = Modifier.height(8.pixelDp))

                // Touch Mode Option
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(colors.BackgroundColor)
                        .border(
                            1.pixelDp,
                            if (!uiState.isCursorMode) colors.LinkColor else colors.Foreground2Color,
                            MaterialTheme.shapes.small
                        )
                        .clickable { onCursorModeSelected(false) }
                        .padding(8.pixelDp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !uiState.isCursorMode,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = colors.LinkColor)
                        )
                        Spacer(modifier = Modifier.width(8.pixelDp))
                        Text(
                            text = stringResource(R.string.onboarding_touch_mode),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.TextColorLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.pixelDp))
                    Text(
                        text = stringResource(R.string.onboarding_touch_mode_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.Subtext0Color,
                        modifier = Modifier.padding(start = 8.pixelDp)
                    )
                }

                Spacer(modifier = Modifier.height(8.pixelDp))

                // Cursor Mode Option
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(colors.BackgroundColor)
                        .border(
                            1.pixelDp,
                            if (uiState.isCursorMode) colors.LinkColor else colors.Foreground2Color,
                            MaterialTheme.shapes.small
                        )
                        .clickable { onCursorModeSelected(true) }
                        .padding(12.pixelDp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = uiState.isCursorMode,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = colors.LinkColor)
                        )
                        Spacer(modifier = Modifier.width(8.pixelDp))
                        Text(
                            text = stringResource(R.string.onboarding_cursor_mode),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.TextColorLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.pixelDp))
                    Text(
                        text = stringResource(R.string.onboarding_cursor_mode_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.Subtext0Color,
                        modifier = Modifier.padding(start = 8.pixelDp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomePagePreview() {
    InstaSpriteTheme {
        WelcomePage()
    }
}

@Preview(showBackground = true)
@Composable
private fun AppearancePagePreview() {
    InstaSpriteTheme {
        AppearancePage(
            uiState = OnboardingUiState(),
            onAppFontSelected = {},
            onThemeFlavourSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CursorModePagePreview() {
    InstaSpriteTheme {
        CursorModePage(
            uiState = OnboardingUiState(),
            onCursorModeSelected = {}
        )
    }
}
