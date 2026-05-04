package com.olaz.instasprite.ui.social.hashtag

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.olaz.instasprite.data.paging.HashtagPagingSource
import com.olaz.instasprite.data.repository.PostRepository
import com.olaz.instasprite.domain.model.PostData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class HashtagFeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _hashtag = MutableStateFlow("")
    val hashtag: StateFlow<String> = _hashtag.asStateFlow()

    fun setHashtag(tag: String) {
        if (_hashtag.value != tag) {
            _hashtag.value = tag
        }
    }

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    val pagedPosts: Flow<PagingData<PostData>> = _hashtag.flatMapLatest { tag ->
        if (tag.isEmpty()) return@flatMapLatest emptyFlow()
        Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                HashtagPagingSource { page ->
                    postRepository.getHashtagPosts(hashtag = tag, page = page, size = 10)
                }
            }
        ).flow
    }.cachedIn(viewModelScope)
}
