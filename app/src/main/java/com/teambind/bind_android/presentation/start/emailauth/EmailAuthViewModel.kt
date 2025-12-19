package com.teambind.bind_android.presentation.start.emailauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.repository.SignUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EmailAuthState {
    data object Idle : EmailAuthState()
    data object Loading : EmailAuthState()
    data object CodeSent : EmailAuthState()
    data object CodeVerified : EmailAuthState()
    data class Error(val message: String) : EmailAuthState()
}

@HiltViewModel
class EmailAuthViewModel @Inject constructor(
    private val signUpRepository: SignUpRepository
) : ViewModel() {

    private val _emailAuthState = MutableStateFlow<EmailAuthState>(EmailAuthState.Idle)
    val emailAuthState: StateFlow<EmailAuthState> = _emailAuthState.asStateFlow()

    var email: String = ""
        private set

    fun requestEmailCode(email: String) {
        this.email = email
        viewModelScope.launch {
            _emailAuthState.value = EmailAuthState.Loading

            signUpRepository.requestEmailCode(email)
                .onSuccess {
                    _emailAuthState.value = EmailAuthState.CodeSent
                }
                .onFailure { exception ->
                    _emailAuthState.value = EmailAuthState.Error(
                        exception.message ?: "인증 코드 발송에 실패했습니다."
                    )
                }
        }
    }

    fun verifyEmailCode(code: String) {
        viewModelScope.launch {
            _emailAuthState.value = EmailAuthState.Loading

            signUpRepository.verifyEmailCode(email, code)
                .onSuccess {
                    _emailAuthState.value = EmailAuthState.CodeVerified
                }
                .onFailure { exception ->
                    _emailAuthState.value = EmailAuthState.Error(
                        exception.message ?: "인증에 실패했습니다."
                    )
                }
        }
    }

    fun resetState() {
        _emailAuthState.value = EmailAuthState.Idle
    }
}
