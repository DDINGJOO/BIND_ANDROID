package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.request.ReportCreateRequest
import com.teambind.bind_android.data.model.response.BaseResponse
import com.teambind.bind_android.data.model.response.EmptyResult
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportService {

    // 신고 등록 (서버: POST /bff/v1/support/reports)
    @POST("support/reports")
    suspend fun createReport(
        @Body request: ReportCreateRequest
    ): BaseResponse<EmptyResult>
}
