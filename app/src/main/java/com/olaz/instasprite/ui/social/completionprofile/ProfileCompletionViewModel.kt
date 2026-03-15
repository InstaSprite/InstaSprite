package com.olaz.instasprite.ui.social.completionprofile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olaz.instasprite.data.network.model.EditProfileRequestDto
import com.olaz.instasprite.data.repository.ProfileRepository
import com.olaz.instasprite.ui.social.completionprofile.contract.ProfileCompletionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileCompletionViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileCompletionState())
    val uiState: StateFlow<ProfileCompletionState> = _uiState.asStateFlow()

    fun loadProfileData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val response = repository.getEditProfile()
                if (response.status == 200 && response.data != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profileData = response.data
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = response.message ?: "Failed to load profile data"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error occurred"
                    )
                }
            }
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
            try {
                val request = EditProfileRequestDto(
                    memberUsername = username,
                    memberName = name,
                    memberIntroduce = introduce,
                    memberEmail = email
                )
                val response = repository.editProfile(request)
                if (response.status == 200) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isProfileUpdated = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = response.message ?: "Failed to update profile"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun selectImage(imageUri: Uri?) {
        _uiState.update {
            it.copy(
                selectedImageUri = imageUri,
                imageUploadError = null
            )
        }
    }

    fun uploadImage(context: Context) {
        val currentState = _uiState.value
        val imageUri = currentState.selectedImageUri

        if (imageUri == null) {
            _uiState.update { it.copy(imageUploadError = "No image selected") }
            return
        }

        _uiState.update {
            it.copy(
                isUploadingImage = true,
                imageUploadError = null
            )
        }

        viewModelScope.launch {
            try {
                val response = repository.uploadProfileImage(imageUri, context)
                if (response.status == 200) {
                    _uiState.update {
                        it.copy(
                            isUploadingImage = false,
                            imageUploadError = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isUploadingImage = false,
                            imageUploadError = response.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUploadingImage = false,
                        imageUploadError = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun clearImageError() {
        _uiState.update { it.copy(imageUploadError = null) }
    }
}
