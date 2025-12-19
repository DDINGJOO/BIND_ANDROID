package com.teambind.bind_android.presentation.myposts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.model.response.ArticleDto
import com.teambind.bind_android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MyPostsTab(val displayName: String) {
    WRITTEN("작성글"),
    COMMENTED("댓글단 글"),
    LIKED("좋아요한 글")
}

data class MyPostsUiState(
    val isLoading: Boolean = false,
    val selectedTab: MyPostsTab = MyPostsTab.WRITTEN,
    val posts: List<ArticleDto> = emptyList(),
    val hasNextPage: Boolean = false,
    val nextCursorId: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class MyPostsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPostsUiState())
    val uiState: StateFlow<MyPostsUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    fun selectTab(tab: MyPostsTab) {
        if (_uiState.value.selectedTab != tab) {
            _uiState.value = _uiState.value.copy(
                selectedTab = tab,
                posts = emptyList(),
                nextCursorId = null
            )
            loadPosts()
        }
    }

    fun loadPosts(refresh: Boolean = true) {
        val currentState = _uiState.value
        if (currentState.isLoading) return

        viewModelScope.launch {
            val cursorId = if (refresh) null else currentState.nextCursorId

            _uiState.value = currentState.copy(isLoading = true)

            val result = when (currentState.selectedTab) {
                MyPostsTab.WRITTEN -> userRepository.getMyArticles(cursorId = cursorId)
                MyPostsTab.COMMENTED -> userRepository.getMyCommentedArticles(cursorId = cursorId)
                MyPostsTab.LIKED -> userRepository.getMyLikedArticles(cursorId = cursorId)
            }

            result
                .onSuccess { response ->
                    val newPosts = response.page.articleList
                    val allPosts = if (refresh) newPosts else currentState.posts + newPosts

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        posts = allPosts,
                        hasNextPage = response.page.hasNext,
                        nextCursorId = response.page.nextCursorId
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    fun loadMorePosts() {
        val currentState = _uiState.value
        if (currentState.hasNextPage && !currentState.isLoading) {
            loadPosts(refresh = false)
        }
    }

    fun refresh() {
        loadPosts(refresh = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
