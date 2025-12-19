package com.teambind.bind_android.data.repository

import android.graphics.Bitmap
import com.teambind.bind_android.data.api.service.ProfileService
import com.teambind.bind_android.data.model.ImageUploadPurpose
import com.teambind.bind_android.data.model.request.CreateProfileRequest
import com.teambind.bind_android.data.model.request.EditProfileRequest
import com.teambind.bind_android.data.model.response.ImageUploadResponse
import com.teambind.bind_android.data.model.response.ProfileResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileService: ProfileService,
    private val imageRepository: ImageRepository
) {

    // 내 프로필 조회
    suspend fun getMyProfile(): Result<ProfileResponse> {
        return try {
            val response = profileService.getMyProfile()
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("프로필 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 다른 유저 프로필 조회
    suspend fun getProfile(userId: String): Result<ProfileResponse> {
        return try {
            val response = profileService.getProfile(userId)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("프로필 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 프로필 이미지 업로드
    suspend fun uploadProfileImage(
        bitmap: Bitmap,
        userId: String
    ): Result<ImageUploadResponse> {
        return imageRepository.uploadImage(
            bitmap = bitmap,
            fileName = "profile_$userId",
            purpose = ImageUploadPurpose.PROFILE,
            uploaderId = userId
        )
    }

    // 프로필 생성 (신규 가입자)
    suspend fun createProfile(request: CreateProfileRequest): Result<Boolean> {
        return try {
            val response = profileService.createProfile(request)
            if (response.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(Exception("프로필 생성에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 프로필 수정
    suspend fun editProfile(request: EditProfileRequest): Result<Boolean> {
        return try {
            val response = profileService.editProfile(request)
            if (response.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(Exception("프로필 수정에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 닉네임 중복 확인
    suspend fun checkNicknameAvailable(nickname: String): Result<Boolean> {
        return try {
            val response = profileService.checkNicknameAvailable(type = "nickname", nickname = nickname)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("닉네임 확인에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
