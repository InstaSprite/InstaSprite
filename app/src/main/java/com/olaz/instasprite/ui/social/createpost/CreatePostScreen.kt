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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? -> uri?.let { viewModel.onImageChange(it) } }
    )

    LaunchedEffect(uiState.isPostCreated) {
        if (uiState.isPostCreated) onBackClick()
    }

    val event = remember(viewModel) {
        CreatePostScreenEvent(
            onBackClick = onBackClick,
            onCaptionChange = viewModel::onCaptionChange,
            onImageClick = { imagePickerLauncher.launch(arrayOf("image/*")) },
            onCommentEnabledChange = viewModel::onCommentEnabledChange,
            onCreatePost = viewModel::createPost
        )
    }

    CreatePostScreenContent(
        uiState = uiState,
        event = event
    )
}

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
                onCreatePost = {}
            )
        )
    }
}