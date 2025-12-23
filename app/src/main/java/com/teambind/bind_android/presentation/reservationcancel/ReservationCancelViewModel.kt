package com.teambind.bind_android.presentation.reservationcancel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teambind.bind_android.data.model.response.MyReservationDetailResponse
import com.teambind.bind_android.data.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ReservationCancelUiState(
    val isLoading: Boolean = false,
    val reservationDetail: MyReservationDetailResponse? = null,
    val totalPrice: Int = 0,
    val cancelFee: Int = 0,
    val refundAmount: Int = 0,
    val cancelFeePolicy: String = "",
    val canCancel: Boolean = true,
    val errorMessage: String? = null
)

sealed class ReservationCancelEvent {
    object CancelSuccess : ReservationCancelEvent()
    data class CancelError(val message: String) : ReservationCancelEvent()
    data class ShowConfirmDialog(val message: String) : ReservationCancelEvent()
}

@HiltViewModel
class ReservationCancelViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationCancelUiState())
    val uiState: StateFlow<ReservationCancelUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<ReservationCancelEvent>()
    val event = _event.asSharedFlow()

    private var reservationId: Long = 0

    fun loadReservationDetail(id: Long) {
        reservationId = id
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            reservationRepository.getMyReservationDetail(id)
                .onSuccess { detail ->
                    val cancelInfo = calculateCancelFee(detail)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        reservationDetail = detail,
                        totalPrice = detail.totalPrice,
                        cancelFee = cancelInfo.cancelFee,
                        refundAmount = cancelInfo.refundAmount,
                        cancelFeePolicy = cancelInfo.policyText,
                        canCancel = cancelInfo.canCancel
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "예약 정보를 불러올 수 없습니다."
                    )
                }
        }
    }

    fun onCancelClick() {
        viewModelScope.launch {
            val state = _uiState.value
            if (!state.canCancel) {
                _event.emit(ReservationCancelEvent.CancelError("이용 당일에는 취소가 불가능합니다."))
                return@launch
            }

            if (state.reservationDetail?.status == "CONFIRMED") {
                _event.emit(ReservationCancelEvent.CancelError("이미 확정된 예약은 취소할 수 없습니다. 고객센터에 문의해주세요."))
                return@launch
            }

            val message = if (state.cancelFee > 0) {
                "취소 수수료 ${formatPrice(state.cancelFee)}원이 발생합니다.\n정말 취소하시겠습니까?"
            } else {
                "예약을 취소하시겠습니까?\n전액 환불됩니다."
            }
            _event.emit(ReservationCancelEvent.ShowConfirmDialog(message))
        }
    }

    fun confirmCancel() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val status = _uiState.value.reservationDetail?.status ?: ""
            val result = when {
                status == "PENDING_CONFIRMED" || status == "PENDING" -> {
                    reservationRepository.cancelPayment(reservationId)
                }
                status == "CONFIRMED" || status == "REJECTED" -> {
                    reservationRepository.requestRefund(reservationId)
                }
                else -> {
                    reservationRepository.cancelReservation(reservationId)
                }
            }

            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _event.emit(ReservationCancelEvent.CancelSuccess)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _event.emit(ReservationCancelEvent.CancelError(e.message ?: "예약 취소에 실패했습니다."))
                }
        }
    }

    private fun calculateCancelFee(detail: MyReservationDetailResponse): CancelInfo {
        val totalPrice = detail.totalPrice
        val reservationDate = detail.reservationDate
        val createdAt = detail.createdAt

        // 예약 생성 후 10분 이내인지 확인
        if (isWithin10Minutes(createdAt)) {
            return CancelInfo(
                cancelFee = 0,
                refundAmount = totalPrice,
                policyText = "예약 후 10분 이내 취소로 전액 환불됩니다.",
                canCancel = true
            )
        }

        // 이용일까지 남은 일수 계산
        val daysUntilUse = calculateDaysUntilUse(reservationDate)

        return when {
            daysUntilUse >= 5 -> CancelInfo(
                cancelFee = 0,
                refundAmount = totalPrice,
                policyText = "이용 5일 전 이상 취소 시 100% 환불",
                canCancel = true
            )
            daysUntilUse == 4L -> {
                val fee = (totalPrice * 0.3).toInt()
                CancelInfo(
                    cancelFee = fee,
                    refundAmount = totalPrice - fee,
                    policyText = "이용 4일 전 취소 시 70% 환불 (수수료 30%)",
                    canCancel = true
                )
            }
            daysUntilUse == 3L -> {
                val fee = (totalPrice * 0.5).toInt()
                CancelInfo(
                    cancelFee = fee,
                    refundAmount = totalPrice - fee,
                    policyText = "이용 3일 전 취소 시 50% 환불 (수수료 50%)",
                    canCancel = true
                )
            }
            daysUntilUse == 2L -> {
                val fee = (totalPrice * 0.7).toInt()
                CancelInfo(
                    cancelFee = fee,
                    refundAmount = totalPrice - fee,
                    policyText = "이용 2일 전 취소 시 30% 환불 (수수료 70%)",
                    canCancel = true
                )
            }
            daysUntilUse == 1L -> {
                val fee = (totalPrice * 0.9).toInt()
                CancelInfo(
                    cancelFee = fee,
                    refundAmount = totalPrice - fee,
                    policyText = "이용 1일 전 취소 시 10% 환불 (수수료 90%)",
                    canCancel = true
                )
            }
            else -> CancelInfo(
                cancelFee = totalPrice,
                refundAmount = 0,
                policyText = "이용 당일 취소 시 환불 불가",
                canCancel = false
            )
        }
    }

    private fun isWithin10Minutes(createdAt: String?): Boolean {
        if (createdAt == null) return false
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
            val createdDate = formatter.parse(createdAt) ?: return false
            val now = Date()
            val diffMinutes = (now.time - createdDate.time) / (1000 * 60)
            diffMinutes < 10
        } catch (e: Exception) {
            false
        }
    }

    private fun calculateDaysUntilUse(reservationDate: String): Long {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val useDate = LocalDate.parse(reservationDate, formatter)
            val today = LocalDate.now()
            ChronoUnit.DAYS.between(today, useDate)
        } catch (e: Exception) {
            0
        }
    }

    private fun formatPrice(price: Int): String {
        return String.format("%,d", price)
    }

    private data class CancelInfo(
        val cancelFee: Int,
        val refundAmount: Int,
        val policyText: String,
        val canCancel: Boolean
    )
}
