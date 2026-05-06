package com.olaz.instasprite.ui.social.createpost.contract

import android.net.Uri
import com.olaz.instasprite.domain.model.SpriteWithMeta

data class CreatePostState(
    val caption: String = "",
    val selectedImage: Uri? = null,
    val commentEnabled: Boolean = true,
    val isPostInProgress: Boolean = false,
    val isPostCreated: Boolean = false,
    val showSpriteSelector: Boolean = false,
    val userSprites: List<SpriteWithMeta> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val currentHashtagInput: String = "",
    val showEmailNotVerified: Boolean = false
)

data class CreatePostScreenEvent(
    val onBackClick: () -> Unit,
    val onCaptionChange: (String) -> Unit,
    val onImageClick: () -> Unit,
    val onCommentEnabledChange: (Boolean) -> Unit,
    val onCreatePost: () -> Unit,
    val onToggleSpriteSelector: () -> Unit,
    val onSpriteSelected: (String) -> Unit,
    val onHashtagInputChange: (String) -> Unit,
    val onAddHashtag: () -> Unit,
    val onRemoveHashtag: (String) -> Unit
)
