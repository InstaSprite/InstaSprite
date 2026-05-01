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
import javax.inject.Inject

@HiltViewModel
class HashtagFeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val hashtag: String = savedStateHandle.get<String>("hashtag") ?: ""

    val pagedPosts: Flow<PagingData<PostData>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            HashtagPagingSource { page ->
                postRepository.getHashtagPosts(hashtag = hashtag, page = page, size = 10)
            }
        }
    ).flow.cachedIn(viewModelScope)
}
