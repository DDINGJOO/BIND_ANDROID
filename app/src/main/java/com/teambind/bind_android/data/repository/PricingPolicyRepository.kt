package com.teambind.bind_android.data.repository

import com.teambind.bind_android.data.api.service.PricingPolicyService
import com.teambind.bind_android.data.model.response.PricingPolicyByDateResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PricingPolicyRepository @Inject constructor(
    private val pricingPolicyService: PricingPolicyService
) {

    // 특정 날짜의 룸 가격 정책 조회
    suspend fun getRoomPricingPolicyByDate(
        roomId: Long,
        date: String
    ): Result<PricingPolicyByDateResponse> {
        return try {
            val response = pricingPolicyService.getRoomPricingPolicyByDate(roomId, date)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("가격 정책 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
