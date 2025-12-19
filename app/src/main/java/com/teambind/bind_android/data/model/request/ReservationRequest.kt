package com.teambind.bind_android.data.model.request

import com.google.gson.annotations.SerializedName

// 예약 생성 요청 (기존)
data class CreateReservationRequest(
    @SerializedName("roomId")
    val roomId: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("endTime")
    val endTime: String,
    @SerializedName("headCount")
    val headCount: Int
)

// 예약 취소 요청
data class CancelReservationRequest(
    @SerializedName("reservationId")
    val reservationId: String,
    @SerializedName("reason")
    val reason: String?
)

// ==================== iOS 기준 예약 API 요청 ====================

// 예약 가격 미리보기 요청 (ISO8601 형식: "2025-12-19T09:00:00")
data class ReservationPreviewRequest(
    @SerializedName("roomId")
    val roomId: Long,
    @SerializedName("timeSlots")
    val timeSlots: List<String>,  // ISO8601 형식: "yyyy-MM-ddTHH:mm:ss"
    @SerializedName("products")
    val products: List<ProductQuantity>?
)

data class ProductQuantity(
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("quantity")
    val quantity: Int
)

// 다중 슬롯 예약 요청 (room-reservations/multi)
data class MultiSlotReservationRequest(
    @SerializedName("roomId")
    val roomId: Long,
    @SerializedName("slotDate")
    val slotDate: String,
    @SerializedName("slotTimes")
    val slotTimes: List<String>  // "HH:mm" 형식
)

// 예약 상품 업데이트 요청
data class UpdateProductsRequest(
    @SerializedName("products")
    val products: List<ProductQuantity>
)

// 예약자 정보 업데이트 요청 (iOS와 동일한 형식)
data class UpdateUserInfoRequest(
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("reserverName")
    val reserverName: String,
    @SerializedName("reserverPhone")
    val reserverPhone: String,
    @SerializedName("additionalInfo")
    val additionalInfo: Map<String, String>?
)
