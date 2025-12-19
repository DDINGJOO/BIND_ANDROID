package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.response.BaseResponse
import com.teambind.bind_android.data.model.response.FeedResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedService {

    // 미니피드 조회 (category: article, comment, like)
    @GET("activities/feed/{category}")
    suspend fun getFeed(
        @Path("category") category: String,
        @Query("targetUserId") targetUserId: String,
        @Query("size") size: Int = 20,
        @Query("cursor") cursor: String? = null
    ): BaseResponse<FeedResponse>
}
