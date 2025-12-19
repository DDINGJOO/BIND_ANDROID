package com.teambind.bind_android.presentation.noticeevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.model.response.ArticleDto
import com.teambind.bind_android.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NoticeEventTab(val displayName: String) {
    NOTICE("공지사항"),
    EVENT("이벤트")
}

data class NoticeEventUiState(
    val isLoading: Boolean = false,
    val selectedTab: NoticeEventTab = NoticeEventTab.NOTICE,
    val posts: List<ArticleDto> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class NoticeEventViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoticeEventUiState())
    val uiState: StateFlow<NoticeEventUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    fun selectTab(tab: NoticeEventTab) {
        if (_uiState.value.selectedTab != tab) {
            _uiState.value = _uiState.value.copy(
                selectedTab = tab,
                posts = emptyList()
            )
            loadPosts()
        }
    }

    fun loadPosts() {
        val currentState = _uiState.value
        if (currentState.isLoading) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)

            val result = when (currentState.selectedTab) {
                NoticeEventTab.NOTICE -> homeRepository.getNotices(size = 50)
                NoticeEventTab.EVENT -> homeRepository.getEvents(size = 50)
            }

            result
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        posts = response.content ?: emptyList()
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

    fun refresh() {
        loadPosts()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
