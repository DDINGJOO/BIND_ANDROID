package com.teambind.bind_android.data.repository

import com.teambind.bind_android.data.api.service.EnumsService
import com.teambind.bind_android.data.model.response.PlaceKeywordDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnumsRepository @Inject constructor(
    private val enumsService: EnumsService
) {

    // 지역 정보 조회
    suspend fun getRegions(): Result<Map<String, String>> {
        return try {
            val response = enumsService.getRegions()
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("지역 정보 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 게시글 키워드 목록 조회
    suspend fun getArticleKeywords(): Result<List<Any>> {
        return try {
            val response = enumsService.getArticleKeywords()
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("게시글 키워드 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 장소 키워드 목록 조회
    suspend fun getPlaceKeywords(): Result<List<PlaceKeywordDto>> {
        return try {
            val response = enumsService.getPlaceKeywords()
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("장소 키워드 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
