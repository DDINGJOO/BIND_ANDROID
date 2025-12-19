package com.teambind.bind_android.data.repository

import com.teambind.bind_android.data.api.service.PaymentService
import com.teambind.bind_android.data.model.request.PaymentConfirmRequest
import com.teambind.bind_android.data.model.response.PaymentResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentService: PaymentService
) {

    // 결제 확인
    suspend fun confirmPayment(
        reservationId: Long,
        paymentKey: String,
        orderId: String,
        amount: Int
    ): Result<PaymentResponse> {
        return try {
            val request = PaymentConfirmRequest(reservationId, paymentKey, orderId, amount)
            val response = paymentService.confirmPayment(request)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("결제 확인에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
