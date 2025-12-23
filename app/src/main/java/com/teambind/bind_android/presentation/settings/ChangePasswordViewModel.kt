package com.teambind.bind_android.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val state: StateFlow<ChangePasswordState> = _state.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    private val _confirmPassword = MutableStateFlow("")

    val isButtonEnabled = _newPassword.combine(_confirmPassword) { newPw, confirmPw ->
        newPw.length >= 10 && newPw == confirmPw
    }

    fun updateNewPassword(password: String) {
        _newPassword.value = password
    }

    fun updateConfirmPassword(password: String) {
        _confirmPassword.value = password
    }

    fun changePassword() {
        val newPassword = _newPassword.value
        val confirmPassword = _confirmPassword.value

        if (newPassword != confirmPassword) {
            _state.value = ChangePasswordState.Error("비밀번호가 일치하지 않습니다.")
            return
        }

        if (newPassword.length < 10) {
            _state.value = ChangePasswordState.Error("비밀번호는 10자 이상이어야 합니다.")
            return
        }

        viewModelScope.launch {
            _state.value = ChangePasswordState.Loading

            profileRepository.changePassword(newPassword, confirmPassword)
                .onSuccess {
                    _state.value = ChangePasswordState.Success
                }
                .onFailure { error ->
                    _state.value = ChangePasswordState.Error(
                        error.message ?: "비밀번호 변경에 실패했습니다."
                    )
                }
        }
    }
}
