package com.teambind.bind_android.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    protected fun showError(message: String) {
        viewModelScope.launch {
            _errorMessage.emit(message)
        }
    }

    protected fun showToast(message: String) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    protected fun <T> launchWithLoading(
        block: suspend () -> T,
        onSuccess: (T) -> Unit = {},
        onError: (Throwable) -> Unit = { showError(it.message ?: "Unknown error") }
    ) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val result = block()
                onSuccess(result)
            } catch (e: Exception) {
                onError(e)
            } finally {
                setLoading(false)
            }
        }
    }
}
