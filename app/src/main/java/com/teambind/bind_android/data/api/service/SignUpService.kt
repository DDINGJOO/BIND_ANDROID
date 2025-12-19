package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.request.SignUpRequest
import com.teambind.bind_android.data.model.response.BaseResponse
import com.teambind.bind_android.data.model.response.EmptyResult
import retrofit2.http.*

interface SignUpService {

    // 이메일 인증 코드 발송 (서버: POST /bff/v1/auth/emails/{email})
    // 서버 응답: data에 문자열 반환
    @POST("auth/emails/{email}")
    suspend fun requestEmailCode(
        @Path("email") email: String
    ): BaseResponse<String>

    // 이메일 인증 코드 확인 (서버: GET /bff/v1/auth/emails/{email}?code={code})
    // 서버 응답: data에 Boolean 반환
    @GET("auth/emails/{email}")
    suspend fun verifyEmailCode(
        @Path("email") email: String,
        @Query("code") code: String
    ): BaseResponse<Boolean>

    // 회원가입 (서버: POST /bff/v1/auth/signup)
    @POST("auth/signup")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): BaseResponse<EmptyResult>
}
