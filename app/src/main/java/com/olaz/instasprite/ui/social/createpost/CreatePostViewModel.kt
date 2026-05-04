package com.olaz.instasprite.ui.social.createpost

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olaz.instasprite.R
import com.olaz.instasprite.data.repository.PostRepository
import com.olaz.instasprite.data.repository.SpriteDatabaseRepository
import com.olaz.instasprite.ui.social.PostInteractionEvent
import com.olaz.instasprite.ui.social.createpost.contract.CreatePostState
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
    private val spriteDatabaseRepository: SpriteDatabaseRepository
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

    fun createPost() {
        val caption = _uiState.value.caption
        val selectedImage = _uiState.value.selectedImage ?: return
        val commentEnabled = _uiState.value.commentEnabled

        _uiState.update { it.copy(isPostInProgress = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = postRepository.uploadPostWithUris(
                    content = caption,
                    images = listOf(selectedImage),
                    altTexts = listOf(""),
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
                    onFailure = {
                        _uiState.update { it.copy(isPostInProgress = false) }
                        viewModelScope.launch(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.post_created_fail),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isPostInProgress = false) }
            }
        }
    }
}
