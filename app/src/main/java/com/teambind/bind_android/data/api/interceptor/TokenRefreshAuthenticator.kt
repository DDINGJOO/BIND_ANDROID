package com.teambind.bind_android.data.api.interceptor

import android.content.Intent
import com.teambind.bind_android.application.BindApplication
import com.teambind.bind_android.data.api.service.AuthService
import com.teambind.bind_android.data.local.PreferencesManager
import com.teambind.bind_android.data.local.TokenManager
import com.teambind.bind_android.data.model.request.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val preferencesManager: PreferencesManager,
    private val authServiceProvider: Provider<AuthService>
) : Authenticator {

    companion object {
        private const val MAX_RETRY_COUNT = 2
        private const val REFRESH_TOKEN_PATH = "refreshToken"
        const val ACTION_UNAUTHORIZED_LOGOUT = "com.teambind.bind_android.UNAUTHORIZED_LOGOUT"
    }

    @Volatile
    private var isRefreshing = false
    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // refreshToken API 자체는 재시도하지 않음 (무한 루프 방지)
        if (response.request.url.toString().contains(REFRESH_TOKEN_PATH)) {
            return null
        }

        // 재시도 횟수 체크
        val retryCount = response.request.header("Retry-Count")?.toIntOrNull() ?: 0
        if (retryCount >= MAX_RETRY_COUNT) {
            handleLogout()
            return null
        }

        synchronized(lock) {
            // 이미 토큰이 갱신되었는지 확인
            val currentToken = tokenManager.accessToken
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // 다른 스레드에서 이미 토큰이 갱신된 경우
            if (currentToken != null && currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .header("Retry-Count", (retryCount + 1).toString())
                    .build()
            }

            // 토큰 갱신 시도
            if (!isRefreshing) {
                isRefreshing = true
                try {
                    val newToken = refreshToken()
                    if (newToken != null) {
                        return response.request.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .header("Retry-Count", (retryCount + 1).toString())
                            .build()
                    } else {
                        handleLogout()
                        return null
                    }
                } finally {
                    isRefreshing = false
                }
            }
        }

        return null
    }

    private fun refreshToken(): String? {
        val refreshToken = tokenManager.refreshToken ?: return null
        val deviceId = tokenManager.deviceId ?: return null

        return try {
            val authService = authServiceProvider.get()
            val response = runBlocking {
                authService.refreshToken(RefreshTokenRequest(deviceId, refreshToken))
            }

            if (response.isSuccess && response.result != null) {
                val tokens = response.result
                tokenManager.saveAllTokens(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    deviceId = tokens.deviceId
                )
                tokens.accessToken
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun handleLogout() {
        runBlocking {
            tokenManager.clearAllTokens()
            preferencesManager.clearAll()
        }

        // 로그아웃 브로드캐스트 발송
        val intent = Intent(ACTION_UNAUTHORIZED_LOGOUT)
        BindApplication.instance.sendBroadcast(intent)
    }
}
