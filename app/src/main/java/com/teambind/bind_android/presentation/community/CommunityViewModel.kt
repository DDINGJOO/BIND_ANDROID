package com.teambind.bind_android.presentation.community

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

data class CommunityUiState(
    val isLoading: Boolean = false,
    val hotPosts: List<ArticleDto> = emptyList(),
    val allPosts: List<ArticleDto> = emptyList(),
    val selectedCategory: CategoryType = CategoryType.ALL,
    val sortBy: SortType = SortType.LATEST,
    val errorMessage: String? = null,
    // 페이징
    val hasNextPage: Boolean = false,
    val nextCursorId: String? = null,
    val nextCursorUpdatedAt: String? = null
)

enum class CategoryType(val boardId: Long?, val displayName: String, val sectionTitle: String) {
    ALL(null, "전체", "전체 커뮤니티 💬"),
    TIP(4L, "정보공유", "정보공유 💡"),
    QUESTION(3L, "질문", "질문 ❓"),
    FREE(2L, "자유", "자유 🎸")
}

enum class SortType(val value: String, val displayName: String) {
    LATEST("latest", "최신순"),
    POPULAR("popular", "인기순")
}

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        loadHotPosts()
        loadAllPosts()
    }

    fun loadHotPosts() {
        viewModelScope.launch {
            communityRepository.getHotArticles(size = 10)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        hotPosts = response.page.articleList
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message
                    )
                }
        }
    }

    fun loadAllPosts(refresh: Boolean = true) {
        viewModelScope.launch {
            val currentState = _uiState.value

            // 새로고침이면 커서 초기화
            val cursorId = if (refresh) null else currentState.nextCursorId
            val cursorUpdatedAt = if (refresh) null else currentState.nextCursorUpdatedAt

            _uiState.value = currentState.copy(isLoading = true)

            communityRepository.getArticleList(
                boardIds = currentState.selectedCategory.boardId,
                size = 20,
                cursorId = cursorId,
                cursorUpdatedAt = cursorUpdatedAt,
                sortBy = currentState.sortBy.value
            )
                .onSuccess { response ->
                    val newPosts = response.page.articleList
                    val allPosts = if (refresh) {
                        newPosts
                    } else {
                        currentState.allPosts + newPosts
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allPosts = allPosts,
                        hasNextPage = response.page.hasNext,
                        nextCursorId = response.page.nextCursorId,
                        nextCursorUpdatedAt = response.page.nextCursorUpdatedAt
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

    fun selectCategory(category: CategoryType) {
        if (_uiState.value.selectedCategory != category) {
            _uiState.value = _uiState.value.copy(
                selectedCategory = category,
                allPosts = emptyList()
            )
            loadAllPosts(refresh = true)
        }
    }

    fun changeSortType(sortType: SortType) {
        if (_uiState.value.sortBy != sortType) {
            _uiState.value = _uiState.value.copy(
                sortBy = sortType,
                allPosts = emptyList()
            )
            loadAllPosts(refresh = true)
        }
    }

    fun loadMorePosts() {
        val currentState = _uiState.value
        if (currentState.hasNextPage && !currentState.isLoading) {
            loadAllPosts(refresh = false)
        }
    }

    fun refresh() {
        loadHotPosts()
        loadAllPosts(refresh = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
