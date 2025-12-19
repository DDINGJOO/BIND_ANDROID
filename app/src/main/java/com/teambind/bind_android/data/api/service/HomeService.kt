package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.response.BaseResponse
import com.teambind.bind_android.data.model.response.NoticeEventListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeService {

    // 이벤트/배너 목록 조회 (서버: GET /bff/v1/communities/articles/events)
    @GET("communities/articles/events")
    suspend fun getEvents(
        @Query("size") size: Int = 10
    ): BaseResponse<NoticeEventListResponse>

    // 공지사항 목록 (서버: GET /bff/v1/communities/articles/notices)
    @GET("communities/articles/notices")
    suspend fun getNotices(
        @Query("size") size: Int = 10
    ): BaseResponse<NoticeEventListResponse>
}
