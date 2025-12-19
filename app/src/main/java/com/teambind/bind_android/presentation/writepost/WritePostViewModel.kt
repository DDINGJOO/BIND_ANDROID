package com.teambind.bind_android.presentation.writepost

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.repository.CommunityRepository
import com.teambind.bind_android.presentation.community.CategoryType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WritePostUiState(
    val isLoading: Boolean = false,
    val selectedCategory: CategoryType = CategoryType.FREE,
    val title: String = "",
    val content: String = "",
    val images: List<Uri> = emptyList(),
    val isSubmitEnabled: Boolean = false,
    val isSubmitSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class WritePostViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WritePostUiState())
    val uiState: StateFlow<WritePostUiState> = _uiState.asStateFlow()

    fun selectCategory(category: CategoryType) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            isSubmitEnabled = validateInput(title, _uiState.value.content)
        )
    }

    fun setContent(content: String) {
        _uiState.value = _uiState.value.copy(
            content = content,
            isSubmitEnabled = validateInput(_uiState.value.title, content)
        )
    }

    fun addImage(uri: Uri) {
        val currentImages = _uiState.value.images.toMutableList()
        if (currentImages.size < 10) {
            currentImages.add(uri)
            _uiState.value = _uiState.value.copy(images = currentImages)
        }
    }

    fun removeImage(uri: Uri) {
        val currentImages = _uiState.value.images.toMutableList()
        currentImages.remove(uri)
        _uiState.value = _uiState.value.copy(images = currentImages)
    }

    private fun validateInput(title: String, content: String): Boolean {
        return title.isNotBlank() && content.isNotBlank()
    }

    fun submitPost() {
        val currentState = _uiState.value

        if (!currentState.isSubmitEnabled) return
        if (currentState.selectedCategory == CategoryType.ALL) {
            _uiState.value = currentState.copy(errorMessage = "게시판을 선택해주세요")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)

            val boardIds = currentState.selectedCategory.boardId ?: run {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "게시판을 선택해주세요"
                )
                return@launch
            }

            communityRepository.createArticle(
                boardIds = boardIds,
                title = currentState.title,
                content = currentState.content,
                images = currentState.images
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSubmitSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "게시글 등록에 실패했습니다"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
