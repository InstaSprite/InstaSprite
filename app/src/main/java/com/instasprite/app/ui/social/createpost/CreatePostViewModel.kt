package com.instasprite.app.ui.social.createpost

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasprite.app.R
import com.instasprite.app.data.network.ApiError
import com.instasprite.app.data.repository.AuthRepository
import com.instasprite.app.data.repository.PostRepository
import com.instasprite.app.data.repository.SpriteDatabaseRepository
import com.instasprite.app.ui.social.PostInteractionEvent
import com.instasprite.app.ui.social.createpost.contract.CreatePostState
import com.instasprite.app.utils.toUserMessage
import java.io.File
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val postRepository: PostRepository,
    private val spriteDatabaseRepository: SpriteDatabaseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostState())
    val uiState: StateFlow<CreatePostState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            spriteDatabaseRepository.getAllSpritesWithMeta().collect { sprites ->
                _uiState.update { it.copy(userSprites = sprites) }
            }
        }
    }

    fun onCaptionChange(v: String) {
        _uiState.update { it.copy(caption = v) }
    }

    fun onImageChange(v: Uri?) {
        _uiState.update { it.copy(selectedImage = v) }
    }

    fun onCommentEnabledChange(v: Boolean) {
        _uiState.update { it.copy(commentEnabled = v) }
    }

    fun toggleSpriteSelector() {
        _uiState.update { it.copy(showSpriteSelector = !it.showSpriteSelector) }
    }

    fun selectSpriteForPost(spriteId: String) {
        val file = File(context.filesDir, "thumbnail_$spriteId.png")
        _uiState.update { 
            it.copy(
                selectedImage = Uri.fromFile(file),
                showSpriteSelector = false
            )
        }
    }

    fun onHashtagInputChange(v: String) {
        _uiState.update { it.copy(currentHashtagInput = v) }
    }

    fun addHashtag() {
        val input = _uiState.value.currentHashtagInput.trim().removePrefix("#").lowercase()
        if (input.isNotBlank() && !_uiState.value.hashtags.contains(input)) {
            _uiState.update { it.copy(
                hashtags = it.hashtags + input,
                currentHashtagInput = ""
            ) }
        } else {
            _uiState.update { it.copy(currentHashtagInput = "") }
        }
    }

    fun removeHashtag(tag: String) {
        _uiState.update { it.copy(
            hashtags = it.hashtags - tag
        ) }
    }

    fun createPost() {
        val caption = _uiState.value.caption
        val selectedImage = _uiState.value.selectedImage ?: return
        val commentEnabled = _uiState.value.commentEnabled
        val hashtags = _uiState.value.hashtags

        _uiState.update { it.copy(isPostInProgress = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = postRepository.createPost(
                    content = caption,
                    images = listOf(selectedImage),
                    altTexts = listOf(""),
                    hashtags = hashtags,
                    commentFlag = commentEnabled
                )

                result.fold(
                    onSuccess = {
                        PostInteractionEvent.emitPostCreated()
                        _uiState.update { it.copy(isPostCreated = true) }
                        viewModelScope.launch(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.post_created_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isPostInProgress = false) }
                        if (error is ApiError.Unauthorized && error.code == "M018") {
                            _uiState.update { it.copy(verifyEmailState = it.verifyEmailState.copy(showVerifyDialog = true)) }
                        } else {
                            viewModelScope.launch(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.post_created_fail),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isPostInProgress = false) }
            }
        }
    }

    fun dismissEmailNotVerified() {
        _uiState.update { it.copy(verifyEmailState = it.verifyEmailState.copy(showVerifyDialog = false)) }
    }

    fun verifyEmail(context: Context) {
        _uiState.update {
            it.copy(verifyEmailState = it.verifyEmailState.copy(isSending = true))
        }
        viewModelScope.launch {
            authRepository.verifyEmail().fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            verifyEmailState = it.verifyEmailState.copy(
                                isSending = false,
                                success = true
                            )
                        )
                    }
                    Toast.makeText(
                        context,
                        context.getString(R.string.email_sent),
                        Toast.LENGTH_SHORT
                    ).show()
                    dismissEmailNotVerified()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            verifyEmailState = it.verifyEmailState.copy(
                                isSending = false,
                                success = false,
                                message = error.toUserMessage(context)
                            )
                        )
                    }
                }
            )
        }
    }
}
