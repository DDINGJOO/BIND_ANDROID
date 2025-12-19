package com.teambind.bind_android.data.repository

import com.teambind.bind_android.data.api.service.ReservationService
import com.teambind.bind_android.data.model.request.*
import com.teambind.bind_android.data.model.response.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationRepository @Inject constructor(
    private val reservationService: ReservationService
) {

    // ==================== 내 예약 관련 ====================

    // 내 예약 목록 조회
    suspend fun getMyReservations(
        status: String? = null,
        size: Int = 20,
        cursor: String? = null
    ): Result<MyReservationsResponse> {
        return try {
            val response = reservationService.getMyReservations(status, size, cursor)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("예약 목록 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 내 예약 상세 조회
    suspend fun getMyReservationDetail(reservationId: Long): Result<MyReservationDetailResponse> {
        return try {
            val response = reservationService.getMyReservationDetail(reservationId)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("예약 상세 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 예약 생성 플로우 ====================

    // 예약 가격 미리보기
    // slotDate: "yyyy-MM-dd", slotTimes: ["HH:mm", "HH:mm"] -> ISO8601 형식으로 변환
    suspend fun getReservationPreview(
        roomId: Long,
        slotDate: String,
        slotTimes: List<String>,
        products: List<ProductQuantity>? = null
    ): Result<ReservationPreviewResponse> {
        return try {
            // iOS와 동일하게 ISO8601 형식으로 변환: "2025-12-19T09:00:00"
            val isoTimeSlots = slotTimes.map { time ->
                "${slotDate}T${time}:00"
            }
            val request = ReservationPreviewRequest(roomId, isoTimeSlots, products)
            val response = reservationService.getReservationPreview(request)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("예약 미리보기 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 다중 슬롯 예약 - 시간 확정
    suspend fun createMultiSlotReservation(
        roomId: Long,
        slotDate: String,
        slotTimes: List<String>
    ): Result<ReservationTimeResultResponse> {
        return try {
            val request = MultiSlotReservationRequest(roomId, slotDate, slotTimes)
            val response = reservationService.createMultiSlotReservation(request)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("예약 시간 확정에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 예약 상품 업데이트
    suspend fun updateReservationProducts(
        reservationId: Long,
        products: List<ProductQuantity>
    ): Result<UpdatedReservationResponse> {
        return try {
            val request = UpdateProductsRequest(products)
            val response = reservationService.updateReservationProducts(reservationId, request)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("상품 업데이트에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 예약자 정보 업데이트 (iOS와 동일한 형식)
    suspend fun updateReservationUserInfo(
        reservationId: Long,
        userId: Long,
        reserverName: String,
        reserverPhone: String,
        additionalInfo: Map<String, String>? = null
    ): Result<Unit> {
        return try {
            val request = UpdateUserInfoRequest(userId, reserverName, reserverPhone, additionalInfo)
            val response = reservationService.updateReservationUserInfo(reservationId, request)
            if (response.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("예약자 정보 업데이트에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 예약 확정
    suspend fun confirmReservation(reservationId: Long): Result<UpdatedReservationResponse> {
        return try {
            val response = reservationService.confirmReservation(reservationId)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("예약 확정에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 예약 취소
    suspend fun cancelReservation(reservationId: Long): Result<Unit> {
        return try {
            val response = reservationService.cancelReservation(reservationId)
            if (response.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("예약 취소에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 결제 취소 (PENDING_CONFIRMED 상태)
    suspend fun cancelPayment(reservationId: Long): Result<String> {
        return try {
            val response = reservationService.cancelPayment(reservationId)
            if (response.isSuccess) {
                Result.success(response.result ?: "결제가 취소되었습니다.")
            } else {
                Result.failure(Exception("결제 취소에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 환불 요청 (CONFIRMED/REJECTED 상태)
    suspend fun requestRefund(reservationId: Long): Result<String> {
        return try {
            val response = reservationService.requestRefund(reservationId)
            if (response.isSuccess) {
                Result.success(response.result ?: "환불 요청이 접수되었습니다.")
            } else {
                Result.failure(Exception("환불 요청에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 예약 가능 시간 조회 ====================

    // 예약 가능 슬롯 조회
    suspend fun getAvailableSlots(roomId: Long, date: String): Result<List<TimeSlotDto>> {
        return try {
            val response = reservationService.getAvailableSlots(roomId, date)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("예약 가능 시간 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 예약 필드 조회 ====================

    // 예약 추가 정보 필드 조회
    suspend fun getReservationFields(roomId: Long): Result<List<ReservationFieldDto>> {
        return try {
            val response = reservationService.getReservationFields(roomId)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("예약 필드 조회에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 기존 API (하위 호환) ====================

    // 예약 생성 (기존)
    suspend fun createReservation(request: CreateReservationRequest): Result<ReservationTimeResultResponse> {
        return try {
            val response = reservationService.createReservation(request)
            if (response.isSuccess && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("예약 생성에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 예약 취소 (기존 - 사유 포함)
    suspend fun cancelReservationWithReason(reservationId: String, reason: String?): Result<Boolean> {
        return try {
            val request = CancelReservationRequest(reservationId = reservationId, reason = reason)
            val response = reservationService.cancelReservationWithReason(reservationId, request)
            if (response.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(Exception("예약 취소에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
