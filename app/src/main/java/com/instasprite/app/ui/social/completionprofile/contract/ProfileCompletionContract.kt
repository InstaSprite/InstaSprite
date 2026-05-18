package com.instasprite.app.ui.social.completionprofile.contract

import android.net.Uri
import com.instasprite.app.data.network.model.EditProfileResponseDto

data class ProfileCompletionState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val profileData: EditProfileResponseDto? = null,
    val isProfileUpdated: Boolean = false,
    val selectedImageUri: Uri? = null,
    val isUploadingImage: Boolean = false,
    val imageUploadError: String? = null
)

data class ProfileCompletionScreenEvent(
    val onUpdateClick: (username: String, name: String, introduce: String?, email: String) -> Unit = { _, _, _, _ -> },
    val onErrorChanged: (String) -> Unit = {},
    val onImageSelected: (Uri?) -> Unit = {},
    val onUploadImage: () -> Unit = {},
    val onClearImageError: () -> Unit = {},
    val onProfileCompleted: () -> Unit = {}
)
