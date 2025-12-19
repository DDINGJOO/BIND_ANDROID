package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.response.BaseResponse
import com.teambind.bind_android.data.model.response.PricingPolicyByDateResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PricingPolicyService {

    // 특정 날짜의 룸 가격 정책 조회 (서버: GET /bff/v1/pricing-policies/{roomId}/date/{date})
    @GET("pricing-policies/{roomId}/date/{date}")
    suspend fun getRoomPricingPolicyByDate(
        @Path("roomId") roomId: Long,
        @Path("date") date: String
    ): BaseResponse<PricingPolicyByDateResponse>
}
