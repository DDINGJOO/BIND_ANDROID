package com.teambind.bind_android.data.model.response

import com.google.gson.annotations.SerializedName

// ==================== 예약 가격 미리보기 ====================

data class ReservationPreviewResponse(
    @SerializedName("timeSlotPrice")
    val timeSlotPrice: Int,
    @SerializedName("productBreakdowns")
    val productBreakdowns: List<ProductBreakdown>,
    @SerializedName("totalPrice")
    val totalPrice: Int
) {
    val basePrice: Int get() = timeSlotPrice
}

data class ProductBreakdown(
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("productName")
    val productName: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("unitPrice")
    val unitPrice: Int,
    @SerializedName("subtotal")
    val subtotal: Int
)

// ==================== 예약 시간 확정 결과 ====================

data class ReservationTimeResultResponse(
    @SerializedName("reservationId")
    val reservationId: Long,
    @SerializedName("roomId")
    val roomId: Long,
    @SerializedName("slotDate")
    val slotDate: String,
    @SerializedName("reservedSlotTimes")
    val reservedSlotTimes: List<String>
)

// ==================== 예약 업데이트 결과 ====================

data class UpdatedReservationResponse(
    @SerializedName("reservationId")
    val reservationId: Long,
    @SerializedName("roomId")
    val roomId: Long,
    @SerializedName("totalPrice")
    val totalPrice: Int,
    @SerializedName("calculatedAt")
    val calculatedAt: String
)

// ==================== 내 예약 목록 ====================

data class MyReservationsResponse(
    @SerializedName("content")
    val content: List<MyReservationDto>,
    @SerializedName("cursor")
    val cursor: CursorDto?,
    @SerializedName("size")
    val size: Int
)

data class MyReservationDto(
    @SerializedName("reservationId")
    val reservationId: Long,
    @SerializedName("reservationDate")
    val reservationDate: String,
    @SerializedName("startTimes")
    val startTimes: List<String>,
    @SerializedName("status")
    val status: String,
    @SerializedName("totalPrice")
    val totalPrice: Int,
    @SerializedName("reserverName")
    val reserverName: String?,
    @SerializedName("reserverPhone")
    val reserverPhone: String?,
    @SerializedName("placeInfo")
    val placeInfo: ReservationPlaceInfoDto?,
    @SerializedName("roomInfo")
    val roomInfo: ReservationRoomInfoDto?
) {
    // Convenience accessors for backward compatibility
    val placeName: String? get() = placeInfo?.placeName
    val roomName: String? get() = roomInfo?.roomName
    val firstImageUrl: String? get() = roomInfo?.imageUrls?.firstOrNull()
    val placeId: Long get() = placeInfo?.placeId ?: 0L
    val roomId: Long get() = roomInfo?.roomId ?: 0L
}

data class ReservationPlaceInfoDto(
    @SerializedName("placeId")
    val placeId: Long,
    @SerializedName("placeName")
    val placeName: String?
)

data class ReservationRoomInfoDto(
    @SerializedName("roomId")
    val roomId: Long,
    @SerializedName("roomName")
    val roomName: String?,
    @SerializedName("imageUrls")
    val imageUrls: List<String>?,
    @SerializedName("timeSlot")
    val timeSlot: String?
)

data class CursorDto(
    @SerializedName("next")
    val next: String?,
    @SerializedName("hasNext")
    val hasNext: Boolean
)

// ==================== 내 예약 상세 ====================

data class MyReservationDetailResponse(
    @SerializedName("reservationId")
    val reservationId: Long,
    @SerializedName("userId")
    val userId: Long?,
    @SerializedName("placeInfo")
    val placeInfo: DetailPlaceInfoDto?,
    @SerializedName("roomInfo")
    val roomInfo: ReservationRoomInfoDto?,
    @SerializedName("status")
    val status: String,
    @SerializedName("reservationDate")
    val reservationDate: String,
    @SerializedName("startTimes")
    val startTimes: List<String>?,
    @SerializedName("totalPrice")
    val totalPrice: Int,
    @SerializedName("reservationTimePrice")
    val reservationTimePrice: Int,
    @SerializedName("isBlackUser")
    val isBlackUser: Boolean?,
    @SerializedName("reserverName")
    val reserverName: String?,
    @SerializedName("reserverPhone")
    val reserverPhone: String?,
    @SerializedName("selectedProducts")
    val selectedProducts: List<SelectedProductDto>?,
    @SerializedName("additionalInfo")
    val additionalInfo: Map<String, Any>?,
    @SerializedName("approvedAt")
    val approvedAt: String?,
    @SerializedName("approvedBy")
    val approvedBy: Long?,
    @SerializedName("rejectedAt")
    val rejectedAt: String?,
    @SerializedName("rejectedReason")
    val rejectedReason: String?,
    @SerializedName("rejectedBy")
    val rejectedBy: Long?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
) {
    // Convenience accessors
    val placeName: String? get() = placeInfo?.placeName
    val placeAddress: String? get() = placeInfo?.fullAddress
    val roomName: String? get() = roomInfo?.roomName
    val firstImageUrl: String? get() = roomInfo?.imageUrls?.firstOrNull()
}

data class DetailPlaceInfoDto(
    @SerializedName("placeId")
    val placeId: Long,
    @SerializedName("placeName")
    val placeName: String?,
    @SerializedName("fullAddress")
    val fullAddress: String?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?
)

data class SelectedProductDto(
    @SerializedName("productId")
    val productId: String?,
    @SerializedName("productName")
    val productName: String?,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("unitPrice")
    val unitPrice: Int,
    @SerializedName("subtotal")
    val subtotal: Int
)

// ==================== 예약 필드 ====================

data class ReservationFieldDto(
    @SerializedName("fieldId")
    val fieldId: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("inputType")
    val inputType: String,
    @SerializedName("required")
    val required: Boolean,
    @SerializedName("maxLength")
    val maxLength: Int?,
    @SerializedName("sequence")
    val sequence: Int
)

// ==================== 시간 슬롯 ====================

data class TimeSlotDto(
    @SerializedName("slotId")
    val slotId: Long,
    @SerializedName("roomId")
    val roomId: Long,
    @SerializedName("slotDate")
    val slotDate: String,
    @SerializedName("slotTime")
    val slotTime: String,
    @SerializedName("status")
    val status: String?
)

// ==================== 가격 정책 (날짜별) ====================

data class PricingPolicyByDateResponse(
    @SerializedName("timeSlotPrices")
    val timeSlotPrices: Map<String, Int>
)

// ==================== 상품 가용성 ====================

data class ProductsAvailabilityResponse(
    @SerializedName("roomId")
    val roomId: Long,
    @SerializedName("placeId")
    val placeId: Long,
    @SerializedName("availableProducts")
    val availableProducts: List<AvailableProductDto>
)

data class AvailableProductDto(
    @SerializedName("productId")
    val productId: Long,
    @SerializedName("productName")
    val productName: String,
    @SerializedName("unitPrice")
    val unitPrice: Int,
    @SerializedName("availableQuantity")
    val availableQuantity: Int,
    @SerializedName("totalStock")
    val totalStock: Int
)
