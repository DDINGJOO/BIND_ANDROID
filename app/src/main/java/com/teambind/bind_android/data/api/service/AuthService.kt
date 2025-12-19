package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.request.LoginRequest
import com.teambind.bind_android.data.model.request.RefreshTokenRequest
import com.teambind.bind_android.data.model.response.BaseResponse
import com.teambind.bind_android.data.model.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    // 로그인 (서버: POST /bff/v1/auth/login)
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): BaseResponse<LoginResponse>

    // 토큰 갱신 (서버: POST /bff/v1/auth/refreshToken)
    @POST("auth/refreshToken")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): BaseResponse<LoginResponse>
}
