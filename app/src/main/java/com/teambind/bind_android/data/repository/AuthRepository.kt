package com.teambind.bind_android.data.repository

import com.teambind.bind_android.data.api.service.AuthService
import com.teambind.bind_android.data.local.TokenManager
import com.teambind.bind_android.data.model.request.LoginRequest
import com.teambind.bind_android.data.model.request.RefreshTokenRequest
import com.teambind.bind_android.data.model.response.LoginResponse
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authService: AuthService,
    private val tokenManager: TokenManager
) {

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(email, password)
            val response = authService.login(request)

            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("이메일 또는 비밀번호를 확인해주세요."))
            }
        } catch (e: HttpException) {
            val message = when (e.code()) {
                401, 403 -> "이메일 또는 비밀번호를 확인해주세요."
                404 -> "이메일 또는 비밀번호를 확인해주세요."
                500, 502, 503 -> "서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요."
                else -> "로그인에 실패했습니다. 다시 시도해주세요."
            }
            Result.failure(Exception(message))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("인터넷 연결을 확인해주세요."))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("서버 응답이 없습니다. 잠시 후 다시 시도해주세요."))
        } catch (e: Exception) {
            Result.failure(Exception("이메일 또는 비밀번호를 확인해주세요."))
        }
    }

    suspend fun refreshToken(): Result<LoginResponse> {
        return try {
            val refreshToken = tokenManager.refreshToken
            val deviceId = tokenManager.deviceId

            if (refreshToken.isNullOrEmpty() || deviceId.isNullOrEmpty()) {
                return Result.failure(Exception("로그인이 필요합니다."))
            }

            val request = RefreshTokenRequest(deviceId, refreshToken)
            val response = authService.refreshToken(request)

            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("세션이 만료되었습니다. 다시 로그인해주세요."))
            }
        } catch (e: HttpException) {
            Result.failure(Exception("세션이 만료되었습니다. 다시 로그인해주세요."))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("인터넷 연결을 확인해주세요."))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("서버 응답이 없습니다. 잠시 후 다시 시도해주세요."))
        } catch (e: Exception) {
            Result.failure(Exception("세션이 만료되었습니다. 다시 로그인해주세요."))
        }
    }

    suspend fun logout(): Result<Boolean> {
        return try {
            tokenManager.clearAllTokens()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("로그아웃에 실패했습니다."))
        }
    }
}
