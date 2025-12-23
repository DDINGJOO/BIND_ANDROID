package com.teambind.bind_android.presentation.start.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class IDVerifyState {
    object Idle : IDVerifyState()
    object Loading : IDVerifyState()
    object CodeSent : IDVerifyState()
    object Success : IDVerifyState()
    data class Error(val message: String) : IDVerifyState()
}

@HiltViewModel
class IDVerifyViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TIMER_DURATION = 300 // 5분 = 300초
    }

    private val _state = MutableStateFlow<IDVerifyState>(IDVerifyState.Idle)
    val state: StateFlow<IDVerifyState> = _state.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _verificationCode = MutableStateFlow("")
    val verificationCode: StateFlow<String> = _verificationCode.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(TIMER_DURATION)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private var timerJob: Job? = null

    // 인증 요청 버튼 활성화 여부
    val isRequestButtonEnabled = _phoneNumber.combine(_isTimerRunning) { phone, timerRunning ->
        phone.length >= 10 && !timerRunning
    }

    // 인증하기 버튼 활성화 여부
    val isVerifyButtonEnabled = _verificationCode.combine(_isTimerRunning) { code, timerRunning ->
        code.length == 6 && timerRunning
    }

    fun updatePhoneNumber(phone: String) {
        // 숫자만 허용
        val sanitized = phone.filter { it.isDigit() }
        _phoneNumber.value = sanitized
    }

    fun updateVerificationCode(code: String) {
        // 숫자만 허용, 최대 6자리
        val sanitized = code.filter { it.isDigit() }.take(6)
        _verificationCode.value = sanitized
    }

    fun requestVerificationCode() {
        val phone = _phoneNumber.value
        if (phone.length < 10) {
            _state.value = IDVerifyState.Error("올바른 휴대폰 번호를 입력해주세요.")
            return
        }

        viewModelScope.launch {
            _state.value = IDVerifyState.Loading

            authRepository.requestSmsCode(phone)
                .onSuccess {
                    _state.value = IDVerifyState.CodeSent
                    startTimer()
                }
                .onFailure { error ->
                    _state.value = IDVerifyState.Error(error.message ?: "인증번호 발송에 실패했습니다.")
                }
        }
    }

    fun resendVerificationCode() {
        val phone = _phoneNumber.value
        if (phone.length < 10) {
            _state.value = IDVerifyState.Error("올바른 휴대폰 번호를 입력해주세요.")
            return
        }

        viewModelScope.launch {
            _state.value = IDVerifyState.Loading

            authRepository.resendSmsCode(phone)
                .onSuccess {
                    _state.value = IDVerifyState.CodeSent
                    resetTimer()
                }
                .onFailure { error ->
                    _state.value = IDVerifyState.Error(error.message ?: "인증번호 재발송에 실패했습니다.")
                }
        }
    }

    fun verifyCode() {
        val phone = _phoneNumber.value
        val code = _verificationCode.value

        if (code.length != 6) {
            _state.value = IDVerifyState.Error("인증번호 6자리를 입력해주세요.")
            return
        }

        viewModelScope.launch {
            _state.value = IDVerifyState.Loading

            authRepository.verifySmsCode(phone, code)
                .onSuccess {
                    stopTimer()
                    _state.value = IDVerifyState.Success
                }
                .onFailure { error ->
                    _state.value = IDVerifyState.Error(error.message ?: "인증번호가 올바르지 않습니다.")
                }
        }
    }

    private fun startTimer() {
        stopTimer()
        _remainingSeconds.value = TIMER_DURATION
        _isTimerRunning.value = true

        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value = _remainingSeconds.value - 1
            }
            _isTimerRunning.value = false
            _state.value = IDVerifyState.Error("인증 시간이 만료되었습니다. 다시 시도해주세요.")
        }
    }

    private fun resetTimer() {
        stopTimer()
        startTimer()
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _isTimerRunning.value = false
    }

    fun clearError() {
        if (_state.value is IDVerifyState.Error) {
            _state.value = if (_isTimerRunning.value) IDVerifyState.CodeSent else IDVerifyState.Idle
        }
    }

    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
