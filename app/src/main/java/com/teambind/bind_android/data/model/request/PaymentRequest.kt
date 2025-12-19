package com.teambind.bind_android.data.model.request

import com.google.gson.annotations.SerializedName

data class PaymentConfirmRequest(
    @SerializedName("reservationId")
    val reservationId: Long,
    @SerializedName("paymentKey")
    val paymentKey: String,
    @SerializedName("orderId")
    val orderId: String,
    @SerializedName("amount")
    val amount: Int
)
