package com.instasprite.app.ui.social.completionprofile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.data.network.model.EditProfileRequestDto
import com.instasprite.app.data.repository.ProfileRepository
import com.instasprite.app.ui.social.completionprofile.contract.ProfileCompletionState
import com.instasprite.app.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileCompletionViewModel @Inject constructor(
    private val repository: ProfileRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileCompletionState())
    val uiState: StateFlow<ProfileCompletionState> = _uiState.asStateFlow()

    fun loadProfileData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.getEditProfile().fold(
                onSuccess = { data ->
                    _uiState.update { it.copy(isLoading = false, profileData = data) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.toUserMessage(context))
                    }
                }
            )
        }
    }

    fun updateProfile(
        username: String,
        name: String,
        introduce: String?,
        email: String
    ) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val request = EditProfileRequestDto(
                memberUsername = username,
                memberName = name,
                memberIntroduce = introduce,
                memberEmail = email
            )
            repository.editProfile(request).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isProfileUpdated = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.toUserMessage(context))
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun selectImage(imageUri: Uri?) {
        _uiState.update {
            it.copy(selectedImageUri = imageUri, imageUploadError = null)
        }
    }

    fun uploadImage(context: Context) {
        val imageUri = _uiState.value.selectedImageUri
        if (imageUri == null) {
            _uiState.update { it.copy(imageUploadError = "No image selected") }
            return
        }

        _uiState.update { it.copy(isUploadingImage = true, imageUploadError = null) }

        viewModelScope.launch {
            repository.uploadProfileImage(imageUri, context).fold(
                onSuccess = {
                    _uiState.update { it.copy(isUploadingImage = false, imageUploadError = null) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isUploadingImage = false, imageUploadError = error.toUserMessage(context))
                    }
                }
            )
        }
    }

    fun clearImageError() {
        _uiState.update { it.copy(imageUploadError = null) }
    }
}
