package com.teambind.bind_android.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.repository.AuthRepository
import com.teambind.bind_android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isPushEnabled: Boolean = true,
    val isMarketingEnabled: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _logoutState = MutableStateFlow(false)
    val logoutState: StateFlow<Boolean> = _logoutState.asStateFlow()

    private val _deleteAccountState = MutableStateFlow(false)
    val deleteAccountState: StateFlow<Boolean> = _deleteAccountState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Load notification settings from local storage or server
            // For now, using default values
        }
    }

    fun setPushNotification(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isPushEnabled = enabled)
        // Save to server or local storage
    }

    fun setMarketingNotification(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isMarketingEnabled = enabled)
        // Save to server or local storage
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
                .onSuccess {
                    _logoutState.value = true
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "로그아웃에 실패했습니다"
                    )
                }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            userRepository.deleteAccount()
                .onSuccess {
                    authRepository.logout()
                    _deleteAccountState.value = true
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "회원 탈퇴에 실패했습니다"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
