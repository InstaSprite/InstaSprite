package com.instasprite.app.ui.social.createpost

import com.instasprite.app.utils.pixelDp

import androidx.compose.ui.res.stringResource
import com.instasprite.app.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.instasprite.app.ui.components.shape.PixelShape
import com.instasprite.app.ui.social.createpost.composable.CaptionSection
import com.instasprite.app.ui.social.createpost.composable.HashtagSection
import com.instasprite.app.ui.social.createpost.composable.ImageSection
import com.instasprite.app.ui.components.composable.TopBar
import com.instasprite.app.ui.social.createpost.contract.CreatePostScreenEvent
import com.instasprite.app.ui.social.createpost.contract.CreatePostState
import com.instasprite.app.ui.social.feed.VerifyEmailState
import com.instasprite.app.ui.social.feed.dialog.VerifyEmailDialog
import com.instasprite.app.ui.theme.AppTheme
import com.instasprite.app.ui.theme.InstaSpriteTheme
import com.instasprite.app.ui.theme.ThemeFlavour
import java.io.File

@Composable
fun CreatePostScreen(
    onBackClick: () -> Unit = {},
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isPostCreated) {
        if (uiState.isPostCreated) onBackClick()
    }

    val event = remember(viewModel) {
        CreatePostScreenEvent(
            onBackClick = onBackClick,
            onCaptionChange = viewModel::onCaptionChange,
            onImageClick = viewModel::toggleSpriteSelector,
            onCommentEnabledChange = viewModel::onCommentEnabledChange,
            onCreatePost = viewModel::createPost,
            onToggleSpriteSelector = viewModel::toggleSpriteSelector,
            onSpriteSelected = viewModel::selectSpriteForPost,
            onHashtagInputChange = viewModel::onHashtagInputChange,
            onAddHashtag = viewModel::addHashtag,
            onRemoveHashtag = viewModel::removeHashtag
        )
    }

    CreatePostScreenContent(
        uiState = uiState,
        event = event
    )

    if (uiState.verifyEmailState.showVerifyDialog) {
        val context = LocalContext.current
        VerifyEmailDialog(
            verifyEmailState = uiState.verifyEmailState,
            onDismiss = viewModel::dismissEmailNotVerified,
            onConfirm = { viewModel.verifyEmail(context) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostScreenContent(
    uiState: CreatePostState,
    event: CreatePostScreenEvent
) {

    val modalBottomSheetProperties = remember {
        ModalBottomSheetProperties(
            isAppearanceLightStatusBars = false,
            isAppearanceLightNavigationBars = false,
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBar(
                title = stringResource(R.string.create_post),
                onBackClick = event.onBackClick,
                actions = {
                    Button(
                        onClick = event.onCreatePost,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.SelectedColor,
                            contentColor = AppTheme.colors.TextColorLight
                        ),
                        shape = MaterialTheme.shapes.small,
                        enabled = uiState.caption.isNotBlank() && uiState.selectedImage != null && !uiState.isPostInProgress,
                    ) {
                        Text(stringResource(R.string.post), color = AppTheme.colors.TextColorDark)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(AppTheme.colors.BackgroundColorDarker)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                ImageSection(
                    imageUri = uiState.selectedImage,
                    onClick = {
                        if (!uiState.isPostInProgress) {
                            event.onImageClick()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 266.pixelDp)
                        .padding(10.pixelDp)
                )
                CaptionSection(
                    enabled = !uiState.isPostInProgress,
                    value = uiState.caption,
                    onValueChange = event.onCaptionChange,
                    modifier = Modifier
                        .padding(top = 6.pixelDp)
                        .padding(horizontal = 10.pixelDp)
                        .fillMaxWidth()
                )
                HashtagSection(
                    enabled = !uiState.isPostInProgress,
                    inputValue = uiState.currentHashtagInput,
                    hashtags = uiState.hashtags,
                    onValueChange = event.onHashtagInputChange,
                    onAddHashtag = event.onAddHashtag,
                    onRemoveHashtag = event.onRemoveHashtag,
                    modifier = Modifier
                        .padding(horizontal = 10.pixelDp, vertical = 6.pixelDp)
                        .fillMaxWidth()
                )
//                OptionSection(
//                    isCommentChecked = uiState.commentEnabled,
//                    onEnableCommentChange = event.onCommentEnabledChange,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 10.pixelDp)
//                )
            }
        }

        if (uiState.showSpriteSelector) {

            ModalBottomSheet(
                onDismissRequest = event.onToggleSpriteSelector,
                properties = modalBottomSheetProperties,
                containerColor = AppTheme.colors.BackgroundColorDarker
            ) {
                val context = LocalContext.current
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.pixelDp)
                ) {
                    items(uiState.userSprites) { spriteWithMeta ->
                        val file =
                            File(context.filesDir, "thumbnail_${spriteWithMeta.sprite.id}.png")
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(2.pixelDp)
                                .clickable {
                                    event.onSpriteSelected(spriteWithMeta.sprite.id)
                                }
                        ) {
                            AsyncImage(
                                model = file,
                                contentDescription = spriteWithMeta.meta?.spriteName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .background(AppTheme.colors.BackgroundColor)
                                    .padding(2.pixelDp),
                                contentScale = ContentScale.FillHeight,
                                filterQuality = FilterQuality.None
                            )
                            Text(
                                text = spriteWithMeta.meta?.spriteName ?: "Untitled",
                                color = AppTheme.colors.TextColorLight,
                                modifier = Modifier.padding(top = 2.pixelDp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CreatePostScreenPreview() {
    InstaSpriteTheme(flavour = ThemeFlavour.MOCHA) {
        CreatePostScreenContent(
            uiState = CreatePostState(
                caption = "Sample Caption",
                commentEnabled = true
            ),
            event = CreatePostScreenEvent(
                onBackClick = {},
                onCaptionChange = {},
                onImageClick = {},
                onCommentEnabledChange = {},
                onCreatePost = {},
                onToggleSpriteSelector = {},
                onSpriteSelected = {},
                onHashtagInputChange = {},
                onAddHashtag = {},
                onRemoveHashtag = {}
            )
        )
    }
}