package com.instasprite.app.ui.social.editprofile.contract

import android.net.Uri
import com.instasprite.app.domain.model.SpriteWithMeta

data class EditProfileState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val displayName: String = "",
    val bio: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val pendingAvatarUri: Uri? = null,
    val showAvatarSourceSheet: Boolean = false,
    val showSpritePicker: Boolean = false,
    val userSprites: List<SpriteWithMeta> = emptyList(),
    val errorMessage: String? = null,
    val savedSuccess: Boolean = false,
)

data class EditProfileEvent(
    val onBackClick: () -> Unit = {},
    val onDisplayNameChange: (String) -> Unit = {},
    val onBioChange: (String) -> Unit = {},
    val onOpenAvatarSourceSheet: () -> Unit = {},
    val onDismissAvatarSourceSheet: () -> Unit = {},
    val onPickFromDevice: () -> Unit = {},
    val onPickFromSprite: () -> Unit = {},
    val onDismissSpritePicker: () -> Unit = {},
    val onSpriteSelected: (spriteId: String) -> Unit = {},
    val onAvatarPicked: (Uri) -> Unit = {},
    val onSave: () -> Unit = {},
    val onClearError: () -> Unit = {},
)
