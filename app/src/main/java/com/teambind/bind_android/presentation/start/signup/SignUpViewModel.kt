package com.teambind.bind_android.presentation.start.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PasswordValidation(
    val hasUppercase: Boolean = false,
    val hasLowercase: Boolean = false,
    val hasNumber: Boolean = false,
    val hasSpecialChar: Boolean = false,
    val hasMinLength: Boolean = false,
    val passwordsMatch: Boolean = false
) {
    val isPasswordValid: Boolean
        get() = hasUppercase && hasLowercase && hasNumber && hasSpecialChar && hasMinLength

    val isValid: Boolean
        get() = isPasswordValid && passwordsMatch
}

@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {

    var email: String = ""
        private set

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _passwordConfirm = MutableStateFlow("")
    val passwordConfirm: StateFlow<String> = _passwordConfirm.asStateFlow()

    private val _passwordValidation = MutableStateFlow(PasswordValidation())
    val passwordValidation: StateFlow<PasswordValidation> = _passwordValidation.asStateFlow()

    val isButtonEnabled: StateFlow<Boolean> = MutableStateFlow(false).also { stateFlow ->
        viewModelScope.launch {
            combine(_password, _passwordConfirm) { password, passwordConfirm ->
                validatePassword(password, passwordConfirm).isValid
            }.collect { stateFlow.value = it }
        }
    }

    fun setEmail(email: String) {
        this.email = email
    }

    fun updatePassword(password: String) {
        _password.value = password
        _passwordValidation.value = validatePassword(password, _passwordConfirm.value)
    }

    fun updatePasswordConfirm(passwordConfirm: String) {
        _passwordConfirm.value = passwordConfirm
        _passwordValidation.value = validatePassword(_password.value, passwordConfirm)
    }

    private fun validatePassword(password: String, passwordConfirm: String): PasswordValidation {
        // Check uppercase
        val hasUppercase = password.any { it.isUpperCase() }

        // Check lowercase
        val hasLowercase = password.any { it.isLowerCase() }

        // Check number
        val hasNumber = password.any { it.isDigit() }

        // Check special character
        val specialCharRegex = Regex("[^a-zA-Z0-9]")
        val hasSpecialChar = specialCharRegex.containsMatchIn(password)

        // Check minimum length (10 characters)
        val hasMinLength = password.length >= 10

        // Check passwords match
        val passwordsMatch = password.isNotEmpty() && password == passwordConfirm

        return PasswordValidation(
            hasUppercase = hasUppercase,
            hasLowercase = hasLowercase,
            hasNumber = hasNumber,
            hasSpecialChar = hasSpecialChar,
            hasMinLength = hasMinLength,
            passwordsMatch = passwordsMatch
        )
    }
}
