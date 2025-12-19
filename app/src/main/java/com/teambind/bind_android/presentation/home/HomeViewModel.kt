package com.teambind.bind_android.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.model.response.ArticleDto
import com.teambind.bind_android.data.model.response.StudioDto
import com.teambind.bind_android.data.repository.CommunityRepository
import com.teambind.bind_android.data.repository.StudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeFilter(
    val province: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val headCount: Int? = null,
    val keywordIds: String? = null
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val hotPosts: List<ArticleDto> = emptyList(),
    val studios: List<StudioDto> = emptyList(),
    val filter: HomeFilter = HomeFilter(),
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val studioRepository: StudioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 인기 게시글 로드
            loadHotPosts()

            // 스튜디오 목록 로드
            loadStudios()

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private suspend fun loadHotPosts() {
        try {
            communityRepository.getHotArticles(size = 10)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        hotPosts = response.page?.articleList ?: emptyList()
                    )
                }
                .onFailure { /* Ignore - optional data */ }
        } catch (e: Exception) {
            // Ignore - optional data
        }
    }

    private suspend fun loadStudios() {
        val filter = _uiState.value.filter
        try {
            studioRepository.getStudioList(
                province = filter.province,
                date = filter.date,
                startTime = filter.startTime,
                endTime = filter.endTime,
                headCount = filter.headCount,
                keywordIds = filter.keywordIds,
                size = 20
            )
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        studios = response.studioList ?: emptyList()
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "스튜디오 목록을 불러오는데 실패했습니다."
                    )
                }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "인터넷 연결을 확인해주세요."
            )
        }
    }

    fun updateRegionFilter(province: String?) {
        _uiState.value = _uiState.value.copy(
            filter = _uiState.value.filter.copy(province = province)
        )
        viewModelScope.launch {
            loadStudios()
        }
    }

    fun updateDateTimeFilter(date: String?, startTime: String?, endTime: String?) {
        _uiState.value = _uiState.value.copy(
            filter = _uiState.value.filter.copy(
                date = date,
                startTime = startTime,
                endTime = endTime
            )
        )
        viewModelScope.launch {
            loadStudios()
        }
    }

    fun updateHeadCountFilter(headCount: Int?) {
        _uiState.value = _uiState.value.copy(
            filter = _uiState.value.filter.copy(headCount = headCount)
        )
        viewModelScope.launch {
            loadStudios()
        }
    }

    fun updateKeywordFilter(keywordIds: String?) {
        _uiState.value = _uiState.value.copy(
            filter = _uiState.value.filter.copy(keywordIds = keywordIds)
        )
        viewModelScope.launch {
            loadStudios()
        }
    }

    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(filter = HomeFilter())
        viewModelScope.launch {
            loadStudios()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun refresh() {
        loadHomeData()
    }
}
