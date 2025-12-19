package com.teambind.bind_android.data.repository

import com.teambind.bind_android.data.api.service.HomeService
import com.teambind.bind_android.data.model.response.NoticeEventListResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val homeService: HomeService
) {

    // 이벤트 목록 조회 (배너용)
    suspend fun getEvents(size: Int = 10): Result<NoticeEventListResponse> {
        return try {
            val response = homeService.getEvents(size)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("이벤트 목록 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 공지사항 목록 조회
    suspend fun getNotices(size: Int = 10): Result<NoticeEventListResponse> {
        return try {
            val response = homeService.getNotices(size)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("공지사항 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
