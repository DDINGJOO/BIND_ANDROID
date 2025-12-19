package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.request.PaymentConfirmRequest
import com.teambind.bind_android.data.model.response.BaseResponse
import com.teambind.bind_android.data.model.response.PaymentResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentService {

    // 결제 확인 (서버: POST /api/v1/payments/confirm)
    @POST("payments/confirm")
    suspend fun confirmPayment(
        @Body request: PaymentConfirmRequest
    ): BaseResponse<PaymentResponse>
}
