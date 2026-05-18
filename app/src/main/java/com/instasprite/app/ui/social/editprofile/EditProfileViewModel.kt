package com.instasprite.app.ui.social.editprofile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.data.network.model.EditProfileRequestDto
import com.instasprite.app.data.repository.AccountRepository
import com.instasprite.app.data.repository.ProfileRepository
import com.instasprite.app.data.repository.SpriteDatabaseRepository
import com.instasprite.app.ui.social.PostInteractionEvent
import com.instasprite.app.ui.social.editprofile.contract.EditProfileState
import com.instasprite.app.domain.session.SocialSessionManager
import com.instasprite.app.utils.Constants
import com.instasprite.app.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val accountRepository: AccountRepository,
    private val spriteDatabaseRepository: SpriteDatabaseRepository,
    private val sessionManager: SocialSessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    init {
        loadProfile()
        observeSprites()
    }

    private fun loadProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true) }
            profileRepository.getEditProfile().fold(
                onSuccess = { data ->
                    val imageUrl = data.memberImageUrl
                    val finalUrl = if (imageUrl.isNotEmpty()) {
                        if (imageUrl.startsWith("http")) imageUrl
                        else "${Constants.BASE_URL}/images/$imageUrl"
                    } else null
                    _state.update {
                        it.copy(
                            isLoading = false,
                            displayName = data.memberName ?: "",
                            bio = data.memberIntroduce ?: "",
                            email = data.memberEmail ?: "",
                            avatarUrl = finalUrl,
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.toUserMessage(context))
                    }
                }
            )
        }
    }

    private fun observeSprites() {
        viewModelScope.launch {
            spriteDatabaseRepository.getAllSpritesWithMeta().collect { sprites ->
                _state.update { it.copy(userSprites = sprites) }
            }
        }
    }

    fun onDisplayNameChange(value: String) {
        _state.update { it.copy(displayName = value) }
    }

    fun onBioChange(value: String) {
        _state.update { it.copy(bio = value) }
    }

    fun openAvatarSourceSheet() {
        _state.update { it.copy(showAvatarSourceSheet = true) }
    }

    fun dismissAvatarSourceSheet() {
        _state.update { it.copy(showAvatarSourceSheet = false) }
    }

    fun openSpritePicker() {
        _state.update { it.copy(showAvatarSourceSheet = false, showSpritePicker = true) }
    }

    fun dismissSpritePicker() {
        _state.update { it.copy(showSpritePicker = false) }
    }

    fun selectSpriteForAvatar(spriteId: String) {
        val file = File(context.filesDir, "thumbnail_$spriteId.png")
        val uri = Uri.fromFile(file)
        _state.update { it.copy(pendingAvatarUri = uri, showSpritePicker = false) }
    }

    fun onAvatarPicked(uri: Uri) {
        _state.update { it.copy(pendingAvatarUri = uri, showAvatarSourceSheet = false) }
    }

    fun save() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            val currentState = _state.value

            val pendingUri = currentState.pendingAvatarUri
            if (pendingUri != null) {
                profileRepository.uploadProfileImage(pendingUri, context).fold(
                    onSuccess = { saveProfileFields(currentState.displayName, currentState.bio) },
                    onFailure = { error ->
                        _state.update {
                            it.copy(isSaving = false, errorMessage = error.toUserMessage(context))
                        }
                    }
                )
            } else {
                saveProfileFields(currentState.displayName, currentState.bio)
            }
        }
    }

    private suspend fun saveProfileFields(displayName: String, bio: String) {
        profileRepository.getEditProfile().fold(
            onSuccess = { editData ->
                val request = EditProfileRequestDto(
                    memberUsername = editData.memberUsername ?: "",
                    memberName = displayName,
                    memberIntroduce = bio,
                    memberEmail = editData.memberEmail ?: ""
                )
                profileRepository.editProfile(request).fold(
                    onSuccess = {
                        sessionManager.refreshCurrentUser()
                        PostInteractionEvent.emitProfileRefreshEvent()
                        _state.update { it.copy(isSaving = false, savedSuccess = true) }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(isSaving = false, errorMessage = error.toUserMessage(context))
                        }
                    }
                )
            },
            onFailure = { error ->
                _state.update {
                    it.copy(isSaving = false, errorMessage = error.toUserMessage(context))
                }
            }
        )
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
