package com.teambind.bind_android.presentation.inquirydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.model.response.InquiryDetailResponse
import com.teambind.bind_android.data.repository.InquiryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InquiryDetailUiState(
    val isLoading: Boolean = false,
    val inquiry: InquiryDetailResponse? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class InquiryDetailViewModel @Inject constructor(
    private val inquiryRepository: InquiryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InquiryDetailUiState())
    val uiState: StateFlow<InquiryDetailUiState> = _uiState.asStateFlow()

    fun loadInquiryDetail(inquiryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            inquiryRepository.getInquiryDetail(inquiryId)
                .onSuccess { inquiry ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        inquiry = inquiry
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
