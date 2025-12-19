package com.teambind.bind_android.presentation.start.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.local.PreferencesManager
import com.teambind.bind_android.data.local.TokenManager
import com.teambind.bind_android.data.repository.AuthRepository
import com.teambind.bind_android.data.repository.ProfileRepository
import com.teambind.bind_android.util.extension.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data class Success(val hasProfile: Boolean) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val tokenManager: TokenManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")

    val isButtonEnabled: StateFlow<Boolean> = combine(_email, _password) { email, password ->
        email.isNotEmpty() && password.isNotEmpty() && email.isValidEmail() && password.length >= 8
    }.let { flow ->
        MutableStateFlow(false).also { stateFlow ->
            viewModelScope.launch {
                flow.collect { stateFlow.value = it }
            }
        }
    }

    fun updateEmail(email: String) {
        _email.value = email
    }

    fun updatePassword(password: String) {
        _password.value = password
    }

    fun login() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val email = _email.value
            val password = _password.value

            authRepository.login(email, password)
                .onSuccess { loginResponse ->
                    // 토큰 저장
                    tokenManager.saveAllTokens(
                        accessToken = loginResponse.accessToken,
                        refreshToken = loginResponse.refreshToken,
                        deviceId = loginResponse.deviceId
                    )

                    preferencesManager.setIsLoggedIn(true)

                    // 프로필 존재 여부 확인 및 userId 저장
                    val profileResult = checkProfileAndSaveUserId()
                    preferencesManager.setHasProfile(profileResult.hasProfile)

                    _loginState.value = LoginState.Success(profileResult.hasProfile)
                }
                .onFailure { error ->
                    _loginState.value = LoginState.Error(error.message ?: "로그인에 실패했습니다.")
                }
        }
    }

    private data class ProfileCheckResult(val hasProfile: Boolean, val userId: String?)

    private suspend fun checkProfileAndSaveUserId(): ProfileCheckResult {
        return profileRepository.getMyProfile()
            .map { response ->
                // userId 저장
                val userId = response.profile.userId
                preferencesManager.setUserId(userId.toLongOrNull() ?: 0L)

                // 닉네임이 있으면 프로필이 설정된 것으로 판단
                val hasProfile = response.profile.nickname.isNotEmpty()
                ProfileCheckResult(hasProfile, userId)
            }
            .getOrElse {
                // 프로필 조회 실패 시 (새 유저)
                ProfileCheckResult(false, null)
            }
    }
}
