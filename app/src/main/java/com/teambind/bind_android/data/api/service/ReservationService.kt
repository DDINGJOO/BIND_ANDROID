package com.teambind.bind_android.data.api.service

import com.teambind.bind_android.data.model.request.*
import com.teambind.bind_android.data.model.response.*
import retrofit2.http.*

interface ReservationService {

    // ==================== 내 예약 관련 ====================

    // 내 예약 목록 조회 (서버: GET /bff/v1/reservations/me)
    @GET("reservations/me")
    suspend fun getMyReservations(
        @Query("status") status: String? = null,
        @Query("size") size: Int? = null,
        @Query("cursor") cursor: String? = null
    ): BaseResponse<MyReservationsResponse>

    // 내 예약 상세 조회 (서버: GET /bff/v1/reservations/detail/{reservationId})
    @GET("reservations/detail/{reservationId}")
    suspend fun getMyReservationDetail(
        @Path("reservationId") reservationId: Long
    ): BaseResponse<MyReservationDetailResponse>

    // ==================== 예약 생성 플로우 ====================

    // 예약 가격 미리보기 (서버: POST /bff/v1/reservations/preview)
    @POST("reservations/preview")
    suspend fun getReservationPreview(
        @Body request: ReservationPreviewRequest
    ): BaseResponse<ReservationPreviewResponse>

    // 다중 슬롯 예약 - 시간 확정 (서버: POST /bff/v1/room-reservations/multi)
    @POST("room-reservations/multi")
    suspend fun createMultiSlotReservation(
        @Body request: MultiSlotReservationRequest
    ): BaseResponse<ReservationTimeResultResponse>

    // 예약 상품 업데이트 (서버: PUT /bff/v1/reservations/{reservationId}/products)
    @PUT("reservations/{reservationId}/products")
    suspend fun updateReservationProducts(
        @Path("reservationId") reservationId: Long,
        @Body request: UpdateProductsRequest
    ): BaseResponse<UpdatedReservationResponse>

    // 예약자 정보 업데이트 (서버: POST /bff/v1/reservations/{reservationId}/user-info)
    @POST("reservations/{reservationId}/user-info")
    suspend fun updateReservationUserInfo(
        @Path("reservationId") reservationId: Long,
        @Body request: UpdateUserInfoRequest
    ): BaseResponse<Unit>

    // 예약 확정 (서버: PUT /bff/v1/reservations/{reservationId}/confirm)
    @PUT("reservations/{reservationId}/confirm")
    suspend fun confirmReservation(
        @Path("reservationId") reservationId: Long
    ): BaseResponse<UpdatedReservationResponse>

    // 예약 취소 (서버: PUT /bff/v1/reservations/{reservationId}/cancel)
    @PUT("reservations/{reservationId}/cancel")
    suspend fun cancelReservation(
        @Path("reservationId") reservationId: Long
    ): BaseResponse<Unit>

    // 결제 취소 - PENDING_CONFIRMED 상태 (서버: POST /bff/v1/reservations/{id}/cancel)
    @POST("reservations/{reservationId}/cancel")
    suspend fun cancelPayment(
        @Path("reservationId") reservationId: Long
    ): BaseResponse<String>

    // 환불 요청 - CONFIRMED/REJECTED 상태 (서버: POST /bff/v1/reservations/{id}/refund)
    @POST("reservations/{reservationId}/refund")
    suspend fun requestRefund(
        @Path("reservationId") reservationId: Long
    ): BaseResponse<String>

    // ==================== 예약 가능 시간 조회 ====================

    // 예약 가능 슬롯 조회 (서버: GET /bff/v1/room-reservations/available-slots)
    @GET("room-reservations/available-slots")
    suspend fun getAvailableSlots(
        @Query("roomId") roomId: Long,
        @Query("date") date: String
    ): BaseResponse<List<TimeSlotDto>>

    // ==================== 예약 필드 조회 ====================

    // 예약 추가 정보 필드 조회 (서버: GET /bff/v1/rooms/{roomId}/reservation-fields)
    @GET("rooms/{roomId}/reservation-fields")
    suspend fun getReservationFields(
        @Path("roomId") roomId: Long
    ): BaseResponse<List<ReservationFieldDto>>

    // ==================== 기존 API (하위 호환) ====================

    // 예약 생성 (서버: POST /bff/v1/reservations)
    @POST("reservations")
    suspend fun createReservation(
        @Body request: CreateReservationRequest
    ): BaseResponse<ReservationTimeResultResponse>

    // 예약 취소 (기존 - Body 포함)
    @POST("reservations/{reservationId}/cancel")
    suspend fun cancelReservationWithReason(
        @Path("reservationId") reservationId: String,
        @Body request: CancelReservationRequest
    ): BaseResponse<Boolean>
}
