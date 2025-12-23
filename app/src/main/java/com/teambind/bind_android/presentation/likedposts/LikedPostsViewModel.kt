package com.teambind.bind_android.presentation.likedposts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.model.response.ArticleDto
import com.teambind.bind_android.data.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LikedPostsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val posts: List<ArticleDto> = emptyList(),
    val hasMore: Boolean = true,
    val nextCursor: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class LikedPostsViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LikedPostsUiState())
    val uiState: StateFlow<LikedPostsUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            communityRepository.getMyLikedArticles()
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        posts = response.page.articleList,
                        hasMore = response.page.hasNext,
                        nextCursor = response.page.nextCursorId
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
        }
    }

    fun loadMore() {
        val cursor = _uiState.value.nextCursor ?: return
        if (!_uiState.value.hasMore) return

        viewModelScope.launch {
            communityRepository.getMyLikedArticles(cursor = cursor)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        posts = _uiState.value.posts + response.page.articleList,
                        hasMore = response.page.hasNext,
                        nextCursor = response.page.nextCursorId
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = e.message
                    )
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            communityRepository.getMyLikedArticles()
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        posts = response.page.articleList,
                        hasMore = response.page.hasNext,
                        nextCursor = response.page.nextCursorId
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        errorMessage = e.message
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
