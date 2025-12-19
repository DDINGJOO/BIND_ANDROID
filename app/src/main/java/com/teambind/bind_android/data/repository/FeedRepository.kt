package com.teambind.bind_android.data.repository

import com.teambind.bind_android.data.api.service.FeedService
import com.teambind.bind_android.data.model.response.FeedResponse
import javax.inject.Inject
import javax.inject.Singleton

enum class FeedCategory(val value: String) {
    ARTICLE("article"),
    COMMENT("comment"),
    LIKE("like")
}

@Singleton
class FeedRepository @Inject constructor(
    private val feedService: FeedService
) {

    // 미니피드 조회
    suspend fun getFeed(
        category: FeedCategory,
        targetUserId: String,
        size: Int = 20,
        cursor: String? = null
    ): Result<FeedResponse> {
        return try {
            val response = feedService.getFeed(
                category = category.value,
                targetUserId = targetUserId,
                size = size,
                cursor = cursor
            )
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("피드 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("피드 조회에 실패했습니다."))
        }
    }
}
