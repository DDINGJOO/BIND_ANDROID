package com.teambind.bind_android.presentation.customerservice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.repository.InquiryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WriteInquiryState {
    object Idle : WriteInquiryState()
    object Loading : WriteInquiryState()
    object Success : WriteInquiryState()
    data class Error(val message: String) : WriteInquiryState()
}

@HiltViewModel
class WriteInquiryViewModel @Inject constructor(
    private val inquiryRepository: InquiryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<WriteInquiryState>(WriteInquiryState.Idle)
    val state: StateFlow<WriteInquiryState> = _state.asStateFlow()

    private val _title = MutableStateFlow("")
    private val _content = MutableStateFlow("")

    val isButtonEnabled = _title.combine(_content) { title, content ->
        title.isNotBlank() && content.isNotBlank()
    }

    fun updateTitle(title: String) {
        _title.value = title
    }

    fun updateContent(content: String) {
        _content.value = content
    }

    fun submitInquiry(category: String) {
        val title = _title.value
        val content = _content.value

        if (title.isBlank() || content.isBlank()) {
            _state.value = WriteInquiryState.Error("제목과 내용을 입력해주세요.")
            return
        }

        viewModelScope.launch {
            _state.value = WriteInquiryState.Loading

            inquiryRepository.writeInquiry(category, title, content)
                .onSuccess {
                    _state.value = WriteInquiryState.Success
                }
                .onFailure { error ->
                    _state.value = WriteInquiryState.Error(
                        error.message ?: "문의 접수에 실패했습니다."
                    )
                }
        }
    }
}
