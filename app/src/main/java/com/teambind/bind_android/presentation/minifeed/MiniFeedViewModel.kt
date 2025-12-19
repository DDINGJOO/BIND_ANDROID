package com.teambind.bind_android.presentation.minifeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.model.Genre
import com.teambind.bind_android.data.model.Instrument
import com.teambind.bind_android.data.model.response.FeedArticleDto
import com.teambind.bind_android.data.model.response.ProfileDto
import com.teambind.bind_android.data.repository.FeedCategory
import com.teambind.bind_android.data.repository.FeedRepository
import com.teambind.bind_android.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MiniFeedTab(val index: Int, val displayName: String) {
    POSTS(0, "작성한 글"),
    COMMENTS(1, "댓글단 글"),
    LIKES(2, "좋아요한 글")
}

data class MiniFeedUiState(
    val isLoading: Boolean = false,
    val profile: ProfileDto? = null,
    val categories: List<String> = emptyList(),
    val selectedTab: MiniFeedTab = MiniFeedTab.POSTS,
    val posts: List<FeedArticleDto> = emptyList(),
    val errorMessage: String? = null,
    // 페이징
    val hasNextPage: Boolean = false,
    val nextCursor: String? = null,
    val isLoadingMore: Boolean = false
)

@HiltViewModel
class MiniFeedViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiniFeedUiState())
    val uiState: StateFlow<MiniFeedUiState> = _uiState.asStateFlow()

    private var userId: String = ""

    fun init(userId: String) {
        this.userId = userId
        loadProfile()
        loadPosts(refresh = true)
    }

    private fun loadProfile() {
        viewModelScope.launch {
            profileRepository.getMyProfile()
                .onSuccess { response ->
                    val profile = response.profile
                    // 장르와 악기를 카테고리 태그로 변환
                    val genreNames = profile.genres.mapNotNull { serverKey ->
                        Genre.fromServerKey(serverKey)?.displayName
                    }
                    val instrumentNames = profile.instruments.mapNotNull { serverKey ->
                        Instrument.fromServerKey(serverKey)?.displayName
                    }
                    val categories = (genreNames + instrumentNames)
                        .take(4)
                        .map { "#$it" }

                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        categories = categories
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message
                    )
                }
        }
    }

    fun selectTab(tab: MiniFeedTab) {
        if (_uiState.value.selectedTab != tab) {
            _uiState.value = _uiState.value.copy(
                selectedTab = tab,
                posts = emptyList(),
                nextCursor = null,
                hasNextPage = false
            )
            loadPosts(refresh = true)
        }
    }

    fun loadPosts(refresh: Boolean = true) {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (!refresh && currentState.isLoadingMore) return@launch
            if (!refresh && !currentState.hasNextPage) return@launch

            val cursor = if (refresh) null else currentState.nextCursor

            _uiState.value = currentState.copy(
                isLoading = refresh && currentState.posts.isEmpty(),
                isLoadingMore = !refresh
            )

            val category = when (currentState.selectedTab) {
                MiniFeedTab.POSTS -> FeedCategory.ARTICLE
                MiniFeedTab.COMMENTS -> FeedCategory.COMMENT
                MiniFeedTab.LIKES -> FeedCategory.LIKE
            }

            feedRepository.getFeed(
                category = category,
                targetUserId = userId,
                size = 20,
                cursor = cursor
            )
                .onSuccess { response ->
                    val newPosts = response.articles
                    val allPosts = if (refresh) {
                        newPosts
                    } else {
                        currentState.posts + newPosts
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        posts = allPosts,
                        hasNextPage = !response.nextCursor.isNullOrEmpty(),
                        nextCursor = response.nextCursor
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    fun loadMorePosts() {
        val currentState = _uiState.value
        if (currentState.hasNextPage && !currentState.isLoadingMore) {
            loadPosts(refresh = false)
        }
    }

    fun refreshProfile() {
        loadProfile()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
