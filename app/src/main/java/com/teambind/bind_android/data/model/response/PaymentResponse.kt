package com.teambind.bind_android.data.model.response

import com.google.gson.annotations.SerializedName

data class PaymentResponse(
    @SerializedName("paymentId")
    val paymentId: String,
    @SerializedName("reservationId")
    val reservationId: String,
    @SerializedName("orderId")
    val orderId: String,
    @SerializedName("paymentKey")
    val paymentKey: String,
    @SerializedName("transactionId")
    val transactionId: String,
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("method")
    val method: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("completedAt")
    val completedAt: String
)

// PlaceKeyword DTO (Enums API)
data class PlaceKeywordDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("displayOrder")
    val displayOrder: Int
)
