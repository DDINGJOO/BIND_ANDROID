package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.request.CreateProfileRequest
import com.teambind.bind_android.data.model.request.EditProfileRequest
import com.teambind.bind_android.data.model.response.BaseResponse
import com.teambind.bind_android.data.model.response.ProfileResponse
import retrofit2.http.*

interface ProfileService {

    // 내 프로필 조회 (서버: GET /bff/v1/profiles/me)
    @GET("profiles/me")
    suspend fun getMyProfile(): BaseResponse<ProfileResponse>

    // 다른 사용자 프로필 조회 (서버: GET /bff/v1/profiles
    // /{userId})
    @GET("profiles/{userId}")
    suspend fun getProfile(
        @Path("userId") userId: String
    ): BaseResponse<ProfileResponse>

    // 프로필 생성 (서버: POST /bff/v1/profiles)
    @POST("profiles")
    suspend fun createProfile(
        @Body request: CreateProfileRequest
    ): BaseResponse<Boolean>

    // 프로필 수정 (서버: PUT /bff/v1/profiles/me)
    @PUT("profiles/me")
    suspend fun editProfile(
        @Body request: EditProfileRequest
    ): BaseResponse<Boolean>

    // 닉네임 중복 확인 (서버: GET /bff/v1/profiles/validate)
    @GET("profiles/validate")
    suspend fun checkNicknameAvailable(
        @Query("type") type: String = "nickname",
        @Query("value") nickname: String
    ): BaseResponse<Boolean>

    // 회원 탈퇴 (서버: DELETE /bff/v1/profiles/me)
    @DELETE("profiles/me")
    suspend fun withdraw(): BaseResponse<Boolean>

    // 비밀번호 변경 (서버: PUT /bff/v1/profiles/password)
    @PUT("profiles/password")
    suspend fun changePassword(
        @Body request: Map<String, String>
    ): BaseResponse<Boolean>
}
