package com.olaz.instasprite.ui.social.createpost.contract

import android.net.Uri

data class CreatePostState(
    val caption: String = "",
    val selectedImage: Uri? = null,
    val commentEnabled: Boolean = true,
    val isPostInProgress: Boolean = false,
    val isPostCreated: Boolean = false,
)

data class CreatePostScreenEvent(
    val onBackClick: () -> Unit,
    val onCaptionChange: (String) -> Unit,
    val onImageClick: () -> Unit,
    val onCommentEnabledChange: (Boolean) -> Unit,
    val onCreatePost: () -> Unit
)
