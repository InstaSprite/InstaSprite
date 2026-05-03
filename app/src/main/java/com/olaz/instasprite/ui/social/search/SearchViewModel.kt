package com.olaz.instasprite.ui.social.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olaz.instasprite.data.network.model.toDomain
import com.olaz.instasprite.data.repository.SearchRepository
import com.olaz.instasprite.domain.model.MemberData
import com.olaz.instasprite.domain.model.PostData
import com.olaz.instasprite.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoadingTrending: Boolean = false,
    val isSearching: Boolean = false,
    val trendingPosts: List<PostData> = emptyList(),
    val searchResultPosts: List<PostData> = emptyList(),
    val searchResultMembers: List<MemberData> = emptyList(),
    val searchType: String? = null,
    val hasSearched: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadTrendingPosts()
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onSearch() {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSearching = true, error = null, hasSearched = true) }
            searchRepository.search(query).fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            searchType = response.type,
                            searchResultPosts = response.posts?.map { dto -> dto.toDomain() } ?: emptyList(),
                            searchResultMembers = response.members?.map { dto -> dto.toDomain() } ?: emptyList()
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSearching = false, error = e.toUserMessage(context)) }
                }
            )
        }
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(
                query = "",
                hasSearched = false,
                searchResultPosts = emptyList(),
                searchResultMembers = emptyList(),
                searchType = null,
                error = null
            )
        }
    }

    private fun loadTrendingPosts() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoadingTrending = true) }
            searchRepository.getTrendingPosts().fold(
                onSuccess = { dtos ->
                    _uiState.update {
                        it.copy(
                            isLoadingTrending = false,
                            trendingPosts = dtos.map { dto -> dto.toDomain() }
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoadingTrending = false, error = e.toUserMessage(context)) }
                }
            )
        }
    }
}
