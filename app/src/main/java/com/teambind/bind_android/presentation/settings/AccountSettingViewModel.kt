package com.teambind.bind_android.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.local.PreferencesManager
import com.teambind.bind_android.data.local.TokenManager
import com.teambind.bind_android.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AccountSettingState {
    object Idle : AccountSettingState()
    object Loading : AccountSettingState()
    object WithdrawSuccess : AccountSettingState()
    data class Error(val message: String) : AccountSettingState()
}

@HiltViewModel
class AccountSettingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val tokenManager: TokenManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow<AccountSettingState>(AccountSettingState.Idle)
    val state: StateFlow<AccountSettingState> = _state.asStateFlow()

    fun withdraw() {
        viewModelScope.launch {
            _state.value = AccountSettingState.Loading

            profileRepository.withdraw()
                .onSuccess {
                    // 로컬 데이터 삭제
                    tokenManager.clearAllTokens()
                    preferencesManager.clearAll()
                    _state.value = AccountSettingState.WithdrawSuccess
                }
                .onFailure { error ->
                    _state.value = AccountSettingState.Error(
                        error.message ?: "회원 탈퇴에 실패했습니다."
                    )
                }
        }
    }
}
