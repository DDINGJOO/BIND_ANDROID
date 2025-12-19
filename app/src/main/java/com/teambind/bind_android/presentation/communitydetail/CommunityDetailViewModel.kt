package com.teambind.bind_android.presentation.communitydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.local.PreferencesManager
import com.teambind.bind_android.data.model.request.ReferenceType
import com.teambind.bind_android.data.model.response.ArticleDetailDto
import com.teambind.bind_android.data.model.response.CommentDto
import com.teambind.bind_android.data.repository.CommunityRepository
import com.teambind.bind_android.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityDetailUiState(
    val isLoading: Boolean = false,
    val article: ArticleDetailDto? = null,
    val comments: List<CommentDto> = emptyList(),
    val isLiked: Boolean = false,
    val likeCount: Int = 0,
    val errorMessage: String? = null,
    val isCommentSending: Boolean = false,
    val commentSent: Boolean = false,
    val isReportSending: Boolean = false,
    val reportSuccess: Boolean = false
)

@HiltViewModel
class CommunityDetailViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val reportRepository: ReportRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityDetailUiState())
    val uiState: StateFlow<CommunityDetailUiState> = _uiState.asStateFlow()

    private var articleId: String = ""

    fun loadArticleDetail(articleId: String) {
        this.articleId = articleId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            communityRepository.getArticleDetail(articleId)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            article = response.article,
                            comments = response.comments,
                            isLiked = response.isLiked,
                            likeCount = response.likeDetail?.likeCount ?: 0
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "게시글을 불러오는데 실패했습니다."
                        )
                    }
                }
        }
    }

    fun toggleLike() {
        val currentLiked = _uiState.value.isLiked
        viewModelScope.launch {
            val result = if (currentLiked) {
                communityRepository.unlikeArticle(articleId)
            } else {
                communityRepository.likeArticle(articleId)
            }

            result.onSuccess {
                // 게시글 상세 정보 다시 로드 (좋아요 상태, 댓글 포함)
                loadArticleDetail(articleId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "좋아요에 실패했습니다.")
                }
            }
        }
    }

    fun sendComment(content: String, parentId: String? = null) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCommentSending = true) }

            communityRepository.createComment(articleId, content, parentId)
                .onSuccess {
                    _uiState.update { it.copy(isCommentSending = false, commentSent = true) }
                    // 댓글 목록 새로고침
                    loadArticleDetail(articleId)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isCommentSending = false,
                            errorMessage = error.message ?: "댓글 작성에 실패했습니다."
                        )
                    }
                }
        }
    }

    fun deleteArticle() {
        viewModelScope.launch {
            communityRepository.deleteArticle(articleId)
                .onSuccess {
                    _uiState.update { it.copy(errorMessage = "게시글이 삭제되었습니다.") }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "게시글 삭제에 실패했습니다.")
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearCommentSent() {
        _uiState.update { it.copy(commentSent = false) }
    }

    fun reportArticle(category: String, reason: String) {
        val article = _uiState.value.article ?: return
        val reporterId = preferencesManager.userId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isReportSending = true) }

            reportRepository.createReport(
                reporterId = reporterId,
                reportedId = article.articleId,
                referenceType = ReferenceType.ARTICLE,
                reportCategory = category,
                reason = reason
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isReportSending = false,
                        reportSuccess = true,
                        errorMessage = "신고가 접수되었습니다."
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isReportSending = false,
                        errorMessage = error.message ?: "신고 접수에 실패했습니다."
                    )
                }
            }
        }
    }

    fun clearReportSuccess() {
        _uiState.update { it.copy(reportSuccess = false) }
    }
}
