package com.teambind.bind_android.presentation.customerservice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.api.service.EnumsService
import com.teambind.bind_android.data.model.response.FaqDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerServiceUiState(
    val isLoading: Boolean = false,
    val allFaqs: List<FaqDto> = emptyList(),
    val filteredFaqs: List<FaqDto> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "전체",
    val errorMessage: String? = null
)

@HiltViewModel
class CustomerServiceViewModel @Inject constructor(
    private val enumsService: EnumsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerServiceUiState())
    val uiState: StateFlow<CustomerServiceUiState> = _uiState.asStateFlow()

    init {
        loadFaqs()
    }

    private fun loadFaqs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = enumsService.getFaqs()
                if (response.isSuccess && response.result != null) {
                    val faqs = response.result
                    // "전체"를 제외한 카테고리만 추출하여 중복 방지
                    val otherCategories = faqs.map { it.getCategoryDisplayName() }
                        .distinct()
                        .filter { it != "전체" }
                    val categories = listOf("전체") + otherCategories

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allFaqs = faqs,
                        filteredFaqs = faqs,
                        categories = categories
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "FAQ를 불러오는데 실패했습니다."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "FAQ를 불러오는데 실패했습니다."
                )
            }
        }
    }

    fun selectCategory(category: String) {
        val currentState = _uiState.value
        val filteredFaqs = if (category == "전체") {
            currentState.allFaqs
        } else {
            currentState.allFaqs.filter { it.getCategoryDisplayName() == category }
        }

        _uiState.value = currentState.copy(
            selectedCategory = category,
            filteredFaqs = filteredFaqs
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
