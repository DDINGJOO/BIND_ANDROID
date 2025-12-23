package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.request.LoginRequest
import com.teambind.bind_android.data.model.request.RefreshTokenRequest
import com.teambind.bind_android.data.model.request.SmsCodeRequest
import com.teambind.bind_android.data.model.request.SmsVerifyRequest
import com.teambind.bind_android.data.model.response.BaseResponse
import com.teambind.bind_android.data.model.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
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

    // 휴대폰 등록 여부 확인 (서버: GET /bff/v1/auth/phone-number)
    @GET("auth/phone-number")
    suspend fun checkPhoneValid(): BaseResponse<Boolean>

    // SMS 인증 코드 요청 (서버: POST /bff/v1/auth/sms/request)
    @POST("auth/sms/request")
    suspend fun requestSmsCode(
        @Body request: SmsCodeRequest
    ): BaseResponse<String>

    // SMS 인증 코드 확인 (서버: POST /bff/v1/auth/sms/verify)
    @POST("auth/sms/verify")
    suspend fun verifySmsCode(
        @Body request: SmsVerifyRequest
    ): BaseResponse<Boolean>

    // SMS 인증 코드 재발송 (서버: POST /bff/v1/auth/sms/resend)
    @POST("auth/sms/resend")
    suspend fun resendSmsCode(
        @Body request: SmsCodeRequest
    ): BaseResponse<Boolean>
}
