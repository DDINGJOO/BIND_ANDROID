package com.teambind.bind_android.presentation.start.findpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.repository.SignUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ResetPasswordState {
    object Idle : ResetPasswordState()
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}

data class PasswordValidationState(
    val hasUppercase: Boolean = false,
    val hasLowercase: Boolean = false,
    val hasNumber: Boolean = false,
    val hasSpecialCharacter: Boolean = false,
    val hasValidLength: Boolean = false,
    val passwordsMatch: Boolean = false
) {
    val isPasswordValid: Boolean
        get() = hasUppercase && hasLowercase && hasNumber && hasSpecialCharacter && hasValidLength

    val isAllValid: Boolean
        get() = isPasswordValid && passwordsMatch
}

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val signUpRepository: SignUpRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val state: StateFlow<ResetPasswordState> = _state.asStateFlow()

    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _passwordConfirm = MutableStateFlow("")

    private val _validationState = MutableStateFlow(PasswordValidationState())
    val validationState: StateFlow<PasswordValidationState> = _validationState.asStateFlow()

    val isButtonEnabled = combine(_email, _validationState) { email, validation ->
        isValidEmail(email) && validation.isAllValid
    }

    fun updateEmail(email: String) {
        _email.value = email
    }

    fun updatePassword(password: String) {
        _password.value = password
        updateValidation()
    }

    fun updatePasswordConfirm(confirm: String) {
        _passwordConfirm.value = confirm
        updateValidation()
    }

    private fun updateValidation() {
        val password = _password.value
        val confirm = _passwordConfirm.value

        _validationState.value = PasswordValidationState(
            hasUppercase = password.any { it.isUpperCase() },
            hasLowercase = password.any { it.isLowerCase() },
            hasNumber = password.any { it.isDigit() },
            hasSpecialCharacter = password.any { !it.isLetterOrDigit() },
            hasValidLength = password.length >= 10,
            passwordsMatch = password.isNotEmpty() && password == confirm
        )
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    fun resetPassword() {
        val email = _email.value
        val password = _password.value
        val passwordConfirm = _passwordConfirm.value

        if (!_validationState.value.isAllValid) {
            _state.value = ResetPasswordState.Error("비밀번호 형식을 확인해주세요.")
            return
        }

        viewModelScope.launch {
            _state.value = ResetPasswordState.Loading

            signUpRepository.changePassword(email, password, passwordConfirm)
                .onSuccess {
                    _state.value = ResetPasswordState.Success
                }
                .onFailure { error ->
                    _state.value = ResetPasswordState.Error(
                        error.message ?: "비밀번호 변경에 실패했습니다."
                    )
                }
        }
    }
}
