package com.olaz.instasprite.ui.social.createpost

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.io.File
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.olaz.instasprite.domain.model.SpriteWithMeta
import com.olaz.instasprite.ui.social.createpost.composable.CaptionSection
import com.olaz.instasprite.ui.social.createpost.composable.ImageSection
import com.olaz.instasprite.ui.social.createpost.composable.OptionSection
import com.olaz.instasprite.ui.social.createpost.composable.TopBar
import com.olaz.instasprite.ui.social.createpost.contract.CreatePostScreenEvent
import com.olaz.instasprite.ui.social.createpost.contract.CreatePostState
import com.olaz.instasprite.ui.theme.CatppuccinUI
import com.olaz.instasprite.ui.theme.InstaSpriteTheme

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
            onSpriteSelected = viewModel::selectSpriteForPost
        )
    }

    CreatePostScreenContent(
        uiState = uiState,
        event = event
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostScreenContent(
    uiState: CreatePostState,
    event: CreatePostScreenEvent
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopBar(onDismiss = event.onBackClick)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .background(CatppuccinUI.BackgroundColorDarker)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CaptionSection(
                    enabled = !uiState.isPostInProgress,
                    value = uiState.caption,
                    onValueChange = event.onCaptionChange,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                )
                ImageSection(
                    imageUri = uiState.selectedImage,
                    onClick = {
                        if (!uiState.isPostInProgress) {
                            event.onImageClick()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .padding(16.dp)
                )
                OptionSection(
                    isCommentChecked = uiState.commentEnabled,
                    onEnableCommentChange = event.onCommentEnabledChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Button(
                    onClick = event.onCreatePost,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CatppuccinUI.SelectedColor,
                        contentColor = CatppuccinUI.TextColorLight
                    ),
                    enabled = uiState.caption.isNotBlank() && uiState.selectedImage != null && !uiState.isPostInProgress,
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    Text("Post", color = CatppuccinUI.TextColorDark)
                }
            }
        }
        
        if (uiState.showSpriteSelector) {
            ModalBottomSheet(
                onDismissRequest = event.onToggleSpriteSelector,
                containerColor = CatppuccinUI.BackgroundColorDarker
            ) {
                val context = LocalContext.current
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    items(uiState.userSprites) { spriteWithMeta ->
                        val file = File(context.filesDir, "thumbnail_${spriteWithMeta.sprite.id}.png")
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable {
                                    event.onSpriteSelected(spriteWithMeta.sprite.id)
                                }
                        ) {
                            coil3.compose.AsyncImage(
                                model = file,
                                contentDescription = spriteWithMeta.meta?.spriteName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CatppuccinUI.BackgroundColor)
                                    .padding(4.dp),
                                contentScale = ContentScale.FillBounds,
                                filterQuality = FilterQuality.None
                            )
                            Text(
                                text = spriteWithMeta.meta?.spriteName ?: "Untitled",
                                color = CatppuccinUI.TextColorLight,
                                modifier = Modifier.padding(top = 4.dp),
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
    InstaSpriteTheme(darkTheme = false) {
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
                onSpriteSelected = {}
            )
        )
    }
}