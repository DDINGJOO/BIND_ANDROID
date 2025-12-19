package com.teambind.bind_android.presentation.start.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.local.PreferencesManager
import com.teambind.bind_android.data.local.TokenManager
import com.teambind.bind_android.data.repository.AuthRepository
import com.teambind.bind_android.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthMainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val tokenManager: TokenManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val hasProfile: Boolean) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            authRepository.login(email, password)
                .onSuccess { response ->
                    // 토큰 저장
                    tokenManager.saveAllTokens(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
                        deviceId = response.deviceId
                    )

                    // 프로필 조회하여 userId 저장
                    fetchProfileAndUpdateState()
                }
                .onFailure { exception ->
                    _loginState.value = LoginState.Error(
                        exception.message ?: "로그인에 실패했습니다."
                    )
                }
        }
    }

    private suspend fun fetchProfileAndUpdateState() {
        profileRepository.getMyProfile()
            .onSuccess { profileResponse ->
                // userId 및 프로필 정보 저장
                val userId = profileResponse.profile.userId.toLongOrNull() ?: 0L
                preferencesManager.saveUserInfo(
                    userId = userId,
                    userName = profileResponse.profile.nickname,
                    profileImageUrl = profileResponse.profile.profileImageUrl
                )
                preferencesManager.setHasProfile(true)

                _loginState.value = LoginState.Success(hasProfile = true)
            }
            .onFailure {
                // 프로필이 없는 경우 (신규 가입자)
                preferencesManager.setIsLoggedIn(true)
                preferencesManager.setHasProfile(false)

                _loginState.value = LoginState.Success(hasProfile = false)
            }
    }

    fun kakaoLogin() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            // TODO: 카카오 로그인 구현
            _loginState.value = LoginState.Error("카카오 로그인은 준비 중입니다.")
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
